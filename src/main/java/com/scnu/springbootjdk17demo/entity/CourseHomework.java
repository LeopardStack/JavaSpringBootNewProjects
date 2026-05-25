package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("course_homework")
public class CourseHomework {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private OffsetDateTime deadline;
    private BigDecimal maxScore;
    private Integer allowLate;
    private Long publisherId;
    private OffsetDateTime publishedAt;
    private Integer status;            // 0=草稿 1=已发布 2=已截止
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}