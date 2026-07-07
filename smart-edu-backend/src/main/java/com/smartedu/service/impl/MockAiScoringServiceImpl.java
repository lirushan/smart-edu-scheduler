package com.smartedu.service.impl;

import com.smartedu.service.AiScoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mock AI 评分实现 — 精确匹配 + 关键词模糊匹配（忽略空格/大小写）
 * V2.0 可替换为真实大模型 API
 */
@Slf4j
@Service
public class MockAiScoringServiceImpl implements AiScoringService {

    @Override
    public AiScoringResult scoreFillBlank(String questionContent, String referenceAnswer,
                                          String studentAnswer, BigDecimal maxScore) {
        if (studentAnswer == null || studentAnswer.isBlank()) {
            return new AiScoringResult(BigDecimal.ZERO,
                    "未作答", "学生未填写答案");
        }

        String normalizedStudent = normalize(studentAnswer);
        String normalizedRef = normalize(referenceAnswer);

        // 1. 精确匹配
        if (normalizedStudent.equals(normalizedRef)) {
            return new AiScoringResult(maxScore,
                    "回答正确", "答案与参考答案完全一致");
        }

        // 2. 多答案匹配（参考答案可能包含多个可接受的答案，用逗号分隔）
        String[] refParts = normalizedRef.split(",");
        for (String part : refParts) {
            if (normalizedStudent.equals(part.trim())) {
                return new AiScoringResult(maxScore,
                        "回答正确", "答案匹配参考答案中的一个可接受答案");
            }
        }

        // 3. 关键词模糊匹配 — 提取关键词并计算匹配度
        Set<String> refKeywords = extractKeywords(normalizedRef);
        Set<String> studentKeywords = extractKeywords(normalizedStudent);

        if (refKeywords.isEmpty()) {
            return new AiScoringResult(BigDecimal.ZERO,
                    "回答错误", "关键词匹配失败");
        }

        long matchedCount = studentKeywords.stream().filter(refKeywords::contains).count();
        double matchRatio = (double) matchedCount / refKeywords.size();

        if (matchRatio >= 0.8) {
            BigDecimal score = maxScore.multiply(new BigDecimal("0.8")).setScale(1, RoundingMode.HALF_UP);
            return new AiScoringResult(score,
                    "部分正确（匹配度: " + (int)(matchRatio * 100) + "%）",
                    "关键词匹配: " + matchedCount + "/" + refKeywords.size());
        } else if (matchRatio >= 0.5) {
            BigDecimal score = maxScore.multiply(new BigDecimal("0.5")).setScale(1, RoundingMode.HALF_UP);
            return new AiScoringResult(score,
                    "部分正确（匹配度: " + (int)(matchRatio * 100) + "%）",
                    "关键词部分匹配: " + matchedCount + "/" + refKeywords.size());
        }

        return new AiScoringResult(BigDecimal.ZERO,
                "回答错误", "关键词匹配不足: " + matchedCount + "/" + refKeywords.size());
    }

    private String normalize(String s) {
        return s.replaceAll("\\s+", "").toLowerCase().replaceAll("[\\p{Punct}&&[^,]]", "");
    }

    private Set<String> extractKeywords(String s) {
        return Arrays.stream(s.split("[,，、]"))
                .map(String::trim)
                .filter(k -> !k.isEmpty() && k.length() >= 2)
                .collect(Collectors.toSet());
    }
}
