package com.smartedu.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程查询参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseOfferingQuery {

    private String keyword;
    private String category;
    private Integer page = 1;
    private Integer size = 12;
}
