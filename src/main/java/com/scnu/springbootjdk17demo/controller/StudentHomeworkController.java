package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.student.*;
import com.scnu.springbootjdk17demo.service.StudentHomeworkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/homework")
@RequiredArgsConstructor
@PreAuthorize("hasRole('student')")
public class StudentHomeworkController {

    private final StudentHomeworkService service;

    /** 按课程列出作业 */
    @GetMapping
    public List<StudentHomeworkVO> list(@RequestParam(required = false) Long courseId,
                                        @AuthenticationPrincipal UserInfoResponse user) {
        return courseId == null
                ? service.listAll(user.getId())
                : service.listByCourse(courseId, user.getId());
    }

    /** 提交作业 */
    @PostMapping("/submit")
    public Map<String, Object> submit(@Valid @RequestBody SubmitHomeworkRequest req,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        Long sid = service.submit(req, user.getId());
        return Map.of("code", 200, "message", "提交成功", "submissionId", sid);
    }
}