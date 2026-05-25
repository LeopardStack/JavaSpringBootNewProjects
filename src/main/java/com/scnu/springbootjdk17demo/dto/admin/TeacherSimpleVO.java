package com.scnu.springbootjdk17demo.dto.admin;
import lombok.Data;
@Data
public class TeacherSimpleVO {
    private Long id;            // sys_teacher.id
    private Long userId;
    private String teacherNo;
    private String name;        // nickname
    private String title;
    private String collegeName;
}