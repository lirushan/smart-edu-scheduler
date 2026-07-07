package com.smartedu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.entity.SysEvaluation;
import com.smartedu.mapper.SysEvaluationMapper;
import com.smartedu.model.vo.EvaluationStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 教务教学评价服务
 */
@Service
@RequiredArgsConstructor
public class AcademicEvaluationService {

    private final SysEvaluationMapper evaluationMapper;

    /**
     * 评价统计列表
     */
    public List<EvaluationStatsVO> getStats() {
        List<Map<String, Object>> rows = evaluationMapper.selectTeacherStats();
        return rows.stream().map(row -> EvaluationStatsVO.builder()
                .teacherId((Long) row.get("teacherId"))
                .teacherName((String) row.get("teacherName"))
                .evalCount((Long) row.get("evalCount"))
                .avgScore(((Number) row.get("avgScore")).doubleValue())
                .build()).collect(Collectors.toList());
    }

    /**
     * 获取某教师的评价详情
     */
    public List<SysEvaluation> getByTeacherId(Long teacherId) {
        return evaluationMapper.selectByTeacherId(teacherId);
    }

    /**
     * 获取所有评价详情
     */
    public List<SysEvaluation> getAllDetails() {
        return evaluationMapper.selectAllWithDetails();
    }
}
