package com.smartedu.controller;

import com.smartedu.common.PageResult;
import com.smartedu.common.Result;
import com.smartedu.entity.SysUser;
import com.smartedu.model.vo.CourseOfferingVO;
import com.smartedu.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 课程审核控制器
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ACADEMIC','ADMIN')")
public class CourseApprovalController {

    private final CourseService courseService;

    @GetMapping("/offerings")
    public Result<PageResult<CourseOfferingVO>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        SysUser user = getCurrentUser();
        return Result.ok(courseService.listPendingApprovals(page, size, user));
    }

    @GetMapping("/offerings/{id}")
    public Result<CourseOfferingVO> detail(@PathVariable Long id) {
        return Result.ok(courseService.getOfferingDetail(id));
    }

    @PutMapping("/offerings/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        courseService.approve(id, body.getOrDefault("comment", ""));
        return Result.ok();
    }

    @PutMapping("/offerings/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        courseService.reject(id, body.getOrDefault("comment", ""));
        return Result.ok();
    }

    private SysUser getCurrentUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
