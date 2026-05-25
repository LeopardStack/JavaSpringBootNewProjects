package com.scnu.springbootjdk17demo.dto.teacher;

import com.scnu.springbootjdk17demo.entity.CourseSchedule;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherCourseDetailVO {
    private Long id;
    private String code;
    private String name;
    private String semesterName;
    private String courseType;
    private BigDecimal credit;
    private Integer totalHours;
    private Integer status;
    private String description;
    private Integer studentCount;
    private Integer classCount;
    private List<CourseSchedule> schedules;
    private List<ClassInfo> classes;

    @Data
    public static class ClassInfo {
        private Long id;
        private String code;
        private String name;
        private Integer studentCount;
    }
}