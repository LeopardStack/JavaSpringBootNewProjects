package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data @TableName("sys_permission")
public class SysPermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Short  type;          // 1=菜单 2=按钮
    private String parentCode;
    private String path;
    private String icon;
    private Integer sort;
}