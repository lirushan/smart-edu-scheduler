package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成绩实体
 */
@Data
@TableName("reg_score")
public class RegScore {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long offeringId;
    private BigDecimal rawScore;
    private String gradeLevel;     // 五级制
    private BigDecimal gpa;
    private Integer rankInClass;
    private Integer status;        // 0=草稿 1=已发布
    private Long enteredBy;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非DB字段
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private BigDecimal credit;
}
