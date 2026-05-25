package com.scnu.springbootjdk17demo.dto.student;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentScoreVO {
    private Long courseId;
    private String courseCode;
    private String courseName;
    private String semesterName;
    private String teacherName;
    private BigDecimal credit;
    private BigDecimal finalScore;            // 最终成绩，可能为 null
    private BigDecimal avgHomeworkScore;      // 作业平均分
    private Integer homeworkCount;
    private Integer submittedCount;
}