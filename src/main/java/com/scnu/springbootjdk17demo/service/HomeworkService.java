package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.teacher.*;
import com.scnu.springbootjdk17demo.entity.*;
import com.scnu.springbootjdk17demo.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeworkService {

    private static final String BIZ = "homework";

    private final CourseHomeworkMapper     homeworkMapper;
    private final HomeworkSubmissionMapper submissionMapper;
    private final CourseStudentMapper      courseStudentMapper;
    private final AttachmentLinkMapper     linkMapper;
    private final TeacherCourseService     teacherCourseService;

    public List<HomeworkVO> listByCourse(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            teacherCourseService.assertOwn(courseId, userId);
            List<CourseHomework> rows = homeworkMapper.selectList(
                    new LambdaQueryWrapper<CourseHomework>()
                            .eq(CourseHomework::getCourseId, courseId)
                            .orderByDesc(CourseHomework::getPublishedAt));
            int totalStudents = Math.toIntExact(courseStudentMapper.selectCount(
                    new LambdaQueryWrapper<CourseStudent>().eq(CourseStudent::getCourseId, courseId)));

            return rows.stream().map(h -> {
                HomeworkVO v = new HomeworkVO();
                BeanUtils.copyProperties(h, v);
                v.setTotalStudents(totalStudents);
                v.setSubmittedCount(Math.toIntExact(submissionMapper.selectCount(
                        new LambdaQueryWrapper<HomeworkSubmission>()
                                .eq(HomeworkSubmission::getHomeworkId, h.getId())
                                .isNotNull(HomeworkSubmission::getSubmittedAt))));
                v.setGradedCount(Math.toIntExact(submissionMapper.selectCount(
                        new LambdaQueryWrapper<HomeworkSubmission>()
                                .eq(HomeworkSubmission::getHomeworkId, h.getId())
                                .isNotNull(HomeworkSubmission::getScore))));
                v.setAttachments(linkMapper.selectByBiz(BIZ, h.getId()));
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public Long create(HomeworkCreateRequest req, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            teacherCourseService.assertOwn(req.getCourseId(), userId);
            CourseHomework h = new CourseHomework();
            BeanUtils.copyProperties(req, h, "attachmentIds");
            h.setPublisherId(userId);
            h.setPublishedAt(OffsetDateTime.now());
            h.setCreatedAt(OffsetDateTime.now());
            homeworkMapper.insert(h);
            linkAttachments(h.getId(), req.getAttachmentIds());
            return h.getId();
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public void update(Long id, HomeworkCreateRequest req, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            teacherCourseService.assertOwn(req.getCourseId(), userId);
            CourseHomework h = homeworkMapper.selectById(id);
            if (h == null) throw new IllegalArgumentException("作业不存在");
            BeanUtils.copyProperties(req, h, "attachmentIds");
            h.setId(id);
            homeworkMapper.updateById(h);

            linkMapper.delete(new LambdaQueryWrapper<AttachmentLink>()
                    .eq(AttachmentLink::getBizType, BIZ).eq(AttachmentLink::getBizId, id));
            linkAttachments(id, req.getAttachmentIds());
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public void delete(Long id, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            CourseHomework h = homeworkMapper.selectById(id);
            if (h == null) return;
            teacherCourseService.assertOwn(h.getCourseId(), userId);
            homeworkMapper.deleteById(id);
            linkMapper.delete(new LambdaQueryWrapper<AttachmentLink>()
                    .eq(AttachmentLink::getBizType, BIZ).eq(AttachmentLink::getBizId, id));
        } finally { DataSourceContextHolder.clear(); }
    }

    public List<SubmissionRowVO> listSubmissions(Long homeworkId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            CourseHomework h = homeworkMapper.selectById(homeworkId);
            if (h == null) throw new IllegalArgumentException("作业不存在");
            teacherCourseService.assertOwn(h.getCourseId(), userId);
            return submissionMapper.selectSubmissionRows(homeworkId, h.getCourseId());
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public void grade(Long homeworkId, Long studentId, GradeRequest req, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            CourseHomework h = homeworkMapper.selectById(homeworkId);
            if (h == null) throw new IllegalArgumentException("作业不存在");
            teacherCourseService.assertOwn(h.getCourseId(), userId);

            HomeworkSubmission s = submissionMapper.selectOne(
                    new LambdaQueryWrapper<HomeworkSubmission>()
                            .eq(HomeworkSubmission::getHomeworkId, homeworkId)
                            .eq(HomeworkSubmission::getStudentId, studentId));
            if (s == null) {
                s = new HomeworkSubmission();
                s.setHomeworkId(homeworkId);
                s.setStudentId(studentId);
            }
            s.setScore(req.getScore());
            s.setFeedback(req.getFeedback());
            s.setGradedAt(OffsetDateTime.now());
            s.setGraderId(userId);
            if (s.getId() == null) submissionMapper.insert(s);
            else submissionMapper.updateById(s);
        } finally { DataSourceContextHolder.clear(); }
    }

    private void linkAttachments(Long homeworkId, List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) return;
        int sort = 0;
        for (Long aid : attachmentIds) {
            AttachmentLink l = new AttachmentLink();
            l.setAttachmentId(aid);
            l.setBizType(BIZ);
            l.setBizId(homeworkId);
            l.setSort(sort++);
            l.setCreatedAt(OffsetDateTime.now());
            linkMapper.insert(l);
        }
    }
}