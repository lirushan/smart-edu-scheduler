package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 培养方案实体
 */
@Data
@TableName("sys_training_plan")
public class SysTrainingPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String major;
    private String grade;
    private BigDecimal totalCredits;
    private BigDecimal requiredCredits;
    private BigDecimal electiveCredits;
    private String description;
    private Integer status; // 0=停用 1=启用

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
