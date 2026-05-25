package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.teacher.*;
import com.scnu.springbootjdk17demo.entity.*;
import com.scnu.springbootjdk17demo.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherCourseService {

    private final CourseMapper             courseMapper;
    private final SysTeacherMapper         teacherMapper;
    private final SysSemesterMapper        semesterMapper;
    private final SysClassMapper           classMapper;
    private final SysStudentMapper         studentMapper;
    private final CourseClassMapper        courseClassMapper;
    private final CourseStudentMapper      courseStudentMapper;
    private final CourseScheduleMapper     courseScheduleMapper;
    private final CourseAnnouncementMapper announcementMapper;
    private final CourseHomeworkMapper     homeworkMapper;
    private final HomeworkSubmissionMapper submissionMapper;
    private final SysUserMapper            userMapper;

    /**
     * 辅助方法：用当前 ThreadLocal 的数据源查（不要主动 set/clear）。
     * 调用方需要在外层先 set 好 "primary" 或 "replica"。
     */
    public Long currentTeacherId(Long userId) {
        SysTeacher t = teacherMapper.selectOne(
                new LambdaQueryWrapper<SysTeacher>().eq(SysTeacher::getUserId, userId));
        if (t == null) throw new AccessDeniedException("当前用户没有教师身份");
        return t.getId();
    }

    /** 辅助方法：同上，不切 ThreadLocal */
    public void assertOwn(Long courseId, Long userId) {
        Long teacherId = currentTeacherId(userId);
        Course c = courseMapper.selectById(courseId);
        if (c == null) throw new IllegalArgumentException("课程不存在");
        if (!Objects.equals(c.getTeacherId(), teacherId)) {
            throw new AccessDeniedException("这不是您的课程");
        }
    }

    /** 我的课程列表 */
    public List<TeacherCourseListItemVO> listMyCourses(Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            Long teacherId = currentTeacherId(userId);

            List<Course> courses = courseMapper.selectList(
                    new LambdaQueryWrapper<Course>()
                            .eq(Course::getTeacherId, teacherId)
                            .orderByDesc(Course::getCreatedAt));
            if (courses.isEmpty()) return List.of();

            Map<Long, String> semNames = semesterMapper.selectBatchIds(
                    courses.stream().map(Course::getSemesterId).collect(Collectors.toSet())
            ).stream().collect(Collectors.toMap(SysSemester::getId, SysSemester::getName));

            return courses.stream().map(c -> {
                TeacherCourseListItemVO v = new TeacherCourseListItemVO();
                v.setId(c.getId());
                v.setCode(c.getCode());
                v.setName(c.getName());
                v.setSemesterName(semNames.get(c.getSemesterId()));
                v.setCourseType(c.getCourseType());
                v.setCredit(c.getCredit());
                v.setTotalHours(c.getTotalHours());
                v.setStatus(c.getStatus());
                v.setClassCount(Math.toIntExact(courseClassMapper.selectCount(
                        new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, c.getId()))));
                v.setStudentCount(Math.toIntExact(courseStudentMapper.selectCount(
                        new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, c.getId()))));
                v.setAnnouncementCount(Math.toIntExact(announcementMapper.selectCount(
                        new LambdaQueryWrapper<CourseAnnouncement>().eq(CourseAnnouncement::getCourseId, c.getId()))));
                List<CourseHomework> hws = homeworkMapper.selectList(
                        new LambdaQueryWrapper<CourseHomework>().eq(CourseHomework::getCourseId, c.getId()));
                v.setHomeworkCount(hws.size());
                int pending = 0;
                for (CourseHomework hw : hws) {
                    pending += Math.toIntExact(submissionMapper.selectCount(
                            new LambdaQueryWrapper<HomeworkSubmission>()
                                    .eq(HomeworkSubmission::getHomeworkId, hw.getId())
                                    .isNull(HomeworkSubmission::getScore)));
                }
                v.setPendingGradeCount(pending);
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 课程详情 */
    public TeacherCourseDetailVO detail(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            assertOwn(courseId, userId);
            Course c = courseMapper.selectById(courseId);
            TeacherCourseDetailVO v = new TeacherCourseDetailVO();
            v.setId(c.getId()); v.setCode(c.getCode()); v.setName(c.getName());
            v.setCourseType(c.getCourseType()); v.setCredit(c.getCredit());
            v.setTotalHours(c.getTotalHours()); v.setStatus(c.getStatus());
            v.setDescription(c.getDescription());

            SysSemester sem = semesterMapper.selectById(c.getSemesterId());
            if (sem != null) v.setSemesterName(sem.getName());

            List<CourseClass> ccs = courseClassMapper.selectList(
                    new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, courseId));
            v.setClassCount(ccs.size());

            Map<Long, Long> stuCountByClass = studentMapper.selectList(
                            new LambdaQueryWrapper<SysStudent>().eq(SysStudent::getStatus, 1))
                    .stream().collect(Collectors.groupingBy(SysStudent::getClassId, Collectors.counting()));

            List<TeacherCourseDetailVO.ClassInfo> classes = ccs.stream().map(cc -> {
                SysClass cls = classMapper.selectById(cc.getClassId());
                TeacherCourseDetailVO.ClassInfo ci = new TeacherCourseDetailVO.ClassInfo();
                ci.setId(cls.getId()); ci.setCode(cls.getCode()); ci.setName(cls.getName());
                ci.setStudentCount(stuCountByClass.getOrDefault(cls.getId(), 0L).intValue());
                return ci;
            }).toList();
            v.setClasses(classes);

            v.setStudentCount(Math.toIntExact(courseStudentMapper.selectCount(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, courseId))));

            v.setSchedules(courseScheduleMapper.selectList(
                    new LambdaQueryWrapper<CourseSchedule>().eq(CourseSchedule::getCourseId, courseId)
                            .orderByAsc(CourseSchedule::getWeekday)
                            .orderByAsc(CourseSchedule::getPeriodStart)));
            return v;
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 学生名单 */
    public List<StudentRowVO> listStudents(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            assertOwn(courseId, userId);

            List<CourseStudent> cs = courseStudentMapper.selectList(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, courseId));
            if (cs.isEmpty()) return List.of();

            Set<Long> stuIds = cs.stream().map(CourseStudent::getStudentId).collect(Collectors.toSet());
            List<SysStudent> students = studentMapper.selectBatchIds(stuIds);
            Map<Long, String> userNick = userMapper.selectBatchIds(
                    students.stream().map(SysStudent::getUserId).toList()
            ).stream().collect(Collectors.toMap(SysUser::getId, SysUser::getNickname));
            Map<Long, String> classNames = classMapper.selectBatchIds(
                    students.stream().map(SysStudent::getClassId).toList()
            ).stream().collect(Collectors.toMap(SysClass::getId, SysClass::getName));

            List<Long> hwIds = homeworkMapper.selectList(
                            new LambdaQueryWrapper<CourseHomework>().eq(CourseHomework::getCourseId, courseId))
                    .stream().map(CourseHomework::getId).toList();

            return students.stream().map(s -> {
                StudentRowVO v = new StudentRowVO();
                v.setStudentId(s.getId());
                v.setStudentNo(s.getStudentNo());
                v.setName(userNick.getOrDefault(s.getUserId(), ""));
                v.setGender(s.getGender());
                v.setClassName(classNames.getOrDefault(s.getClassId(), ""));
                v.setPhone(s.getPhone());

                int subCnt = 0; BigDecimal totalScore = BigDecimal.ZERO; int scoreCnt = 0;
                if (!hwIds.isEmpty()) {
                    List<HomeworkSubmission> subs = submissionMapper.selectList(
                            new LambdaQueryWrapper<HomeworkSubmission>()
                                    .eq(HomeworkSubmission::getStudentId, s.getId())
                                    .in(HomeworkSubmission::getHomeworkId, hwIds));
                    subCnt = subs.size();
                    for (HomeworkSubmission sub : subs) {
                        if (sub.getScore() != null) {
                            totalScore = totalScore.add(sub.getScore());
                            scoreCnt++;
                        }
                    }
                }
                v.setSubmissionCount(subCnt);
                v.setAvgScore(scoreCnt > 0
                        ? totalScore.divide(new BigDecimal(scoreCnt), 1, java.math.RoundingMode.HALF_UP)
                        : null);
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }
}