package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.RegRound;
import com.smartedu.mapper.RegRoundMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 选课轮次服务
 */
@Service
@RequiredArgsConstructor
public class RoundService {

    private final RegRoundMapper roundMapper;

    /**
     * 轮次列表
     */
    public List<RegRound> listRounds() {
        return roundMapper.selectList(
                new LambdaQueryWrapper<RegRound>().orderByDesc(RegRound::getCreateTime));
    }

    /**
     * 创建轮次
     */
    public RegRound createRound(RegRound round) {
        roundMapper.insert(round);
        return round;
    }

    /**
     * 编辑轮次
     */
    public RegRound updateRound(Long id, RegRound round) {
        RegRound existing = roundMapper.selectById(id);
        if (existing == null) throw new BusinessException(BizError.NOT_FOUND);

        if (round.getRoundName() != null) existing.setRoundName(round.getRoundName());
        if (round.getStartTime() != null) existing.setStartTime(round.getStartTime());
        if (round.getEndTime() != null) existing.setEndTime(round.getEndTime());
        if (round.getMaxCredits() != null) existing.setMaxCredits(round.getMaxCredits());
        if (round.getMaxCourses() != null) existing.setMaxCourses(round.getMaxCourses());
        if (round.getTargetGrades() != null) existing.setTargetGrades(round.getTargetGrades());
        if (round.getAgeMin() != null) existing.setAgeMin(round.getAgeMin());
        if (round.getAgeMax() != null) existing.setAgeMax(round.getAgeMax());

        roundMapper.updateById(existing);
        return existing;
    }

    /**
     * 删除轮次
     */
    public void deleteRound(Long id) {
        roundMapper.deleteById(id);
    }

    /**
     * 切换轮次状态
     */
    public RegRound toggleStatus(Long id, Integer status) {
        RegRound round = roundMapper.selectById(id);
        if (round == null) throw new BusinessException(BizError.NOT_FOUND);
        round.setStatus(status);
        roundMapper.updateById(round);
        return round;
    }
}
