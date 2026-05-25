package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_major")
public class SysMajor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long collegeId;
    private String code;
    private String name;
    private String degreeType;
    private Integer durationYears;
    private Integer sort;
    private OffsetDateTime createdAt;
}