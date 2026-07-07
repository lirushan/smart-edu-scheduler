package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教学评价实体
 */
@Data
@TableName("sys_evaluation")
public class SysEvaluation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;
    private Long teacherId;
    private Long offeringId;
    private Integer score1;
    private Integer score2;
    private Integer score3;
    private Integer score4;
    private Integer score5;
    private String comment;
    private Integer status; // 0=草稿 1=已提交

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 非数据库字段
    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String teacherName;

    @TableField(exist = false)
    private String courseName;

    /**
     * 计算平均分
     */
    public double getAvgScore() {
        return (score1 + score2 + score3 + score4 + score5) / 5.0;
    }
}
