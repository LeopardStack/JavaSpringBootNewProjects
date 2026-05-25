package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.teacher.*;
import com.scnu.springbootjdk17demo.service.TeacherCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/course")
@RequiredArgsConstructor
@PreAuthorize("hasRole('teacher')")
public class TeacherCourseController {

    private final TeacherCourseService service;

    @GetMapping
    public List<TeacherCourseListItemVO> listMine(@AuthenticationPrincipal UserInfoResponse user) {
        return service.listMyCourses(user.getId());
    }

    @GetMapping("/{id}")
    public TeacherCourseDetailVO detail(@PathVariable Long id,
                                        @AuthenticationPrincipal UserInfoResponse user) {
        return service.detail(id, user.getId());
    }

    @GetMapping("/{id}/students")
    public List<StudentRowVO> students(@PathVariable Long id,
                                       @AuthenticationPrincipal UserInfoResponse user) {
        return service.listStudents(id, user.getId());
    }
}