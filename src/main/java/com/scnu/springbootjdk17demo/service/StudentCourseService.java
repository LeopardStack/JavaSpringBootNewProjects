package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.student.*;
import com.scnu.springbootjdk17demo.dto.teacher.AnnouncementVO;
import com.scnu.springbootjdk17demo.entity.*;
import com.scnu.springbootjdk17demo.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentCourseService {

    private final SysStudentMapper          studentMapper;
    private final CourseStudentMapper       courseStudentMapper;
    private final CourseMapper              courseMapper;
    private final SysSemesterMapper         semesterMapper;
    private final SysTeacherMapper          teacherMapper;
    private final SysUserMapper             userMapper;
    private final CourseScheduleMapper      scheduleMapper;
    private final CourseAnnouncementMapper  announcementMapper;
    private final CourseHomeworkMapper      homeworkMapper;
    private final HomeworkSubmissionMapper  submissionMapper;
    private final AttachmentLinkMapper      linkMapper;

    /** 当前用户对应的 sys_student.id（不切 DS） */
    public Long currentStudentId(Long userId) {
        SysStudent s = studentMapper.selectOne(
                new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getUserId, userId));
        if (s == null) throw new AccessDeniedException("当前用户没有学生身份");
        return s.getId();
    }

    /** 校验学生确实选了这门课 */
    public void assertEnrolled(Long courseId, Long userId) {
        Long sid = currentStudentId(userId);
        Long cnt = courseStudentMapper.selectCount(
                new LambdaQueryWrapper<CourseStudent>()
                        .eq(CourseStudent::getCourseId, courseId)
                        .eq(CourseStudent::getStudentId, sid));
        if (cnt == null || cnt == 0) throw new AccessDeniedException("您没有选这门课");
    }

    /** 我的课程列表 */
    public List<StudentCourseListItemVO> listMyCourses(Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            Long sid = currentStudentId(userId);
            List<CourseStudent> enrollments = courseStudentMapper.selectList(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getStudentId, sid));
            if (enrollments.isEmpty()) return List.of();

            Set<Long> courseIds = enrollments.stream().map(CourseStudent::getCourseId).collect(Collectors.toSet());
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            Map<Long, String> semNames = semesterMapper.selectBatchIds(
                    courses.stream().map(Course::getSemesterId).collect(Collectors.toSet())
            ).stream().collect(Collectors.toMap(SysSemester::getId, SysSemester::getName));

            List<SysTeacher> teachers = teacherMapper.selectBatchIds(
                    courses.stream().map(Course::getTeacherId).collect(Collectors.toSet()));
            Map<Long, SysTeacher> teacherMap = teachers.stream()
                    .collect(Collectors.toMap(SysTeacher::getId, t -> t));
            Map<Long, String> userNick = userMapper.selectBatchIds(
                    teachers.stream().map(SysTeacher::getUserId).toList()
            ).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));

            return courses.stream().map(c -> {
                StudentCourseListItemVO v = new StudentCourseListItemVO();
                v.setId(c.getId());
                v.setCode(c.getCode());
                v.setName(c.getName());
                v.setSemesterName(semNames.get(c.getSemesterId()));
                v.setCourseType(c.getCourseType());
                v.setCredit(c.getCredit());
                v.setTotalHours(c.getTotalHours());

                SysTeacher t = teacherMap.get(c.getTeacherId());
                if (t != null) {
                    v.setTeacherName(userNick.getOrDefault(t.getUserId(), ""));
                    v.setTeacherTitle(t.getTitle());
                }

                // 统计待交作业
                List<CourseHomework> hws = homeworkMapper.selectList(
                        new LambdaQueryWrapper<CourseHomework>()
                                .eq(CourseHomework::getCourseId, c.getId())
                                .eq(CourseHomework::getStatus, 1));
                if (!hws.isEmpty()) {
                    List<Long> hwIds = hws.stream().map(CourseHomework::getId).toList();
                    long submittedCnt = submissionMapper.selectCount(
                            new LambdaQueryWrapper<HomeworkSubmission>()
                                    .eq(HomeworkSubmission::getStudentId, sid)
                                    .in(HomeworkSubmission::getHomeworkId, hwIds)
                                    .isNotNull(HomeworkSubmission::getSubmittedAt));
                    v.setPendingHomeworkCount(hws.size() - (int) submittedCnt);
                } else {
                    v.setPendingHomeworkCount(0);
                }
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 课程详情（包含老师信息、班级、排课）*/
    public Map<String, Object> detail(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            assertEnrolled(courseId, userId);
            Course c = courseMapper.selectById(courseId);
            SysSemester sem = semesterMapper.selectById(c.getSemesterId());
            SysTeacher t = teacherMapper.selectById(c.getTeacherId());
            SysUser tUser = t != null ? userMapper.selectById(t.getUserId()) : null;

            Map<String, Object> result = new HashMap<>();
            result.put("id", c.getId());
            result.put("code", c.getCode());
            result.put("name", c.getName());
            result.put("courseType", c.getCourseType());
            result.put("credit", c.getCredit());
            result.put("totalHours", c.getTotalHours());
            result.put("status", c.getStatus());
            result.put("description", c.getDescription());
            result.put("semesterName", sem == null ? "" : sem.getName());
            result.put("teacher", t == null ? null : Map.of(
                    "id", t.getId(),
                    "name", tUser == null ? "" : tUser.getNickname(),
                    "title", t.getTitle() == null ? "" : t.getTitle(),
                    "intro", t.getIntro() == null ? "" : t.getIntro()
            ));
            result.put("schedules", scheduleMapper.selectList(
                    new LambdaQueryWrapper<CourseSchedule>().eq(CourseSchedule::getCourseId, courseId)
                            .orderByAsc(CourseSchedule::getWeekday)
                            .orderByAsc(CourseSchedule::getPeriodStart)));
            return result;
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 学生看公告（只读） */
    public List<AnnouncementVO> listAnnouncements(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            assertEnrolled(courseId, userId);
            List<CourseAnnouncement> rows = announcementMapper.selectList(
                    new LambdaQueryWrapper<CourseAnnouncement>()
                            .eq(CourseAnnouncement::getCourseId, courseId)
                            .orderByDesc(CourseAnnouncement::getPublishedAt));
            return rows.stream().map(a -> {
                AnnouncementVO v = new AnnouncementVO();
                BeanUtils.copyProperties(a, v);
                SysUser u = userMapper.selectById(a.getPublisherId());
                v.setPublisherName(u == null ? "" : u.getNickname());
                v.setAttachments(linkMapper.selectByBiz("announcement", a.getId()));
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 我的成绩单（按课程聚合） */
    public List<StudentScoreVO> myScores(Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            Long sid = currentStudentId(userId);
            List<CourseStudent> enrollments = courseStudentMapper.selectList(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getStudentId, sid));
            if (enrollments.isEmpty()) return List.of();

            Set<Long> courseIds = enrollments.stream().map(CourseStudent::getCourseId).collect(Collectors.toSet());
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            Map<Long, String> semNames = semesterMapper.selectBatchIds(
                    courses.stream().map(Course::getSemesterId).collect(Collectors.toSet())
            ).stream().collect(Collectors.toMap(SysSemester::getId, SysSemester::getName));
            List<SysTeacher> teachers = teacherMapper.selectBatchIds(
                    courses.stream().map(Course::getTeacherId).collect(Collectors.toSet()));
            Map<Long, SysTeacher> teacherMap = teachers.stream()
                    .collect(Collectors.toMap(SysTeacher::getId, t -> t));
            Map<Long, String> teacherNicks = userMapper.selectBatchIds(
                    teachers.stream().map(SysTeacher::getUserId).toList()
            ).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));

            Map<Long, CourseStudent> enrollMap = enrollments.stream()
                    .collect(Collectors.toMap(CourseStudent::getCourseId, e -> e));

            return courses.stream().map(c -> {
                StudentScoreVO v = new StudentScoreVO();
                v.setCourseId(c.getId());
                v.setCourseCode(c.getCode());
                v.setCourseName(c.getName());
                v.setSemesterName(semNames.get(c.getSemesterId()));
                v.setCredit(c.getCredit());
                SysTeacher t = teacherMap.get(c.getTeacherId());
                if (t != null) v.setTeacherName(teacherNicks.getOrDefault(t.getUserId(), ""));
                v.setFinalScore(enrollMap.get(c.getId()).getFinalScore());

                List<CourseHomework> hws = homeworkMapper.selectList(
                        new LambdaQueryWrapper<CourseHomework>().eq(CourseHomework::getCourseId, c.getId()));
                v.setHomeworkCount(hws.size());
                if (!hws.isEmpty()) {
                    List<HomeworkSubmission> subs = submissionMapper.selectList(
                            new LambdaQueryWrapper<HomeworkSubmission>()
                                    .eq(HomeworkSubmission::getStudentId, sid)
                                    .in(HomeworkSubmission::getHomeworkId, hws.stream().map(CourseHomework::getId).toList()));
                    v.setSubmittedCount(subs.size());
                    BigDecimal total = BigDecimal.ZERO; int cnt = 0;
                    for (HomeworkSubmission s : subs) {
                        if (s.getScore() != null) { total = total.add(s.getScore()); cnt++; }
                    }
                    if (cnt > 0) v.setAvgHomeworkScore(total.divide(new BigDecimal(cnt), 1, RoundingMode.HALF_UP));
                } else {
                    v.setSubmittedCount(0);
                }
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }
}