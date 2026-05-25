package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TeacherCourseListItemVO {
    private Long id;
    private String code;
    private String name;
    private String semesterName;
    private String courseType;
    private BigDecimal credit;
    private Integer totalHours;
    private Integer status;
    private Integer classCount;
    private Integer studentCount;
    private Integer announcementCount;
    private Integer homeworkCount;
    private Integer pendingGradeCount;     // 待批改作业数
}