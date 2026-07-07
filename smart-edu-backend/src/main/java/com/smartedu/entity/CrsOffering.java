package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 开课实例实体
 */
@Data
@TableName("crs_offering")
public class CrsOffering {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;
    private Long teacherId;
    private String semester;
    private Integer weekday;       // 1-7
    private Integer periodStart;   // 1-8
    private Integer periodEnd;
    private String location;
    private Integer capacity;
    private Integer enrolledCount;
    private Integer status;        // 0=待审 1=通过 2=驳回
    private String auditComment;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段 — 关联查询
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private java.math.BigDecimal credit;
}
