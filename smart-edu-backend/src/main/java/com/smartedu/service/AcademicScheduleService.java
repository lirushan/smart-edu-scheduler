package com.smartedu.service;

import com.smartedu.entity.CrsOffering;
import com.smartedu.entity.RegEnrollment;
import com.smartedu.mapper.CrsOfferingMapper;
import com.smartedu.mapper.RegEnrollmentMapper;
import com.smartedu.model.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 教务排课管理服务
 */
@Service
@RequiredArgsConstructor
public class AcademicScheduleService {

    private final CrsOfferingMapper offeringMapper;
    private final RegEnrollmentMapper enrollmentMapper;

    /**
     * 获取所有课程列表（用于筛选下拉）
     */
    public List<CrsOffering> getAllOfferings() {
        List<CrsOffering> offerings = offeringMapper.selectList(null);
        // 填充关联字段
        for (CrsOffering o : offerings) {
            CrsOffering detail = offeringMapper.selectDetailById(o.getId());
            if (detail != null) {
                o.setCourseName(detail.getCourseName());
                o.setTeacherName(detail.getTeacherName());
                o.setCredit(detail.getCredit());
            }
        }
        return offerings;
    }

    /**
     * 按课程/学期筛选课表
     */
    public List<ScheduleVO> getFilteredSchedules(Long offeringId, String semester) {
        List<CrsOffering> offerings;
        if (offeringId != null) {
            CrsOffering o = offeringMapper.selectDetailById(offeringId);
            offerings = o != null ? List.of(o) : List.of();
        } else if (semester != null && !semester.isBlank()) {
            offerings = offeringMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrsOffering>()
                            .eq(CrsOffering::getSemester, semester)
                            .eq(CrsOffering::getStatus, 1));
            for (CrsOffering o : offerings) {
                CrsOffering detail = offeringMapper.selectDetailById(o.getId());
                if (detail != null) {
                    o.setCourseName(detail.getCourseName());
                    o.setTeacherName(detail.getTeacherName());
                    o.setCredit(detail.getCredit());
                }
            }
        } else {
            offerings = getAllOfferings();
        }

        List<ScheduleVO> schedule = new ArrayList<>();
        for (CrsOffering o : offerings) {
            if (o.getWeekday() == null) continue;
            schedule.add(ScheduleVO.builder()
                    .offeringId(o.getId())
                    .courseName(o.getCourseName())
                    .teacherName(o.getTeacherName())
                    .location(o.getLocation())
                    .weekday(o.getWeekday())
                    .periodStart(o.getPeriodStart())
                    .periodEnd(o.getPeriodEnd())
                    .credit(o.getCredit())
                    .build());
        }

        schedule.sort(Comparator.comparing(ScheduleVO::getWeekday)
                .thenComparing(ScheduleVO::getPeriodStart));
        return schedule;
    }

    /**
     * 获取所有学期列表（用于下拉）
     */
    public List<String> getAllSemesters() {
        List<CrsOffering> offerings = offeringMapper.selectList(null);
        return offerings.stream()
                .map(CrsOffering::getSemester)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .toList();
    }
}
