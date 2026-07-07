package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.entity.ExamExam;
import com.smartedu.mapper.ExamExamMapper;
import com.smartedu.model.dto.ExamManageDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 教务考试管理服务
 */
@Service
@RequiredArgsConstructor
public class AcademicExamService {

    private final ExamExamMapper examMapper;

    /**
     * 分页查询考试列表
     */
    public Page<ExamExam> listExams(int page, int size, String keyword) {
        Page<ExamExam> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ExamExam> wrapper = new LambdaQueryWrapper<ExamExam>()
                .orderByDesc(ExamExam::getCreateTime);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(ExamExam::getExamName, keyword);
        }
        Page<ExamExam> result = examMapper.selectPage(pageParam, wrapper);
        // 填充课程名
        List<ExamExam> allWithCourse = examMapper.selectAllWithCourse();
        for (ExamExam record : result.getRecords()) {
            allWithCourse.stream()
                    .filter(e -> e.getId().equals(record.getId()))
                    .findFirst()
                    .ifPresent(e -> record.setCourseName(e.getCourseName()));
        }
        return result;
    }

    /**
     * 创建考试
     */
    @Transactional
    public ExamExam createExam(ExamManageDTO dto) {
        if (dto.getStartTime() != null && dto.getEndTime() != null
                && dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new BusinessException(BizError.BAD_REQUEST.getCode(), "开始时间不能晚于结束时间");
        }
        ExamExam exam = new ExamExam();
        exam.setOfferingId(dto.getOfferingId());
        exam.setExamName(dto.getExamName());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 120);
        exam.setTotalScore(dto.getTotalScore() != null ? dto.getTotalScore() : 100);
        exam.setStatus(0);
        exam.setCreateTime(LocalDateTime.now());
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.insert(exam);
        return exam;
    }

    /**
     * 更新考试
     */
    @Transactional
    public ExamExam updateExam(Long id, ExamManageDTO dto) {
        ExamExam exam = examMapper.selectById(id);
        if (exam == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);
        if (dto.getOfferingId() != null) exam.setOfferingId(dto.getOfferingId());
        if (dto.getExamName() != null) exam.setExamName(dto.getExamName());
        if (dto.getStartTime() != null) exam.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) exam.setEndTime(dto.getEndTime());
        if (dto.getDurationMinutes() != null) exam.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getTotalScore() != null) exam.setTotalScore(dto.getTotalScore());
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.updateById(exam);
        return exam;
    }

    /**
     * 删除考试
     */
    @Transactional
    public void deleteExam(Long id) {
        ExamExam exam = examMapper.selectById(id);
        if (exam == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);
        examMapper.deleteById(id);
    }

    /**
     * 获取考试详情
     */
    public ExamExam getDetail(Long id) {
        ExamExam exam = examMapper.selectById(id);
        if (exam == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);
        List<ExamExam> allWithCourse = examMapper.selectAllWithCourse();
        allWithCourse.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .ifPresent(e -> exam.setCourseName(e.getCourseName()));
        return exam;
    }
}
