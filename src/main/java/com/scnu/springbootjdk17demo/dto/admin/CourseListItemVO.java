package com.scnu.springbootjdk17demo.dto.admin;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class CourseListItemVO {
    private Long id;
    private String code;
    private String name;
    private String courseType;
    private BigDecimal credit;
    private Integer totalHours;
    private Integer status;
    private OffsetDateTime createdAt;
    private Long semesterId;
    private String semesterName;
    private Long teacherId;
    private String teacherName;
    private Integer classCount;
    private Integer studentCount;
}