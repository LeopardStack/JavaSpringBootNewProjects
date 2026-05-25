package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("course_schedule")
public class CourseSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Integer weekday;       // 1-7
    private Integer periodStart;
    private Integer periodEnd;
    private Integer weekStart;
    private Integer weekEnd;
    private String location;
    private String roomUrl;
    private OffsetDateTime createdAt;
}