package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 试题实体
 */
@Data
@TableName("exam_question")
public class ExamQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer questionType;   // 1=单选 2=多选 3=判断 4=填空
    private String content;
    private String options;         // JSON string
    private String answer;
    private String analysis;
    private Integer difficulty;     // 1-5
    private String knowledgePoint;
    private Long createdBy;
    private Integer scope;          // 1=全局 2=个人
    private Integer auditStatus;    // 0=待审 1=通过 2=驳回
    private Long auditorId;
    private LocalDateTime auditTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String creatorName;
}
