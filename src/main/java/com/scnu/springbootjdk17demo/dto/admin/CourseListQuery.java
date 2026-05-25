package com.scnu.springbootjdk17demo.dto.admin;

import lombok.Data;

@Data
public class CourseListQuery {
    private Long semesterId;
    private Long teacherId;
    private String courseType;
    private Integer status;
    private String keyword;
}