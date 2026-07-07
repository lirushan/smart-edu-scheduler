package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.entity.RegEnrollment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegEnrollmentMapper extends BaseMapper<RegEnrollment> {

    /**
     * 按学生查询选课（含课程+教师信息）
     */
    @Select("""
        SELECT e.*, c.course_name, c.credit, u.real_name AS teacher_name,
               o.location, o.weekday, o.period_start, o.period_end
        FROM reg_enrollment e
        JOIN crs_offering o ON e.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        JOIN sys_user u ON o.teacher_id = u.id
        WHERE e.student_id = #{studentId} AND e.status = 0 AND e.deleted = 0
        ORDER BY e.created_at DESC
    """)
    List<RegEnrollment> selectByStudentId(@Param("studentId") Long studentId);

    /**
     * 统计学生已选门数
     */
    @Select("""
        SELECT COUNT(*) FROM reg_enrollment
        WHERE student_id = #{studentId} AND status = 0 AND deleted = 0
    """)
    int countByStudentId(@Param("studentId") Long studentId);

    /**
     * 统计学生已选学分
     */
    @Select("""
        SELECT COALESCE(SUM(c.credit), 0) FROM reg_enrollment e
        JOIN crs_offering o ON e.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        WHERE e.student_id = #{studentId} AND e.status = 0 AND e.deleted = 0
    """)
    java.math.BigDecimal sumCreditsByStudentId(@Param("studentId") Long studentId);
}
