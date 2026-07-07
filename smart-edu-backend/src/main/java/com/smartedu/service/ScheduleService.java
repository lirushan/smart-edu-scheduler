package com.smartedu.service;

import com.smartedu.entity.CrsOffering;
import com.smartedu.entity.RegEnrollment;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.CrsOfferingMapper;
import com.smartedu.mapper.RegEnrollmentMapper;
import com.smartedu.mapper.SysUserMapper;
import com.smartedu.model.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 课表服务
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final RegEnrollmentMapper enrollmentMapper;
    private final CrsOfferingMapper offeringMapper;
    private final SysUserMapper userMapper;

    /**
     * 查询当前用户课表（学生：已选课程；教师：授课课程）
     */
    public List<ScheduleVO> getMySchedule(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) return List.of();

        List<ScheduleVO> schedule = new ArrayList<>();

        if (user.getUserType() == 1) {
            // 学生：从选课记录获取
            List<RegEnrollment> enrollments = enrollmentMapper.selectByStudentId(userId);
            for (RegEnrollment e : enrollments) {
                schedule.add(ScheduleVO.builder()
                        .offeringId(e.getOfferingId())
                        .courseName(e.getCourseName())
                        .teacherName(e.getTeacherName())
                        .location(e.getLocation())
                        .weekday(e.getWeekday())
                        .periodStart(e.getPeriodStart())
                        .periodEnd(e.getPeriodEnd())
                        .credit(e.getCredit())
                        .build());
            }
        } else if (user.getUserType() == 2) {
            // 教师：从授课记录获取
            List<CrsOffering> offerings = offeringMapper.selectByTeacherId(userId);
            for (CrsOffering o : offerings) {
                schedule.add(ScheduleVO.builder()
                        .offeringId(o.getId())
                        .courseName(o.getCourseName())
                        .teacherName(user.getRealName())
                        .location(o.getLocation())
                        .weekday(o.getWeekday())
                        .periodStart(o.getPeriodStart())
                        .periodEnd(o.getPeriodEnd())
                        .credit(o.getCredit())
                        .build());
            }
        }

        schedule.sort(Comparator.comparing(ScheduleVO::getWeekday)
                .thenComparing(ScheduleVO::getPeriodStart));
        return schedule;
    }

    /**
     * 查询指定学生课表（教师/教务用）
     */
    public List<ScheduleVO> getStudentSchedule(Long studentId) {
        List<RegEnrollment> enrollments = enrollmentMapper.selectByStudentId(studentId);
        return enrollments.stream().map(e -> ScheduleVO.builder()
                .offeringId(e.getOfferingId())
                .courseName(e.getCourseName())
                .teacherName(e.getTeacherName())
                .location(e.getLocation())
                .weekday(e.getWeekday())
                .periodStart(e.getPeriodStart())
                .periodEnd(e.getPeriodEnd())
                .credit(e.getCredit())
                .build()).sorted(Comparator.comparing(ScheduleVO::getWeekday)
                .thenComparing(ScheduleVO::getPeriodStart)).toList();
    }
}
