package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 考试定义实体
 */
@Data
@TableName("exam_exam")
public class ExamExam {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long offeringId;
    private String examName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;
    private Integer totalScore;
    private Integer status;       // 0=未开始 1=进行中 2=已结束

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String courseName;

    @TableField(exist = false)
    private Long recordId;

    @TableField(exist = false)
    private Integer recordStatus; // 0=未开始 1=进行中 2=已交卷

    @TableField(exist = false)
    private java.math.BigDecimal finalScore;
}
