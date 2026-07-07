package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 选课轮次实体
 */
@Data
@TableName("reg_round")
public class RegRound {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roundName;
    private String semester;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxCredits;
    private Integer maxCourses;
    private String targetGrades;   // JSON string: ["2024级","2023级"]
    private Integer ageMin;
    private Integer ageMax;
    private Integer status;         // 0=未开始 1=进行中 2=已结束

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
