package com.smartedu.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 课表视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleVO {

    private Long offeringId;
    private String courseName;
    private String teacherName;
    private String location;
    private Integer weekday;
    private Integer periodStart;
    private Integer periodEnd;
    private BigDecimal credit;
}
