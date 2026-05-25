package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.PageResult;
import com.scnu.springbootjdk17demo.dto.admin.*;
import com.scnu.springbootjdk17demo.entity.*;
import com.scnu.springbootjdk17demo.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper          courseMapper;
    private final CourseClassMapper     courseClassMapper;
    private final CourseStudentMapper   courseStudentMapper;
    private final CourseScheduleMapper  courseScheduleMapper;
    private final SysSemesterMapper     semesterMapper;
    private final SysTeacherMapper      teacherMapper;
    private final SysUserMapper         userMapper;

    // ========================== 创建 ==========================
    @Transactional
    public Long create(CourseCreateRequest req, Long adminUserId) {
        DataSourceContextHolder.set("primary");
        try {
            // 1. 主表
            Course c = new Course();
            BeanUtils.copyProperties(req, c, "classIds", "schedules");
            c.setCreatedBy(adminUserId);
            c.setCreatedAt(OffsetDateTime.now());
            c.setUpdatedAt(OffsetDateTime.now());
            courseMapper.insert(c);

            // 2. 班级关联
            syncCourseClasses(c.getId(), req.getClassIds());

            // 3. 自动展开成选课名单
            syncCourseStudentsFromClasses(c.getId(), req.getClassIds());

            // 4. 排课
            if (req.getSchedules() != null) {
                for (ScheduleItemDTO sd : req.getSchedules()) {
                    CourseSchedule s = new CourseSchedule();
                    BeanUtils.copyProperties(sd, s);
                    s.setCourseId(c.getId());
                    courseScheduleMapper.insert(s);
                }
            }
            log.info("创建课程 [{}] 成功, id={}, 关联 {} 个班级", c.getName(), c.getId(), req.getClassIds().size());
            return c.getId();
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    // ========================== 更新 ==========================
    @Transactional
    public void update(Long id, CourseUpdateRequest req) {
        DataSourceContextHolder.set("primary");
        try {
            Course c = courseMapper.selectById(id);
            if (c == null) throw new IllegalArgumentException("课程不存在");

            BeanUtils.copyProperties(req, c, "classIds", "schedules");
            c.setId(id);
            c.setUpdatedAt(OffsetDateTime.now());
            courseMapper.updateById(c);

            // 班级 + 学生名单 diff 同步
            syncCourseClasses(id, req.getClassIds());
            syncCourseStudentsFromClasses(id, req.getClassIds());

            // 排课全量替换（简单粗暴，演示足够）
            courseScheduleMapper.delete(
                    new LambdaQueryWrapper<CourseSchedule>().eq(CourseSchedule::getCourseId, id));
            if (req.getSchedules() != null) {
                for (ScheduleItemDTO sd : req.getSchedules()) {
                    CourseSchedule s = new CourseSchedule();
                    BeanUtils.copyProperties(sd, s);
                    s.setCourseId(id);
                    courseScheduleMapper.insert(s);
                }
            }
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    // ========================== 删除 ==========================
    @Transactional
    public void delete(Long id) {
        DataSourceContextHolder.set("primary");
        try {
            // ON DELETE CASCADE 会自动清理关联表
            courseMapper.deleteById(id);
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    // ========================== 列表 ==========================
    public PageResult<CourseListItemVO> list(int current, int size, CourseListQuery q) {
        DataSourceContextHolder.set("replica");
        try {
            IPage<CourseListItemVO> page = new Page<>(current, size);
            IPage<CourseListItemVO> result = courseMapper.selectListVO(page, q);
            return PageResult.of(result);
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    // ========================== 详情 ==========================
    public CourseDetailVO detail(Long id) {
        DataSourceContextHolder.set("replica");
        try {
            Course c = courseMapper.selectById(id);
            if (c == null) return null;

            CourseDetailVO vo = new CourseDetailVO();
            BeanUtils.copyProperties(c, vo);

            // 学期 / 教师名
            SysSemester sem = semesterMapper.selectById(c.getSemesterId());
            if (sem != null) vo.setSemesterName(sem.getName());

            SysTeacher t = teacherMapper.selectById(c.getTeacherId());
            if (t != null) {
                SysUser u = userMapper.selectById(t.getUserId());
                if (u != null) vo.setTeacherName(u.getNickname());
            }

            // 关联班级
            List<CourseClass> ccs = courseClassMapper.selectList(
                    new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, id));
            vo.setClassIds(ccs.stream().map(CourseClass::getClassId).toList());

            // 排课
            vo.setSchedules(courseScheduleMapper.selectList(
                    new LambdaQueryWrapper<CourseSchedule>().eq(CourseSchedule::getCourseId, id)));

            // 学生数
            Long cnt = courseStudentMapper.selectCount(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, id));
            vo.setStudentCount(cnt == null ? 0 : cnt.intValue());

            return vo;
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    // ========================== 内部辅助 ==========================
    private void syncCourseClasses(Long courseId, List<Long> classIds) {
        // 删旧的
        courseClassMapper.delete(
                new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, courseId));
        // 插新的
        for (Long cid : classIds) {
            CourseClass cc = new CourseClass();
            cc.setCourseId(courseId);
            cc.setClassId(cid);
            courseClassMapper.insert(cc);
        }
    }

    private void syncCourseStudentsFromClasses(Long courseId, List<Long> classIds) {
        // 全量替换：先删后插
        courseStudentMapper.delete(
                new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, courseId));
        if (classIds == null || classIds.isEmpty()) return;

        List<Long> studentIds = courseStudentMapper.selectStudentIdsByClassIds(classIds);
        for (Long sid : studentIds) {
            CourseStudent cs = new CourseStudent();
            cs.setCourseId(courseId);
            cs.setStudentId(sid);
            cs.setStatus(1);
            cs.setEnrolledAt(OffsetDateTime.now());
            courseStudentMapper.insert(cs);
        }
    }
}