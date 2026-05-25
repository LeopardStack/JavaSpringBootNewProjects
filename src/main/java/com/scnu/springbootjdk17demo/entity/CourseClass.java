package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("course_class")
public class CourseClass {
    private Long courseId;
    private Long classId;
}