package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.model.vo.EnrollmentDetailVO;
import com.smartedu.model.vo.EnrollmentStatsVO;
import com.smartedu.service.AcademicEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教务选课监控控制器
 */
@RestController
@RequestMapping("/api/v1/academic/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicEnrollmentController {

    private final AcademicEnrollmentService academicEnrollmentService;

    /**
     * 选课统计
     */
    @GetMapping("/stats")
    public Result<EnrollmentStatsVO> stats() {
        return Result.ok(academicEnrollmentService.getStats());
    }

    /**
     * 选课详情列表
     */
    @GetMapping("/details")
    public Result<List<EnrollmentDetailVO>> details(@RequestParam(required = false) Long offeringId) {
        return Result.ok(academicEnrollmentService.getDetails(offeringId));
    }
}
