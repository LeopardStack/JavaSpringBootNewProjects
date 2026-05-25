package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.teacher.*;
import com.scnu.springbootjdk17demo.service.HomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/homework")
@RequiredArgsConstructor
@PreAuthorize("hasRole('teacher')")
public class HomeworkController {

    private final HomeworkService service;

    @GetMapping
    public List<HomeworkVO> list(@RequestParam Long courseId,
                                 @AuthenticationPrincipal UserInfoResponse user) {
        return service.listByCourse(courseId, user.getId());
    }

    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody HomeworkCreateRequest req,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        return Map.of("code", 200, "id", service.create(req, user.getId()));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id,
                                      @Valid @RequestBody HomeworkCreateRequest req,
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

    @GetMapping("/{id}/submissions")
    public List<SubmissionRowVO> submissions(@PathVariable Long id,
                                             @AuthenticationPrincipal UserInfoResponse user) {
        return service.listSubmissions(id, user.getId());
    }

    @PostMapping("/{id}/grade/{studentId}")
    public Map<String, Object> grade(@PathVariable Long id,
                                     @PathVariable Long studentId,
                                     @Valid @RequestBody GradeRequest req,
                                     @AuthenticationPrincipal UserInfoResponse user) {
        service.grade(id, studentId, req, user.getId());
        return Map.of("code", 200, "message", "已评分");
    }
}