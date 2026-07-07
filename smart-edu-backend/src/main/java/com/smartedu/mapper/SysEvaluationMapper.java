package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartedu.entity.SysEvaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SysEvaluationMapper extends BaseMapper<SysEvaluation> {

    /**
     * 按教师统计评价（均分+人数+课程名）
     */
    @Select("""
        SELECT e.teacher_id AS teacherId, u.real_name AS teacherName,
               COUNT(*) AS evalCount,
               AVG((e.score_1 + e.score_2 + e.score_3 + e.score_4 + e.score_5) / 5.0) AS avgScore
        FROM sys_evaluation e
        JOIN sys_user u ON e.teacher_id = u.id
        WHERE e.status = 1 AND e.deleted = 0
        GROUP BY e.teacher_id, u.real_name
        ORDER BY avgScore DESC
    """)
    List<Map<String, Object>> selectTeacherStats();

    /**
     * 获取某教师的评价详情列表
     */
    @Select("""
        SELECT e.*, u.real_name AS student_name, t.real_name AS teacher_name, c.course_name
        FROM sys_evaluation e
        JOIN sys_user u ON e.student_id = u.id
        JOIN sys_user t ON e.teacher_id = t.id
        JOIN crs_offering o ON e.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        WHERE e.teacher_id = #{teacherId} AND e.status = 1 AND e.deleted = 0
        ORDER BY e.create_time DESC
    """)
    List<SysEvaluation> selectByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 获取所有已提交评价（含关联信息）
     */
    @Select("""
        SELECT e.*, u.real_name AS student_name, t.real_name AS teacher_name, c.course_name
        FROM sys_evaluation e
        JOIN sys_user u ON e.student_id = u.id
        JOIN sys_user t ON e.teacher_id = t.id
        JOIN crs_offering o ON e.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        WHERE e.status = 1 AND e.deleted = 0
        ORDER BY e.create_time DESC
    """)
    List<SysEvaluation> selectAllWithDetails();
}
