package com.smartedu.controller;

import com.smartedu.common.PageResult;
import com.smartedu.common.Result;
import com.smartedu.entity.ExamQuestion;
import com.smartedu.entity.SysUser;
import com.smartedu.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 题库控制器
 */
@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','QB_ADMIN')")
    public Result<PageResult<ExamQuestion>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer questionType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer scope,
            @RequestParam(required = false) Integer auditStatus) {
        SysUser user = getCurrentUser();
        return Result.ok(questionService.listQuestions(page, size, questionType, keyword, scope, auditStatus, user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','QB_ADMIN')")
    public Result<ExamQuestion> get(@PathVariable Long id) {
        return Result.ok(questionService.getQuestion(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER','QB_ADMIN')")
    public Result<ExamQuestion> create(@RequestBody ExamQuestion question) {
        SysUser user = getCurrentUser();
        return Result.ok(questionService.createQuestion(question, user));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','QB_ADMIN')")
    public Result<ExamQuestion> update(@PathVariable Long id, @RequestBody ExamQuestion question) {
        return Result.ok(questionService.updateQuestion(id, question));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER','QB_ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.ok();
    }

    @GetMapping("/audit/list")
    @PreAuthorize("hasRole('QB_ADMIN')")
    public Result<PageResult<ExamQuestion>> auditList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(questionService.listPendingAudit(page, size));
    }

    @PutMapping("/{id}/audit")
    @PreAuthorize("hasRole('QB_ADMIN')")
    public Result<ExamQuestion> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = getCurrentUser();
        Integer auditStatus = (Integer) body.get("auditStatus");
        String comment = (String) body.getOrDefault("comment", "");
        return Result.ok(questionService.auditQuestion(id, auditStatus, comment, user));
    }

    private SysUser getCurrentUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
