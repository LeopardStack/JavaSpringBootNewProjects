package com.scnu.springbootjdk17demo.dto.student;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentCourseListItemVO {
    private Long id;
    private String code;
    private String name;
    private String semesterName;
    private String courseType;
    private BigDecimal credit;
    private Integer totalHours;
    private String teacherName;
    private String teacherTitle;
    private Integer pendingHomeworkCount;   // 未交作业数
    private BigDecimal currentScore;        // 当前平均分（如果有）
}