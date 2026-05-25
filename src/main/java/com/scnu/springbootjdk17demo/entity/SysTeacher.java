package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("sys_teacher")
public class SysTeacher {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String teacherNo;
    private Long collegeId;
    private String title;
    private Integer gender;
    private String phone;
    private String intro;
    private String avatarUrl;
    private Integer status;
    private OffsetDateTime createdAt;
}