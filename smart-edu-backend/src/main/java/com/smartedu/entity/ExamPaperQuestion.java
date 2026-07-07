package com.smartedu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 试卷-试题关联实体
 */
@Data
@TableName("exam_paper_question")
public class ExamPaperQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paperId;
    private Long questionId;
    private Integer questionOrder;
    private Integer score;
}
