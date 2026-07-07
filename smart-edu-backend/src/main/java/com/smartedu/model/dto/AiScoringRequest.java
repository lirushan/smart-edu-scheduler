package com.smartedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 评分请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiScoringRequest {

    private Long questionId;
    private String questionContent;
    private String referenceAnswer;
    private String studentAnswer;
    private java.math.BigDecimal maxScore;
}
