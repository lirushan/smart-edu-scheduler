package com.smartedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 成绩批量导入行 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreImportDTO {

    private Long studentId;
    private String studentName;
    private BigDecimal rawScore;
}
