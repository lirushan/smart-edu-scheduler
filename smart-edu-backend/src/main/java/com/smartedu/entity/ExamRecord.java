package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 考试记录实体
 */
@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;
    private Long studentId;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private String answers;           // JSON string
    private BigDecimal objectiveScore;
    private BigDecimal totalScore;
    private String aiFeedback;        // JSON string
    private Integer status;           // 0=未开始 1=进行中 2=已交卷

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
