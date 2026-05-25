package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.admin.ClassTreeNode;
import com.scnu.springbootjdk17demo.dto.admin.SemesterVO;
import com.scnu.springbootjdk17demo.dto.admin.TeacherSimpleVO;
import com.scnu.springbootjdk17demo.entity.*;
import com.scnu.springbootjdk17demo.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrgQueryService {

    private final SysSemesterMapper semesterMapper;
    private final SysCollegeMapper  collegeMapper;
    private final SysMajorMapper    majorMapper;
    private final SysClassMapper    classMapper;
    private final SysTeacherMapper  teacherMapper;
    private final SysUserMapper     userMapper;
    private final SysStudentMapper  studentMapper;

    /** 学期下拉 */
    public List<SemesterVO> listSemesters() {
        DataSourceContextHolder.set("replica");
        try {
            return semesterMapper.selectList(
                            new LambdaQueryWrapper<SysSemester>().orderByDesc(SysSemester::getStartDate))
                    .stream().map(s -> {
                        SemesterVO v = new SemesterVO();
                        BeanUtils.copyProperties(s, v);
                        return v;
                    }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 教师下拉（含学院名） */
    public List<TeacherSimpleVO> listTeachers() {
        DataSourceContextHolder.set("replica");
        try {
            List<SysTeacher> teachers = teacherMapper.selectList(
                    new LambdaQueryWrapper<SysTeacher>().eq(SysTeacher::getStatus, 1));
            if (teachers.isEmpty()) return List.of();

            Map<Long, String> userNickMap = userMapper.selectBatchIds(
                    teachers.stream().map(SysTeacher::getUserId).toList()
            ).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));

            Map<Long, String> collegeNameMap = collegeMapper.selectList(null)
                    .stream().collect(Collectors.toMap(SysCollege::getId, SysCollege::getName));

            return teachers.stream().map(t -> {
                TeacherSimpleVO v = new TeacherSimpleVO();
                v.setId(t.getId());
                v.setUserId(t.getUserId());
                v.setTeacherNo(t.getTeacherNo());
                v.setName(userNickMap.getOrDefault(t.getUserId(), ""));
                v.setTitle(t.getTitle());
                v.setCollegeName(collegeNameMap.getOrDefault(t.getCollegeId(), ""));
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 三级班级树（学院 → 专业 → 班级），用于课程创建时选班 */
    public List<ClassTreeNode> getClassTree() {
        DataSourceContextHolder.set("replica");
        try {
            List<SysCollege> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<SysCollege>().orderByAsc(SysCollege::getSort));
            List<SysMajor> majors = majorMapper.selectList(
                    new LambdaQueryWrapper<SysMajor>().orderByAsc(SysMajor::getSort));
            List<SysClass> classes = classMapper.selectList(null);

            // 统计每个班级的学生数
            Map<Long, Long> studentCountByClass = studentMapper.selectList(
                            new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getStatus, 1))
                    .stream().collect(Collectors.groupingBy(SysStudent::getClassId, Collectors.counting()));

            Map<Long, List<SysMajor>> majorsByCollege = majors.stream()
                    .collect(Collectors.groupingBy(SysMajor::getCollegeId));
            Map<Long, List<SysClass>> classesByMajor = classes.stream()
                    .collect(Collectors.groupingBy(SysClass::getMajorId));

            return colleges.stream().map(col -> {
                ClassTreeNode cn = new ClassTreeNode();
                cn.setKey("college:" + col.getId());
                cn.setId(col.getId());
                cn.setType("college");
                cn.setLabel(col.getName());
                cn.setChildren(majorsByCollege.getOrDefault(col.getId(), List.of()).stream().map(maj -> {
                    ClassTreeNode mn = new ClassTreeNode();
                    mn.setKey("major:" + maj.getId());
                    mn.setId(maj.getId());
                    mn.setType("major");
                    mn.setLabel(maj.getName());
                    mn.setChildren(classesByMajor.getOrDefault(maj.getId(), List.of()).stream().map(cls -> {
                        ClassTreeNode kn = new ClassTreeNode();
                        kn.setKey("class:" + cls.getId());
                        kn.setId(cls.getId());
                        kn.setType("class");
                        kn.setLabel(cls.getName());
                        kn.setStudentCount(studentCountByClass.getOrDefault(cls.getId(), 0L).intValue());
                        return kn;
                    }).toList());
                    return mn;
                }).toList());
                return cn;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }
}