package com.smartedu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartedu.entity.CrsOffering;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CrsOfferingMapper extends BaseMapper<CrsOffering> {

    /**
     * 分页搜索课程（关联课程名+教师名）
     */
    @Select("""
        <script>
        SELECT o.*, c.course_name, c.credit, u.real_name AS teacher_name
        FROM crs_offering o
        JOIN crs_course c ON o.course_id = c.id
        JOIN sys_user u ON o.teacher_id = u.id
        WHERE o.deleted = 0 AND o.status = 1
        <if test='keyword != null and keyword != \"\"'>
          AND (c.course_name LIKE CONCAT('%', #{keyword}, '%')
               OR u.real_name LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='category != null and category != \"\"'>
          AND c.category = #{category}
        </if>
        ORDER BY o.create_time DESC
        </script>
    """)
    IPage<CrsOffering> searchOfferings(Page<CrsOffering> page, @Param("keyword") String keyword, @Param("category") String category);

    /**
     * 按教师查询授课列表
     */
    @Select("""
        SELECT o.*, c.course_name, c.credit
        FROM crs_offering o
        JOIN crs_course c ON o.course_id = c.id
        WHERE o.teacher_id = #{teacherId} AND o.deleted = 0
        ORDER BY o.weekday, o.period_start
    """)
    java.util.List<CrsOffering> selectByTeacherId(@Param("teacherId") Long teacherId);

    /**
     * 按课程ID查询（含教师名）
     */
    @Select("""
        SELECT o.*, c.course_name, c.credit, u.real_name AS teacher_name
        FROM crs_offering o
        JOIN crs_course c ON o.course_id = c.id
        JOIN sys_user u ON o.teacher_id = u.id
        WHERE o.id = #{id} AND o.deleted = 0
    """)
    CrsOffering selectDetailById(@Param("id") Long id);
}
