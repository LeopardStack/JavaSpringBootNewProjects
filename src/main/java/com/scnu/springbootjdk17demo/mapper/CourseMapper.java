package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scnu.springbootjdk17demo.dto.admin.CourseListItemVO;
import com.scnu.springbootjdk17demo.dto.admin.CourseListQuery;
import com.scnu.springbootjdk17demo.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    @Select("""
        <script>
        SELECT c.id, c.code, c.name, c.course_type, c.credit, c.total_hours, c.status,
               c.created_at,
               s.id AS semester_id, s.name AS semester_name,
               t.id AS teacher_id, u.nickname AS teacher_name,
               (SELECT count(*) FROM course_class cc WHERE cc.course_id = c.id) AS class_count,
               (SELECT count(*) FROM course_student cs WHERE cs.course_id = c.id) AS student_count
        FROM course c
        JOIN sys_semester s ON s.id = c.semester_id
        JOIN sys_teacher t ON t.id = c.teacher_id
        JOIN sys_user u ON u.id = t.user_id
        <where>
            <if test="q.semesterId != null"> AND c.semester_id = #{q.semesterId} </if>
            <if test="q.teacherId != null">  AND c.teacher_id = #{q.teacherId}  </if>
            <if test="q.courseType != null and q.courseType != ''"> AND c.course_type = #{q.courseType} </if>
            <if test="q.status != null">     AND c.status = #{q.status}         </if>
            <if test="q.keyword != null and q.keyword != ''">
                AND (c.name ILIKE '%' || #{q.keyword} || '%' OR c.code ILIKE '%' || #{q.keyword} || '%')
            </if>
        </where>
        ORDER BY c.created_at DESC
        </script>
    """)
    IPage<CourseListItemVO> selectListVO(IPage<CourseListItemVO> page, @Param("q") CourseListQuery q);
}