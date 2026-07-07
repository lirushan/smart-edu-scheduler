package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.SysEvaluation;
import com.smartedu.model.vo.EvaluationStatsVO;
import com.smartedu.service.AcademicEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 教务教学评价控制器
 */
@RestController
@RequestMapping("/api/v1/academic/evaluations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicEvaluationController {

    private final AcademicEvaluationService academicEvaluationService;

    /**
     * 评价统计列表
     */
    @GetMapping
    public Result<List<EvaluationStatsVO>> stats() {
        return Result.ok(academicEvaluationService.getStats());
    }

    /**
     * 某教师的评价详情
     */
    @GetMapping("/{teacherId}")
    public Result<List<SysEvaluation>> teacherDetails(@PathVariable Long teacherId) {
        return Result.ok(academicEvaluationService.getByTeacherId(teacherId));
    }

    /**
     * 所有评价详情
     */
    @GetMapping("/all")
    public Result<List<SysEvaluation>> allDetails() {
        return Result.ok(academicEvaluationService.getAllDetails());
    }
}
