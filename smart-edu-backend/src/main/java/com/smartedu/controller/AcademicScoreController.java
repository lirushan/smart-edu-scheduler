package com.smartedu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.Result;
import com.smartedu.entity.RegScore;
import com.smartedu.service.AcademicScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 教务成绩审核控制器
 */
@RestController
@RequestMapping("/api/v1/academic/scores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class AcademicScoreController {

    private final AcademicScoreService academicScoreService;

    /**
     * 待审核成绩列表（分页）
     */
    @GetMapping("/review")
    public Result<Page<RegScore>> reviewList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long offeringId) {
        return Result.ok(academicScoreService.listReviewScores(page, size, status, offeringId));
    }

    /**
     * 审核通过（发布）
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        academicScoreService.approveScore(id);
        return Result.ok();
    }

    /**
     * 批量发布
     */
    @PutMapping("/batch-approve")
    public Result<Void> batchApprove(@RequestBody Map<String, List<Long>> body) {
        academicScoreService.batchApprove(body.get("ids"));
        return Result.ok();
    }

    /**
     * 驳回（退回草稿）
     */
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id) {
        academicScoreService.rejectScore(id);
        return Result.ok();
    }
}
