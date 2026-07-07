package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.RegEnrollment;
import com.smartedu.entity.SysUser;
import com.smartedu.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选课控制器
 */
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/my")
    public Result<List<RegEnrollment>> myEnrollments() {
        SysUser user = getCurrentUser();
        return Result.ok(enrollmentService.myEnrollments(user.getId()));
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public Result<RegEnrollment> enroll(@RequestBody Map<String, Long> body) {
        SysUser user = getCurrentUser();
        return Result.ok(enrollmentService.enroll(user.getId(), body.get("offeringId")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<Void> drop(@PathVariable Long id) {
        SysUser user = getCurrentUser();
        enrollmentService.drop(id, user.getId());
        return Result.ok();
    }

    @GetMapping("/log")
    public Result<String> log() {
        return Result.ok("选课日志功能（V2.0）");
    }

    private SysUser getCurrentUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
