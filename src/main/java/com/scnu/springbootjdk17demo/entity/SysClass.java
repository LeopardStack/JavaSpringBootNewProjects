package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_class")
public class SysClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long majorId;
    private String code;
    private String name;
    private Integer gradeYear;
    private Long headteacherId;
    private OffsetDateTime createdAt;
}