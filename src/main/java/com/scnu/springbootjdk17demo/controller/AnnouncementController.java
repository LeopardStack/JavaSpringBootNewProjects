package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.teacher.*;
import com.scnu.springbootjdk17demo.service.AnnouncementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/announcement")
@RequiredArgsConstructor
@PreAuthorize("hasRole('teacher')")
public class AnnouncementController {

    private final AnnouncementService service;

    @GetMapping
    public List<AnnouncementVO> list(@RequestParam Long courseId,
                                     @AuthenticationPrincipal UserInfoResponse user) {
        return service.listByCourse(courseId, user.getId());
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody AnnouncementCreateRequest req,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        return Map.of("code", 200, "id", service.create(req, user.getId()));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id,
                                      @Valid @RequestBody AnnouncementCreateRequest req,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        service.update(id, req, user.getId());
        return Map.of("code", 200, "message", "更新成功");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        service.delete(id, user.getId());
        return Map.of("code", 200, "message", "删除成功");
    }
}