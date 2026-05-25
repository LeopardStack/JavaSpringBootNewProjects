package com.scnu.springbootjdk17demo.dto.admin;

import com.scnu.springbootjdk17demo.entity.CourseSchedule;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseDetailVO {
    private Long id;
    private String code;
    private String name;
    private Long semesterId;
    private String semesterName;
    private Long teacherId;
    private String teacherName;
    private String courseType;
    private BigDecimal credit;
    private Integer totalHours;
    private String coverUrl;
    private String description;
    private Integer status;
    private List<Long> classIds;
    private List<CourseSchedule> schedules;
    private Integer studentCount;
}