package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.student.*;
import com.scnu.springbootjdk17demo.dto.teacher.AnnouncementVO;
import com.scnu.springbootjdk17demo.service.StudentCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@PreAuthorize("hasRole('student')")
public class StudentCourseController {

    private final StudentCourseService service;

    @GetMapping("/course")
    public List<StudentCourseListItemVO> myCourses(@AuthenticationPrincipal UserInfoResponse user) {
        return service.listMyCourses(user.getId());
    }

    @GetMapping("/course/{id}")
    public Map<String, Object> detail(@PathVariable Long id,
                                      @AuthenticationPrincipal UserInfoResponse user) {
        return service.detail(id, user.getId());
    }

    @GetMapping("/course/{id}/announcements")
    public List<AnnouncementVO> announcements(@PathVariable Long id,
                                              @AuthenticationPrincipal UserInfoResponse user) {
        return service.listAnnouncements(id, user.getId());
    }

    @GetMapping("/score")
    public List<StudentScoreVO> myScores(@AuthenticationPrincipal UserInfoResponse user) {
        return service.myScores(user.getId());
    }
}