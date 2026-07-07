package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartedu.entity.RegScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RegScoreMapper extends BaseMapper<RegScore> {

    @Select("""
        SELECT s.*, u.real_name AS student_name, c.course_name, c.credit
        FROM reg_score s
        JOIN sys_user u ON s.student_id = u.id
        JOIN crs_offering o ON s.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        WHERE s.student_id = #{studentId} AND s.deleted = 0
        ORDER BY s.create_time DESC
    """)
    List<RegScore> selectByStudentId(@Param("studentId") Long studentId);

    @Select("""
        SELECT s.*, u.real_name AS student_name, c.course_name
        FROM reg_score s
        JOIN sys_user u ON s.student_id = u.id
        JOIN crs_offering o ON s.offering_id = o.id
        JOIN crs_course c ON o.course_id = c.id
        WHERE s.offering_id = #{offeringId} AND s.deleted = 0
        ORDER BY s.raw_score DESC
    """)
    List<RegScore> selectByOfferingId(@Param("offeringId") Long offeringId);
}
