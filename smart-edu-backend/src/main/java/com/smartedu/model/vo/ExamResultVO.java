package com.smartedu.model.vo;

import com.smartedu.entity.ExamQuestion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试结果 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultVO {

    private Long recordId;
    private Long examId;
    private String examName;
    private Integer totalScore;
    private Integer durationMinutes;
    private BigDecimal objectiveScore;
    private BigDecimal finalScore;
    private String aiFeedback;
    private Integer status;          // 0=未开始 1=进行中 2=已交卷
    private LocalDateTime submitTime;
    private List<ExamQuestion> questions; // 考试作答页试题（不含答案）
}
