package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.entity.CourseStudent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.Collection;
import java.util.List;

@Mapper
public interface CourseStudentMapper extends BaseMapper<CourseStudent> {

    /** 根据班级 ID 列表，找出所有学生 ID（用于课程创建时自动展开选课名单） */
    @Select("""
        <script>
        SELECT id FROM sys_student
        WHERE status = 1 AND class_id IN
        <foreach collection='classIds' item='cid' open='(' separator=',' close=')'>#{cid}</foreach>
        </script>
    """)
    List<Long> selectStudentIdsByClassIds(@Param("classIds") Collection<Long> classIds);
}