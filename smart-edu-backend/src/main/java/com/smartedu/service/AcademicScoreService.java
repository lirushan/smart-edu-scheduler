package com.smartedu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.RegScore;
import com.smartedu.mapper.RegScoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 教务成绩审核服务
 */
@Service
@RequiredArgsConstructor
public class AcademicScoreService {

    private final RegScoreMapper scoreMapper;

    /**
     * 待审核成绩列表（分页）
     */
    public Page<RegScore> listReviewScores(int page, int size, Integer status, Long offeringId) {
        Page<RegScore> pageParam = new Page<>(page, size);
        return (Page<RegScore>) scoreMapper.selectPageWithDetails(pageParam, status, offeringId);
    }

    /**
     * 审核通过（发布成绩）
     */
    @Transactional
    public void approveScore(Long id) {
        RegScore score = scoreMapper.selectById(id);
        if (score == null) throw new BusinessException(BizError.NOT_FOUND);
        if (score.getStatus() == 1) throw new BusinessException(BizError.SCORE_ALREADY_PUBLISHED);
        score.setStatus(1);
        score.setUpdateTime(LocalDateTime.now());
        scoreMapper.updateById(score);
    }

    /**
     * 批量发布
     */
    @Transactional
    public void batchApprove(java.util.List<Long> ids) {
        for (Long id : ids) {
            approveScore(id);
        }
    }

    /**
     * 驳回（退回草稿）
     */
    @Transactional
    public void rejectScore(Long id) {
        RegScore score = scoreMapper.selectById(id);
        if (score == null) throw new BusinessException(BizError.NOT_FOUND);
        if (score.getStatus() == 0) throw new BusinessException(BizError.BAD_REQUEST.getCode(), "成绩已是草稿状态");
        score.setStatus(0);
        score.setUpdateTime(LocalDateTime.now());
        scoreMapper.updateById(score);
    }
}
