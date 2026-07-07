package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartedu.entity.CrsOffering;
import com.smartedu.mapper.CrsOfferingMapper;
import com.smartedu.mapper.RegEnrollmentMapper;
import com.smartedu.model.vo.EnrollmentDetailVO;
import com.smartedu.model.vo.EnrollmentStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 教务选课监控服务
 */
@Service
@RequiredArgsConstructor
public class AcademicEnrollmentService {

    private final CrsOfferingMapper offeringMapper;
    private final RegEnrollmentMapper enrollmentMapper;

    /**
     * 选课统计
     */
    public EnrollmentStatsVO getStats() {
        LambdaQueryWrapper<CrsOffering> wrapper = new LambdaQueryWrapper<CrsOffering>()
                .eq(CrsOffering::getStatus, 1);
        List<CrsOffering> offerings = offeringMapper.selectList(wrapper);

        long totalOfferings = offerings.size();
        int totalCapacity = offerings.stream().mapToInt(o -> o.getCapacity() != null ? o.getCapacity() : 0).sum();
        int totalEnrolled = offerings.stream().mapToInt(o -> o.getEnrolledCount() != null ? o.getEnrolledCount() : 0).sum();

        BigDecimal usageRate = totalCapacity > 0
                ? BigDecimal.valueOf(totalEnrolled).divide(BigDecimal.valueOf(totalCapacity), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return EnrollmentStatsVO.builder()
                .totalOfferings(totalOfferings)
                .totalEnrollments((long) totalEnrolled)
                .capacityUsageRate(usageRate)
                .totalCapacity(totalCapacity)
                .totalEnrolled(totalEnrolled)
                .build();
    }

    /**
     * 选课详情列表
     */
    public List<EnrollmentDetailVO> getDetails(Long offeringId) {
        LambdaQueryWrapper<CrsOffering> wrapper = new LambdaQueryWrapper<CrsOffering>()
                .eq(CrsOffering::getStatus, 1)
                .orderByDesc(CrsOffering::getEnrolledCount);
        if (offeringId != null) {
            wrapper.eq(CrsOffering::getId, offeringId);
        }
        List<CrsOffering> offerings = offeringMapper.selectList(wrapper);

        // 需要关联查询获取课程名和教师名
        return offerings.stream().map(o -> {
            CrsOffering detail = offeringMapper.selectDetailById(o.getId());
            String courseName = detail != null && detail.getCourseName() != null ? detail.getCourseName() : "";
            String teacherName = detail != null && detail.getTeacherName() != null ? detail.getTeacherName() : "";
            Integer cap = o.getCapacity() != null ? o.getCapacity() : 0;
            Integer enrolled = o.getEnrolledCount() != null ? o.getEnrolledCount() : 0;
            BigDecimal fillRate = cap > 0
                    ? BigDecimal.valueOf(enrolled).divide(BigDecimal.valueOf(cap), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            return EnrollmentDetailVO.builder()
                    .offeringId(o.getId())
                    .courseName(courseName)
                    .teacherName(teacherName)
                    .semester(o.getSemester())
                    .capacity(cap)
                    .enrolledCount(enrolled)
                    .fillRate(fillRate)
                    .build();
        }).collect(Collectors.toList());
    }
}
