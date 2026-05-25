package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.student.*;
import com.scnu.springbootjdk17demo.entity.*;
import com.scnu.springbootjdk17demo.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentHomeworkService {

    private static final String BIZ_SUBMISSION = "submission";

    private final CourseHomeworkMapper      homeworkMapper;
    private final HomeworkSubmissionMapper  submissionMapper;
    private final CourseStudentMapper       courseStudentMapper;
    private final CourseMapper              courseMapper;
    private final AttachmentLinkMapper      linkMapper;
    private final StudentCourseService      studentCourseService;

    /** 课程的作业列表（学生视角） */
    public List<StudentHomeworkVO> listByCourse(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            studentCourseService.assertEnrolled(courseId, userId);
            Long sid = studentCourseService.currentStudentId(userId);
            return buildVOs(homeworkMapper.selectList(
                    new LambdaQueryWrapper<CourseHomework>()
                            .eq(CourseHomework::getCourseId, courseId)
                            .eq(CourseHomework::getStatus, 1)
                            .orderByDesc(CourseHomework::getPublishedAt)), sid);
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 跨课程的全部作业 */
    public List<StudentHomeworkVO> listAll(Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            Long sid = studentCourseService.currentStudentId(userId);
            List<CourseStudent> enrollments = courseStudentMapper.selectList(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getStudentId, sid));
            if (enrollments.isEmpty()) return List.of();
            Set<Long> courseIds = enrollments.stream().map(CourseStudent::getCourseId).collect(Collectors.toSet());

            List<CourseHomework> hws = homeworkMapper.selectList(
                    new LambdaQueryWrapper<CourseHomework>()
                            .in(CourseHomework::getCourseId, courseIds)
                            .eq(CourseHomework::getStatus, 1)
                            .orderByAsc(CourseHomework::getDeadline));
            return buildVOs(hws, sid);
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 提交作业 */
    @Transactional
    public Long submit(SubmitHomeworkRequest req, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            CourseHomework h = homeworkMapper.selectById(req.getHomeworkId());
            if (h == null) throw new IllegalArgumentException("作业不存在");
            studentCourseService.assertEnrolled(h.getCourseId(), userId);
            Long sid = studentCourseService.currentStudentId(userId);

            boolean late = h.getDeadline() != null && OffsetDateTime.now().isAfter(h.getDeadline());
            if (late && h.getAllowLate() != null && h.getAllowLate() == 0) {
                throw new IllegalStateException("作业已截止，且不允许迟交");
            }

            HomeworkSubmission s = submissionMapper.selectOne(
                    new LambdaQueryWrapper<HomeworkSubmission>()
                            .eq(HomeworkSubmission::getHomeworkId, req.getHomeworkId())
                            .eq(HomeworkSubmission::getStudentId, sid));
            boolean isNew = (s == null);
            if (isNew) {
                s = new HomeworkSubmission();
                s.setHomeworkId(req.getHomeworkId());
                s.setStudentId(sid);
            }
            s.setContent(req.getContent());
            s.setSubmittedAt(OffsetDateTime.now());
            s.setIsLate(late ? 1 : 0);
            // 重新提交则清除原评分（让老师重新批）
            s.setScore(null);
            s.setFeedback(null);
            s.setGradedAt(null);
            s.setGraderId(null);

            if (isNew) submissionMapper.insert(s);
            else        submissionMapper.updateById(s);

            // 重置附件
            linkMapper.delete(new LambdaQueryWrapper<AttachmentLink>()
                    .eq(AttachmentLink::getBizType, BIZ_SUBMISSION)
                    .eq(AttachmentLink::getBizId, s.getId()));
            if (req.getAttachmentIds() != null && !req.getAttachmentIds().isEmpty()) {
                int sort = 0;
                for (Long aid : req.getAttachmentIds()) {
                    AttachmentLink l = new AttachmentLink();
                    l.setAttachmentId(aid);
                    l.setBizType(BIZ_SUBMISSION);
                    l.setBizId(s.getId());
                    l.setSort(sort++);
                    l.setCreatedAt(OffsetDateTime.now());
                    linkMapper.insert(l);
                }
            }
            return s.getId();
        } finally { DataSourceContextHolder.clear(); }
    }

    /* ───────────── 工具：批量构建 VO ───────────── */
    private List<StudentHomeworkVO> buildVOs(List<CourseHomework> hws, Long studentId) {
        if (hws.isEmpty()) return List.of();
        Set<Long> courseIds = hws.stream().map(CourseHomework::getCourseId).collect(Collectors.toSet());
        Map<Long, String> courseNames = courseMapper.selectBatchIds(courseIds)
                .stream().collect(Collectors.toMap(Course::getId, Course::getName));

        List<HomeworkSubmission> subs = submissionMapper.selectList(
                new LambdaQueryWrapper<HomeworkSubmission>()
                        .eq(HomeworkSubmission::getStudentId, studentId)
                        .in(HomeworkSubmission::getHomeworkId, hws.stream().map(CourseHomework::getId).toList()));
        Map<Long, HomeworkSubmission> subMap = subs.stream()
                .collect(Collectors.toMap(HomeworkSubmission::getHomeworkId, s -> s));

        OffsetDateTime now = OffsetDateTime.now();
        return hws.stream().map(h -> {
            StudentHomeworkVO v = new StudentHomeworkVO();
            v.setId(h.getId());
            v.setCourseId(h.getCourseId());
            v.setCourseName(courseNames.getOrDefault(h.getCourseId(), ""));
            v.setTitle(h.getTitle());
            v.setDescription(h.getDescription());
            v.setDeadline(h.getDeadline());
            v.setMaxScore(h.getMaxScore());
            v.setAllowLate(h.getAllowLate());
            v.setAttachments(linkMapper.selectByBiz("homework", h.getId()));

            HomeworkSubmission s = subMap.get(h.getId());
            if (s == null) {
                v.setStatus(h.getDeadline() != null && now.isAfter(h.getDeadline()) ? "expired" : "pending");
            } else {
                v.setSubmissionId(s.getId());
                v.setSubmissionContent(s.getContent());
                v.setScore(s.getScore());
                v.setFeedback(s.getFeedback());
                v.setSubmittedAt(s.getSubmittedAt());
                v.setGradedAt(s.getGradedAt());
                v.setSubmissionAttachments(linkMapper.selectByBiz(BIZ_SUBMISSION, s.getId()));
                if (s.getScore() != null)            v.setStatus("graded");
                else if (s.getIsLate() != null && s.getIsLate() == 1) v.setStatus("late");
                else                                  v.setStatus("submitted");
            }
            return v;
        }).toList();
    }
}