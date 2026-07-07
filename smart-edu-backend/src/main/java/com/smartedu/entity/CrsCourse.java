package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程库实体
 */
@Data
@TableName("crs_course")
public class CrsCourse {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String courseCode;
    private String courseName;
    private java.math.BigDecimal credit;
    private String description;
    private String category;
    private String syllabus;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
