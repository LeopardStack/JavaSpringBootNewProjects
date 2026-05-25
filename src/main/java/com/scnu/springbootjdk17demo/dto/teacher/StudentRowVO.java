package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class StudentRowVO {
    private Long studentId;
    private String studentNo;
    private String name;
    private Integer gender;
    private String className;
    private String phone;
    private Integer submissionCount;
    private BigDecimal avgScore;
}