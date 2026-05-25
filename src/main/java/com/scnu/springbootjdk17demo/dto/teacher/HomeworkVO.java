package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class HomeworkVO {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private OffsetDateTime deadline;
    private BigDecimal maxScore;
    private Integer allowLate;
    private Integer status;
    private OffsetDateTime publishedAt;
    private Integer submittedCount;
    private Integer gradedCount;
    private Integer totalStudents;
    private List<AttachmentVO> attachments;
}