package com.smartedu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.Result;
import com.smartedu.entity.ExamExam;
import com.smartedu.model.dto.ExamManageDTO;
import com.smartedu.service.AcademicExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 教务考试管理控制器
 */
@RestController
@RequestMapping("/api/v1/academic/exams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicExamController {

    private final AcademicExamService academicExamService;

    @GetMapping
    public Result<Page<ExamExam>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(academicExamService.listExams(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<ExamExam> detail(@PathVariable Long id) {
        return Result.ok(academicExamService.getDetail(id));
    }

    @PostMapping
    public Result<ExamExam> create(@RequestBody ExamManageDTO dto) {
        return Result.ok(academicExamService.createExam(dto));
    }

    @PutMapping("/{id}")
    public Result<ExamExam> update(@PathVariable Long id, @RequestBody ExamManageDTO dto) {
        return Result.ok(academicExamService.updateExam(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        academicExamService.deleteExam(id);
        return Result.ok();
    }
}
