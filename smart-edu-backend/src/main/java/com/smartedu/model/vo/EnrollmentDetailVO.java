package com.smartedu.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 选课详情 VO（教务端选课监控）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDetailVO {

    private Long offeringId;
    private String courseName;
    private String teacherName;
    private String semester;
    private Integer capacity;
    private Integer enrolledCount;
    private BigDecimal fillRate; // 0-1
}
