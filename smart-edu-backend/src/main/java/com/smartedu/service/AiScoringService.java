package com.smartedu.service;

import java.math.BigDecimal;

/**
 * AI 评分服务接口 — 策略模式
 * V1.0: Mock 实现（精确匹配+关键词模糊）
 * V2.0: 可替换为真实大模型 API
 */
public interface AiScoringService {

    /**
     * 对填空题进行评分
     *
     * @param questionContent 题目内容
     * @param referenceAnswer 参考答案
     * @param studentAnswer   学生答案
     * @param maxScore        满分
     * @return {score, feedback, reasoning}
     */
    AiScoringResult scoreFillBlank(String questionContent, String referenceAnswer,
                                   String studentAnswer, BigDecimal maxScore);

    record AiScoringResult(BigDecimal score, String feedback, String reasoning) {}
}
