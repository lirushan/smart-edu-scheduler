package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartedu.entity.ExamExam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamExamMapper extends BaseMapper<ExamExam> {

    @Select("""
        SELECT e.*, c.course_name
        FROM exam_exam e
        JOIN crs_offering o ON e.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        WHERE e.deleted = 0
        ORDER BY e.start_time DESC
    """)
    List<ExamExam> selectAllWithCourse();

    @Select("""
        SELECT e.*, c.course_name
        FROM exam_exam e
        JOIN crs_offering o ON e.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        JOIN reg_enrollment en ON o.id = en.offering_id
        WHERE en.student_id = #{studentId} AND en.status = 0 AND e.deleted = 0
        ORDER BY e.start_time DESC
    """)
    List<ExamExam> selectByStudentId(@Param("studentId") Long studentId);
}
