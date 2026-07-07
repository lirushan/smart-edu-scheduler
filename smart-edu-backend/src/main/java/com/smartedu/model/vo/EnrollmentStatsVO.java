package com.smartedu.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 选课统计 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentStatsVO {

    private Long totalOfferings;
    private Long totalEnrollments;
    private BigDecimal capacityUsageRate; // 容量使用率 0-1
    private Integer totalCapacity;
    private Integer totalEnrolled;
}
