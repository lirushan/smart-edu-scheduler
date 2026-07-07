package com.smartedu.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 考试创建/更新 DTO
 */
@Data
public class ExamManageDTO {
    private Long offeringId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Integer totalScore;
}
