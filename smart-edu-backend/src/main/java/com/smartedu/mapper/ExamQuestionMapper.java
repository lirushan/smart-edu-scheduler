package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartedu.entity.ExamQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamQuestionMapper extends BaseMapper<ExamQuestion> {

    /**
     * 按试卷查询试题（不含答案——用于考试作答页）
     */
    @Select("""
        SELECT q.id, q.question_type, q.content, q.options, q.difficulty,
               q.knowledge_point, pq.question_order, pq.score
        FROM exam_question q
        JOIN exam_paper_question pq ON q.id = pq.question_id
        WHERE pq.paper_id = (SELECT id FROM exam_paper WHERE exam_id = #{examId} LIMIT 1)
          AND q.deleted = 0
        ORDER BY pq.question_order
    """)
    List<ExamQuestion> selectByExamIdWithoutAnswer(@Param("examId") Long examId);

    /**
     * 按试卷查询试题（含答案——用于评分）
     */
    @Select("""
        SELECT q.*, pq.score
        FROM exam_question q
        JOIN exam_paper_question pq ON q.id = pq.question_id
        WHERE pq.paper_id = (SELECT id FROM exam_paper WHERE exam_id = #{examId} LIMIT 1)
          AND q.deleted = 0
        ORDER BY pq.question_order
    """)
    List<ExamQuestion> selectByExamIdWithAnswer(@Param("examId") Long examId);
}
