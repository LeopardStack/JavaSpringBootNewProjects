package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.PageResult;
import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.admin.*;
import com.scnu.springbootjdk17demo.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/course")
@RequiredArgsConstructor
public class AdminCourseController {

    private final CourseService courseService;

    /** 列表 */
    @GetMapping
    @PreAuthorize("hasAuthority('admin:course:list') or hasRole('admin')")
    public PageResult<CourseListItemVO> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            CourseListQuery query) {
        return courseService.list(current, size, query);
    }

    /** 详情 */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public CourseDetailVO detail(@PathVariable Long id) {
        return courseService.detail(id);
    }

    /** 创建 */
    @PostMapping
    @PreAuthorize("hasAuthority('admin:course:create') or hasRole('admin')")
    public Map<String, Object> create(@Valid @RequestBody CourseCreateRequest req,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        Long id = courseService.create(req, user.getId());
        return Map.of("code", 200, "message", "创建成功", "id", id);
    }

    /** 更新 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:course:edit') or hasRole('admin')")
    public Map<String, Object> update(@PathVariable Long id,
                                      @Valid @RequestBody CourseUpdateRequest req) {
        courseService.update(id, req);
        return Map.of("code", 200, "message", "更新成功");
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:course:delete') or hasRole('admin')")
    public Map<String, Object> delete(@PathVariable Long id) {
        courseService.delete(id);
        return Map.of("code", 200, "message", "删除成功");
    }
}