package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_college")
public class SysCollege {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer sort;
    private OffsetDateTime createdAt;
}