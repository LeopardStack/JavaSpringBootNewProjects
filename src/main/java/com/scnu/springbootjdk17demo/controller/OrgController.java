package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.admin.ClassTreeNode;
import com.scnu.springbootjdk17demo.dto.admin.SemesterVO;
import com.scnu.springbootjdk17demo.dto.admin.TeacherSimpleVO;
import com.scnu.springbootjdk17demo.service.OrgQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/org")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
public class OrgController {

    private final OrgQueryService orgQueryService;

    /** 学期列表（创建课程时学期下拉用） */
    @GetMapping("/semesters")
    public List<SemesterVO> semesters() {
        return orgQueryService.listSemesters();
    }

    /** 教师列表（创建课程时分配教师用） */
    @GetMapping("/teachers")
    public List<TeacherSimpleVO> teachers() {
        return orgQueryService.listTeachers();
    }

    /** 三级班级树（学院 → 专业 → 班级） */
    @GetMapping("/class-tree")
    public List<ClassTreeNode> classTree() {
        return orgQueryService.getClassTree();
    }
}