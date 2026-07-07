package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.config.RabbitMQConfig;
import com.smartedu.entity.CrsCourse;
import com.smartedu.entity.CrsOffering;
import com.smartedu.entity.RegEnrollment;
import com.smartedu.entity.RegRound;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.CrsCourseMapper;
import com.smartedu.mapper.CrsOfferingMapper;
import com.smartedu.mapper.RegEnrollmentMapper;
import com.smartedu.mapper.RegRoundMapper;
import com.smartedu.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 选课服务：抢课（Redisson 分布式锁 + Redis 原子扣减）、退课
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final RegEnrollmentMapper enrollmentMapper;
    private final CrsOfferingMapper offeringMapper;
    private final CrsCourseMapper courseMapper;
    private final RegRoundMapper roundMapper;
    private final SysUserMapper userMapper;
    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final String QUOTA_KEY_PREFIX = "enroll:quota:";

    /**
     * 选课 — 高并发抢课核心
     */
    @Transactional
    public RegEnrollment enroll(Long studentId, Long offeringId) {
        // 1. 查询开课实例
        CrsOffering offering = offeringMapper.selectById(offeringId);
        if (offering == null || offering.getStatus() != 1) {
            throw new BusinessException(BizError.COURSE_NOT_FOUND);
        }

        // 2. 查询学生信息
        SysUser student = userMapper.selectById(studentId);
        if (student == null || student.getStatus() != 1) {
            throw new BusinessException(BizError.ACCOUNT_DISABLED);
        }

        // 3. 校验选课轮次
        RegRound activeRound = getActiveRound();
        if (activeRound == null) {
            throw new BusinessException(BizError.ENROLL_TIME_NOT_OPEN);
        }

        // 4. 校验年级限制
        if (activeRound.getTargetGrades() != null && student.getGrade() != null) {
            if (!activeRound.getTargetGrades().contains(student.getGrade())) {
                throw new BusinessException(BizError.ENROLL_TIME_NOT_OPEN.getCode(), "当前年级不在选课范围内");
            }
        }

        // 5. 校验是否重复选课
        Long existingCount = enrollmentMapper.selectCount(
                new LambdaQueryWrapper<RegEnrollment>()
                        .eq(RegEnrollment::getStudentId, studentId)
                        .eq(RegEnrollment::getOfferingId, offeringId)
                        .eq(RegEnrollment::getStatus, 0));
        if (existingCount > 0) {
            throw new BusinessException(BizError.DUPLICATE_ENROLL);
        }

        // 5.5 校验时间冲突
        List<RegEnrollment> existingEnrollments = enrollmentMapper.selectByStudentId(studentId);
        for (RegEnrollment e : existingEnrollments) {
            CrsOffering existingOffering = offeringMapper.selectById(e.getOfferingId());
            if (existingOffering != null
                    && existingOffering.getWeekday().equals(offering.getWeekday())
                    && !(offering.getPeriodEnd() < existingOffering.getPeriodStart()
                         || offering.getPeriodStart() > existingOffering.getPeriodEnd())) {
                throw new BusinessException(BizError.TIME_CONFLICT);
            }
        }

        // 6. 校验门数/学分限制
        int currentCount = enrollmentMapper.countByStudentId(studentId);
        if (activeRound.getMaxCourses() != null && currentCount >= activeRound.getMaxCourses()) {
            throw new BusinessException(BizError.ENROLL_COUNT_EXCEEDED);
        }
        BigDecimal currentCredits = enrollmentMapper.sumCreditsByStudentId(studentId);
        // 计算新课程学分
        CrsCourse course = courseMapper.selectById(offering.getCourseId());
        BigDecimal newCredit = course != null && course.getCredit() != null ? course.getCredit() : BigDecimal.ZERO;
        BigDecimal totalCredits = currentCredits.add(newCredit);
        if (activeRound.getMaxCredits() != null
                && totalCredits.compareTo(new BigDecimal(activeRound.getMaxCredits())) > 0) {
            throw new BusinessException(BizError.CREDIT_EXCEEDED);
        }

        // 7. Redis 分布式锁 + 原子扣减
        String lockKey = "enroll:offering:" + offeringId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                throw new BusinessException(429, "系统繁忙，请稍后重试");
            }

            String quotaKey = QUOTA_KEY_PREFIX + offeringId;
            // 初始化 Redis 名额计数
            redisTemplate.opsForValue().setIfAbsent(quotaKey, offering.getCapacity() - offering.getEnrolledCount());

            Long remaining = redisTemplate.opsForValue().decrement(quotaKey);
            if (remaining == null || remaining < 0) {
                redisTemplate.opsForValue().increment(quotaKey); // 归还
                throw new BusinessException(BizError.COURSE_FULL);
            }

            // 8. 写入选课记录
            RegEnrollment enrollment = new RegEnrollment();
            enrollment.setStudentId(studentId);
            enrollment.setOfferingId(offeringId);
            enrollment.setRoundId(activeRound.getId());
            enrollment.setStatus(0);
            enrollment.setCreatedAt(LocalDateTime.now());
            enrollmentMapper.insert(enrollment);

            // 9. 异步更新 DB enrolled_count（MQ）
            rabbitTemplate.convertAndSend(RabbitMQConfig.ENROLLMENT_EXCHANGE,
                    RabbitMQConfig.ENROLLMENT_SYNC_KEY, offeringId);

            log.info("选课成功: student={}, offering={}, remaining={}", studentId, offeringId, remaining);
            return enrollment;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(500, "系统异常");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 退课
     */
    @Transactional
    public void drop(Long enrollmentId, Long studentId) {
        RegEnrollment enrollment = enrollmentMapper.selectById(enrollmentId);
        if (enrollment == null || !enrollment.getStudentId().equals(studentId)) {
            throw new BusinessException(BizError.ENROLL_NOT_FOUND);
        }
        if (enrollment.getStatus() == 1) {
            throw new BusinessException(BizError.ALREADY_DROPPED);
        }

        // 归还 Redis 名额
        String quotaKey = QUOTA_KEY_PREFIX + enrollment.getOfferingId();
        redisTemplate.opsForValue().increment(quotaKey);

        // 更新选课状态
        enrollment.setStatus(1);
        enrollment.setDroppedAt(LocalDateTime.now());
        enrollmentMapper.updateById(enrollment);

        // 异步更新 DB enrolled_count
        rabbitTemplate.convertAndSend(RabbitMQConfig.ENROLLMENT_EXCHANGE,
                RabbitMQConfig.ENROLLMENT_SYNC_KEY, enrollment.getOfferingId());

        log.info("退课成功: enrollment={}, student={}", enrollmentId, studentId);
    }

    /**
     * 我的选课列表
     */
    public List<RegEnrollment> myEnrollments(Long studentId) {
        return enrollmentMapper.selectByStudentId(studentId);
    }

    /**
     * 获取当前活跃轮次
     */
    private RegRound getActiveRound() {
        List<RegRound> rounds = roundMapper.selectList(
                new LambdaQueryWrapper<RegRound>()
                        .eq(RegRound::getStatus, 1)
                        .le(RegRound::getStartTime, LocalDateTime.now())
                        .ge(RegRound::getEndTime, LocalDateTime.now())
                        .orderByDesc(RegRound::getCreateTime));
        return rounds.isEmpty() ? null : rounds.get(0);
    }
}
