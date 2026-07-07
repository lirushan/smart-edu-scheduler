package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.ExamExam;
import com.smartedu.entity.ExamQuestion;
import com.smartedu.entity.SysUser;
import com.smartedu.model.dto.ExamSubmitRequest;
import com.smartedu.model.vo.ExamResultVO;
import com.smartedu.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考试控制器
 */
@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public Result<List<ExamExam>> list() {
        SysUser user = getCurrentUser();
        return Result.ok(examService.listExams(user.getId()));
    }

    @GetMapping("/{id}")
    public Result<ExamExam> detail(@PathVariable Long id) {
        return Result.ok(examService.getExamDetail(id));
    }

    @GetMapping("/{id}/questions")
    public Result<List<ExamQuestion>> questions(@PathVariable Long id) {
        return Result.ok(examService.getExamQuestions(id));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ExamResultVO> start(@PathVariable Long id) {
        SysUser user = getCurrentUser();
        return Result.ok(examService.startExam(id, user.getId()));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ExamResultVO> submit(@PathVariable Long id, @RequestBody ExamSubmitRequest request) {
        SysUser user = getCurrentUser();
        return Result.ok(examService.submitExam(id, user.getId(), request.getAnswers()));
    }

    @GetMapping("/{id}/results/my")
    @PreAuthorize("hasRole('STUDENT')")
    public Result<ExamResultVO> myResult(@PathVariable Long id) {
        SysUser user = getCurrentUser();
        return Result.ok(examService.getMyResult(id, user.getId()));
    }

    private SysUser getCurrentUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
