package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("course")
public class Course {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Long semesterId;
    private Long teacherId;
    private String courseType;     // live/recorded/offline/hybrid
    private BigDecimal credit;
    private Integer totalHours;
    private String coverUrl;
    private String description;
    private Integer status;        // 0=草稿 1=进行中 2=已结课
    private Long createdBy;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private OffsetDateTime updatedAt;
}