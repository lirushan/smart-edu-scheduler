package com.smartedu.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 教学评价统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationStatsVO {

    private Long teacherId;
    private String teacherName;
    private String courseName;
    private Long evalCount;
    private Double avgScore;
}
