package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@TableName("sys_semester")
public class SysSemester {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer isCurrent;
    private OffsetDateTime createdAt;
}