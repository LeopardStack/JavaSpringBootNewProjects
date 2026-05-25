package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("course_announcement")
public class CourseAnnouncement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private Integer priority;          // 0=普通 1=重要
    private Long publisherId;
    private OffsetDateTime publishedAt;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}