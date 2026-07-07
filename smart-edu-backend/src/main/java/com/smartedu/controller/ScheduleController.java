package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.SysUser;
import com.smartedu.model.vo.ScheduleVO;
import com.smartedu.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课表控制器
 */
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/my")
    public Result<List<ScheduleVO>> mySchedule() {
        SysUser user = getCurrentUser();
        return Result.ok(scheduleService.getMySchedule(user.getId()));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER','ACADEMIC')")
    public Result<List<ScheduleVO>> studentSchedule(@PathVariable Long studentId) {
        return Result.ok(scheduleService.getStudentSchedule(studentId));
    }

    private SysUser getCurrentUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
