package com.smartedu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.Result;
import com.smartedu.entity.SysTrainingPlan;
import com.smartedu.service.AcademicTrainingPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 教务培养方案控制器
 */
@RestController
@RequestMapping("/api/v1/academic/training-plans")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicTrainingPlanController {

    private final AcademicTrainingPlanService academicTrainingPlanService;

    @GetMapping
    public Result<Page<SysTrainingPlan>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String major) {
        return Result.ok(academicTrainingPlanService.listPlans(page, size, major));
    }

    @GetMapping("/{id}")
    public Result<SysTrainingPlan> detail(@PathVariable Long id) {
        return Result.ok(academicTrainingPlanService.getDetail(id));
    }

    @PostMapping
    public Result<SysTrainingPlan> create(@RequestBody SysTrainingPlan plan) {
        return Result.ok(academicTrainingPlanService.createPlan(plan));
    }

    @PutMapping("/{id}")
    public Result<SysTrainingPlan> update(@PathVariable Long id, @RequestBody SysTrainingPlan plan) {
        return Result.ok(academicTrainingPlanService.updatePlan(id, plan));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        academicTrainingPlanService.deletePlan(id);
        return Result.ok();
    }
}
