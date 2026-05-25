package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("course_student")
public class CourseStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long studentId;
    private Integer status;
    private BigDecimal finalScore;
    private OffsetDateTime enrolledAt;
}