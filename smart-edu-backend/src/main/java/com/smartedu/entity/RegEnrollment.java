package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课记录实体
 */
@Data
@TableName("reg_enrollment")
public class RegEnrollment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long offeringId;
    private Long roundId;
    private Integer status;       // 0=正常 1=已退 2=待审核
    private LocalDateTime createdAt;
    private LocalDateTime droppedAt;

    @TableLogic
    private Integer deleted;

    // 非数据库字段 — 关联查询
    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String location;

    @TableField(exist = false)
    private Integer weekday;

    @TableField(exist = false)
    private Integer periodStart;

    @TableField(exist = false)
    private Integer periodEnd;

    @TableField(exist = false)
    private java.math.BigDecimal credit;
}
