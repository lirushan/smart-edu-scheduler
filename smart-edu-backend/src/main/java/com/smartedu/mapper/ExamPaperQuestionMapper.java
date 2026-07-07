package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartedu.entity.ExamPaperQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamPaperQuestionMapper extends BaseMapper<ExamPaperQuestion> {

    /**
     * 查询某次考试的所有试题分值
     */
    @Select("""
        SELECT epq.* FROM exam_paper_question epq
        JOIN exam_paper ep ON epq.paper_id = ep.id
        WHERE ep.exam_id = #{examId}
        ORDER BY epq.question_order
    """)
    List<ExamPaperQuestion> selectByExamId(@Param("examId") Long examId);
}
