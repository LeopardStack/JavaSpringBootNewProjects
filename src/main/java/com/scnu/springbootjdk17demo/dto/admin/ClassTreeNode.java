package com.scnu.springbootjdk17demo.dto.admin;

import lombok.Data;
import java.util.List;

@Data
public class ClassTreeNode {
    private String key;         // college:1 / major:2 / class:3
    private Long id;
    private String type;        // college / major / class
    private String label;
    private Integer studentCount; // 只有 class 层有意义
    private List<ClassTreeNode> children;
}