package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.RegScore;
import com.smartedu.entity.SysUser;
import com.smartedu.service.ScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成绩控制器
 */
@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping("/my")
    public Result<List<RegScore>> myScores() {
        SysUser user = getCurrentUser();
        return Result.ok(scoreService.getMyScores(user.getId()));
    }

    @GetMapping("/offering/{offeringId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<List<RegScore>> scoresByOffering(@PathVariable Long offeringId) {
        return Result.ok(scoreService.getScoresByOffering(offeringId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<RegScore> updateScore(@PathVariable Long id, @RequestBody RegScore score) {
        SysUser user = getCurrentUser();
        return Result.ok(scoreService.saveScore(id, score.getStudentId(),
                score.getOfferingId(), score.getRawScore(), user.getId()));
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Void> batchSave(@RequestBody List<RegScore> scores) {
        SysUser user = getCurrentUser();
        scoreService.batchSaveScores(scores, user.getId());
        return Result.ok();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasRole('TEACHER')")
    public Result<Void> publish(@PathVariable Long id) {
        scoreService.publishScore(id);
        return Result.ok();
    }

    private SysUser getCurrentUser() {
        return (SysUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
