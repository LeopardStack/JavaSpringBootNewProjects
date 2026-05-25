package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class AnnouncementVO {
    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private Integer priority;
    private String publisherName;
    private OffsetDateTime publishedAt;
    private List<AttachmentVO> attachments;
}