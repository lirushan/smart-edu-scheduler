package com.smartedu.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 课程列表视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOfferingVO {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private BigDecimal credit;
    private String semester;
    private Integer weekday;
    private Integer periodStart;
    private Integer periodEnd;
    private String location;
    private Integer capacity;
    private Integer enrolledCount;
    private Integer status;
    private String auditComment;
}
