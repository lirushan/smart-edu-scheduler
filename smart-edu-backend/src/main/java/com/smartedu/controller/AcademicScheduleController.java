package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.model.vo.ScheduleVO;
import com.smartedu.service.AcademicScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教务排课管理控制器
 */
@RestController
@RequestMapping("/api/v1/academic/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicScheduleController {

    private final AcademicScheduleService academicScheduleService;

    /**
     * 获取所有学期列表
     */
    @GetMapping("/semesters")
    public Result<List<String>> semesters() {
        return Result.ok(academicScheduleService.getAllSemesters());
    }

    /**
     * 获取所有课程（用于下拉筛选）
     */
    @GetMapping("/offerings")
    public Result<?> offerings() {
        return Result.ok(academicScheduleService.getAllOfferings());
    }

    /**
     * 按条件筛选课表
     */
    @GetMapping
    public Result<List<ScheduleVO>> schedules(
            @RequestParam(required = false) Long offeringId,
            @RequestParam(required = false) String semester) {
        return Result.ok(academicScheduleService.getFilteredSchedules(offeringId, semester));
    }
}
