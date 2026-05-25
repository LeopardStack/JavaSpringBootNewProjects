package com.scnu.springbootjdk17demo.dto.admin;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CourseUpdateRequest extends CourseCreateRequest {
    // 继承 Create 所有字段；ID 走 path variable
}