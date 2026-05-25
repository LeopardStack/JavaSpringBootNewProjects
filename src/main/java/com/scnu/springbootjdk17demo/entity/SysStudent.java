package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_student")
public class SysStudent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String studentNo;
    private Long classId;
    private Integer gender;
    private Integer enrollmentYear;
    private String phone;
    private String avatarUrl;
    private Integer status;
    private OffsetDateTime createdAt;
}