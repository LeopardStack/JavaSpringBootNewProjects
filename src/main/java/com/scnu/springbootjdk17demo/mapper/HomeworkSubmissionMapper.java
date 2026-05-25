package com.scnu.springbootjdk17demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scnu.springbootjdk17demo.dto.teacher.SubmissionRowVO;
import com.scnu.springbootjdk17demo.entity.HomeworkSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeworkSubmissionMapper extends BaseMapper<HomeworkSubmission> {

    @Select("""
        SELECT s.id AS student_id, s.student_no, u.nickname AS student_name,
               cls.name AS class_name,
               hs.id AS submission_id, hs.score, hs.feedback,
               hs.submitted_at, hs.graded_at, hs.is_late
        FROM course_student cs
        JOIN sys_student s ON s.id = cs.student_id
        JOIN sys_user u    ON u.id = s.user_id
        JOIN sys_class cls ON cls.id = s.class_id
        LEFT JOIN homework_submission hs
               ON hs.homework_id = #{homeworkId} AND hs.student_id = s.id
        WHERE cs.course_id = #{courseId}
        ORDER BY s.student_no
    """)
    List<SubmissionRowVO> selectSubmissionRows(
            @Param("homeworkId") Long homeworkId,
            @Param("courseId")   Long courseId);
}
