package com.smartedu.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartedu.common.BizError;
import com.smartedu.common.exception.BusinessException;
import com.smartedu.config.RabbitMQConfig;
import com.smartedu.entity.*;
import com.smartedu.mapper.*;
import com.smartedu.model.dto.ExamSubmitRequest;
import com.smartedu.model.vo.ExamResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 考试服务：考试管理 + 交卷 + 客观题自动评分
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamExamMapper examMapper;
    private final ExamQuestionMapper questionMapper;
    private final ExamPaperQuestionMapper paperQuestionMapper;
    private final ExamRecordMapper recordMapper;
    private final AiScoringService aiScoringService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 考试列表（所有考试）
     */
    public List<ExamExam> listExams(Long userId) {
        return examMapper.selectByStudentId(userId);
    }

    /**
     * 考试详情（含试题，不含答案）
     */
    public ExamExam getExamDetail(Long examId) {
        ExamExam exam = examMapper.selectById(examId);
        if (exam == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);
        return exam;
    }

    /**
     * 获取试题（不含答案 — 用于考试作答）
     */
    public List<ExamQuestion> getExamQuestions(Long examId) {
        return questionMapper.selectByExamIdWithoutAnswer(examId);
    }

    /**
     * 开始考试
     */
    @Transactional
    public ExamResultVO startExam(Long examId, Long studentId) {
        ExamExam exam = examMapper.selectById(examId);
        if (exam == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) throw new BusinessException(BizError.EXAM_NOT_STARTED);
        if (now.isAfter(exam.getEndTime())) throw new BusinessException(BizError.EXAM_ENDED);

        // 查找或创建考试记录
        ExamRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStudentId, studentId));

        if (record == null) {
            record = new ExamRecord();
            record.setExamId(examId);
            record.setStudentId(studentId);
            record.setStartTime(now);
            record.setStatus(1);
            record.setObjectiveScore(BigDecimal.ZERO);
            record.setTotalScore(BigDecimal.ZERO);
            record.setCreateTime(now);
            record.setUpdateTime(now);
            recordMapper.insert(record);
        } else if (record.getStatus() == 0) {
            record.setStartTime(now);
            record.setStatus(1);
            recordMapper.updateById(record);
        } else if (record.getStatus() == 2) {
            throw new BusinessException(BizError.ALREADY_SUBMITTED);
        }

        // 获取试题（不含答案）
        List<ExamQuestion> questions = questionMapper.selectByExamIdWithoutAnswer(examId);

        return ExamResultVO.builder()
                .recordId(record.getId())
                .examId(examId)
                .examName(exam.getExamName())
                .totalScore(exam.getTotalScore())
                .durationMinutes(exam.getDurationMinutes())
                .status(1)
                .questions(questions)
                .build();
    }

    /**
     * 交卷 — 客观题自动评分 + 填空题异步AI评分
     */
    @Transactional
    public ExamResultVO submitExam(Long examId, Long studentId, List<ExamSubmitRequest.AnswerItem> answers) {
        ExamExam exam = examMapper.selectById(examId);
        if (exam == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);

        ExamRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStudentId, studentId));
        if (record == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);
        if (record.getStatus() == 2) throw new BusinessException(BizError.ALREADY_SUBMITTED);

        // 获取试题（含答案——用于评分）
        List<ExamQuestion> questions = questionMapper.selectByExamIdWithAnswer(examId);
        // 获取试题分值表
        List<ExamPaperQuestion> paperQuestions = paperQuestionMapper.selectByExamId(examId);
        Map<Long, Integer> questionScoreMap = new HashMap<>();
        for (ExamPaperQuestion pq : paperQuestions) {
            questionScoreMap.put(pq.getQuestionId(), pq.getScore() != null ? pq.getScore() : 0);
        }
        // 均分值（兜底）
        int defaultScore = questions.isEmpty() ? 1 : Math.max(1, exam.getTotalScore() / questions.size());

        // 客观题自动评分（按 paper_question 实际分值）
        BigDecimal objectiveScore = BigDecimal.ZERO;
        for (ExamQuestion q : questions) {
            if (q.getQuestionType() == 4) continue; // 填空题跳过，异步AI评分
            Optional<ExamSubmitRequest.AnswerItem> ans = answers.stream()
                    .filter(a -> a.getQuestionId().equals(q.getId())).findFirst();
            if (ans.isPresent() && q.getAnswer() != null
                    && q.getAnswer().trim().equalsIgnoreCase(ans.get().getAnswer().trim())) {
                // 使用试卷关联的 score，若无则用均分
                int qScore = questionScoreMap.getOrDefault(q.getId(), defaultScore);
                objectiveScore = objectiveScore.add(new BigDecimal(qScore));
            }
        }

        try {
            record.setAnswers(objectMapper.writeValueAsString(answers));
        } catch (Exception e) {
            record.setAnswers("[]");
        }
        record.setObjectiveScore(objectiveScore);
        record.setTotalScore(objectiveScore); // 暂设为客观题分数，异步补充主观题
        record.setSubmitTime(LocalDateTime.now());
        record.setStatus(2);
        recordMapper.updateById(record);

        // 异步处理：AI评分填空题 + 计算总分
        Map<String, Object> mqPayload = new HashMap<>();
        mqPayload.put("examId", examId);
        mqPayload.put("studentId", studentId);
        mqPayload.put("recordId", record.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXAM_EXCHANGE,
                RabbitMQConfig.EXAM_SUBMIT_KEY, mqPayload);

        return ExamResultVO.builder()
                .recordId(record.getId())
                .examId(examId)
                .examName(exam.getExamName())
                .totalScore(exam.getTotalScore())
                .objectiveScore(objectiveScore)
                .finalScore(objectiveScore)
                .status(2)
                .build();
    }

    /**
     * 查看考试结果
     */
    public ExamResultVO getMyResult(Long examId, Long studentId) {
        ExamRecord record = recordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStudentId, studentId));
        if (record == null) throw new BusinessException(BizError.EXAM_NOT_FOUND);

        ExamExam exam = examMapper.selectById(examId);

        // 解析 aiFeedback：向后兼容旧版纯文本和新版 JSON 结构
        String aiFeedbackText = "";
        String aiReasonJson = "[]";
        String rawAiFeedback = record.getAiFeedback();
        if (rawAiFeedback != null && !rawAiFeedback.isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(rawAiFeedback);
                if (node.has("summary") && node.has("details")) {
                    // 新版结构化数据
                    aiFeedbackText = node.get("summary").asText();
                    aiReasonJson = node.get("details").toString();
                } else {
                    // 旧版纯文本（向后兼容）
                    aiFeedbackText = rawAiFeedback;
                }
            } catch (Exception e) {
                // 不是有效 JSON → 当作旧版纯文本
                aiFeedbackText = rawAiFeedback;
            }
        }

        return ExamResultVO.builder()
                .recordId(record.getId())
                .examId(examId)
                .examName(exam != null ? exam.getExamName() : "")
                .totalScore(exam != null ? exam.getTotalScore() : 0)
                .objectiveScore(record.getObjectiveScore())
                .finalScore(record.getTotalScore())
                .aiFeedback(aiFeedbackText)
                .aiReason(aiReasonJson)
                .status(record.getStatus())
                .submitTime(record.getSubmitTime())
                .build();
    }
}
