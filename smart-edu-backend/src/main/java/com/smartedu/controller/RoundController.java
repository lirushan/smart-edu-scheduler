package com.smartedu.controller;

import com.smartedu.common.Result;
import com.smartedu.entity.RegRound;
import com.smartedu.service.RoundService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 选课轮次控制器
 */
@RestController
@RequestMapping("/api/v1/academic/rounds")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ACADEMIC')")
public class RoundController {

    private final RoundService roundService;

    @GetMapping
    public Result<List<RegRound>> list() {
        return Result.ok(roundService.listRounds());
    }

    @PostMapping
    public Result<RegRound> create(@RequestBody RegRound round) {
        return Result.ok(roundService.createRound(round));
    }

    @PutMapping("/{id}")
    public Result<RegRound> update(@PathVariable Long id, @RequestBody RegRound round) {
        return Result.ok(roundService.updateRound(id, round));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roundService.deleteRound(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<RegRound> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return Result.ok(roundService.toggleStatus(id, body.get("status")));
    }
}
