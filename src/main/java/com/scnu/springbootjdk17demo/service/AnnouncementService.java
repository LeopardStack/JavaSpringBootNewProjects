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
public class AnnouncementService {

    private static final String BIZ = "announcement";

    private final CourseAnnouncementMapper announcementMapper;
    private final AttachmentLinkMapper     linkMapper;
    private final TeacherCourseService     teacherCourseService;
    private final SysUserMapper            userMapper;

    public List<AnnouncementVO> listByCourse(Long courseId, Long userId) {
        DataSourceContextHolder.set("replica");
        try {
            teacherCourseService.assertOwn(courseId, userId);
            List<CourseAnnouncement> rows = announcementMapper.selectList(
                    new LambdaQueryWrapper<CourseAnnouncement>()
                            .eq(CourseAnnouncement::getCourseId, courseId)
                            .orderByDesc(CourseAnnouncement::getPublishedAt));
            return rows.stream().map(a -> {
                AnnouncementVO v = new AnnouncementVO();
                BeanUtils.copyProperties(a, v);
                SysUser u = userMapper.selectById(a.getPublisherId());
                v.setPublisherName(u == null ? "" : u.getNickname());
                v.setAttachments(linkMapper.selectByBiz(BIZ, a.getId()));
                return v;
            }).toList();
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public Long create(AnnouncementCreateRequest req, Long userId) {
        DataSourceContextHolder.set("primary");          // ← 必须在所有 DB 操作前
        try {
            teacherCourseService.assertOwn(req.getCourseId(), userId);
            CourseAnnouncement a = new CourseAnnouncement();
            BeanUtils.copyProperties(req, a, "attachmentIds");
            a.setPublisherId(userId);
            a.setPublishedAt(OffsetDateTime.now());
            a.setCreatedAt(OffsetDateTime.now());
            announcementMapper.insert(a);
            linkAttachments(a.getId(), req.getAttachmentIds());
            return a.getId();
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public void update(Long id, AnnouncementCreateRequest req, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            teacherCourseService.assertOwn(req.getCourseId(), userId);
            CourseAnnouncement a = announcementMapper.selectById(id);
            if (a == null) throw new IllegalArgumentException("公告不存在");
            BeanUtils.copyProperties(req, a, "attachmentIds");
            a.setId(id);
            announcementMapper.updateById(a);

            linkMapper.delete(new LambdaQueryWrapper<AttachmentLink>()
                    .eq(AttachmentLink::getBizType, BIZ).eq(AttachmentLink::getBizId, id));
            linkAttachments(id, req.getAttachmentIds());
        } finally { DataSourceContextHolder.clear(); }
    }

    @Transactional
    public void delete(Long id, Long userId) {
        DataSourceContextHolder.set("primary");
        try {
            CourseAnnouncement a = announcementMapper.selectById(id);
            if (a == null) return;
            teacherCourseService.assertOwn(a.getCourseId(), userId);
            announcementMapper.deleteById(id);
            linkMapper.delete(new LambdaQueryWrapper<AttachmentLink>()
                    .eq(AttachmentLink::getBizType, BIZ).eq(AttachmentLink::getBizId, id));
        } finally { DataSourceContextHolder.clear(); }
    }

    private void linkAttachments(Long announcementId, List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) return;
        int sort = 0;
        for (Long aid : attachmentIds) {
            AttachmentLink l = new AttachmentLink();
            l.setAttachmentId(aid);
            l.setBizType(BIZ);
            l.setBizId(announcementId);
            l.setSort(sort++);
            l.setCreatedAt(OffsetDateTime.now());
            linkMapper.insert(l);
        }
    }
}