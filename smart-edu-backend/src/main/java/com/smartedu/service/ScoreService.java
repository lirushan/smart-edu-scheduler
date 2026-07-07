package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.RegScore;
import com.smartedu.entity.RegEnrollment;
import com.smartedu.entity.SysUser;
import com.smartedu.mapper.RegScoreMapper;
import com.smartedu.mapper.RegEnrollmentMapper;
import com.smartedu.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 成绩服务：录入/批量导入/查询/发布 + 五级制转换 + GPA计算
 */
@Service
@RequiredArgsConstructor
public class ScoreService {

    private final RegScoreMapper scoreMapper;
    private final RegEnrollmentMapper enrollmentMapper;
    private final SysUserMapper userMapper;

    /**
     * 我的成绩
     */
    public List<RegScore> getMyScores(Long studentId) {
        return scoreMapper.selectByStudentId(studentId);
    }

    /**
     * 按课程查看学生成绩
     */
    public List<RegScore> getScoresByOffering(Long offeringId) {
        return scoreMapper.selectByOfferingId(offeringId);
    }

    /**
     * 录入/修改单条成绩
     */
    @Transactional
    public RegScore saveScore(Long id, Long studentId, Long offeringId,
                               BigDecimal rawScore, Long enteredBy) {
        RegScore score;
        if (id != null) {
            score = scoreMapper.selectById(id);
            if (score == null) throw new BusinessException(BizError.NOT_FOUND);
            if (score.getStatus() == 1) throw new BusinessException(BizError.SCORE_ALREADY_PUBLISHED);
        } else {
            // 检查是否已有成绩
            Long count = scoreMapper.selectCount(
                    new LambdaQueryWrapper<RegScore>()
                            .eq(RegScore::getStudentId, studentId)
                            .eq(RegScore::getOfferingId, offeringId));
            if (count > 0) throw new BusinessException(BizError.CONFLICT.getCode(), "该学生已有成绩记录");

            score = new RegScore();
            score.setStudentId(studentId);
            score.setOfferingId(offeringId);
            score.setStatus(0);
        }

        score.setRawScore(rawScore);
        score.setGradeLevel(convertToFiveLevel(rawScore));
        score.setGpa(convertToGpa(rawScore));
        score.setEnteredBy(enteredBy);
        if (id != null) {
            scoreMapper.updateById(score);
        } else {
            scoreMapper.insert(score);
        }
        return score;
    }

    /**
     * 批量录入成绩
     */
    @Transactional
    public void batchSaveScores(List<RegScore> scores, Long enteredBy) {
        for (RegScore score : scores) {
            saveScore(null, score.getStudentId(), score.getOfferingId(),
                    score.getRawScore(), enteredBy);
        }
    }

    /**
     * 发布成绩
     */
    public void publishScore(Long id) {
        RegScore score = scoreMapper.selectById(id);
        if (score == null) throw new BusinessException(BizError.NOT_FOUND);
        score.setStatus(1);
        scoreMapper.updateById(score);
    }

    /**
     * 百分制 → 五级制
     * null/空值统一返回空字符串，由前端展示为"N/A"
     */
    public static String convertToFiveLevel(BigDecimal rawScore) {
        if (rawScore == null) return "";
        double s = rawScore.doubleValue();
        if (s >= 90) return "优秀";
        if (s >= 80) return "良好";
        if (s >= 70) return "中等";
        if (s >= 60) return "及格";
        return "不及格";
    }

    /**
     * 百分制 → GPA
     */
    public static BigDecimal convertToGpa(BigDecimal rawScore) {
        if (rawScore == null) return BigDecimal.ZERO;
        double s = rawScore.doubleValue();
        if (s >= 90) return new BigDecimal("4.0");
        if (s >= 80) return new BigDecimal("3.0");
        if (s >= 70) return new BigDecimal("2.0");
        if (s >= 60) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }
}
