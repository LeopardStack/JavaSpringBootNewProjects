package com.scnu.springbootjdk17demo.dto.student;

import com.scnu.springbootjdk17demo.dto.teacher.AttachmentVO;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class StudentHomeworkVO {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private OffsetDateTime deadline;
    private BigDecimal maxScore;
    private Integer allowLate;
    // 提交状态
    private Long submissionId;
    private String submissionContent;
    private BigDecimal score;
    private String feedback;
    private OffsetDateTime submittedAt;
    private OffsetDateTime gradedAt;
    private List<AttachmentVO> attachments;             // 老师上传的附件
    private List<AttachmentVO> submissionAttachments;   // 学生上传的附件
    // 状态: pending=待交 submitted=已交未批改 graded=已批改 late=已迟交 expired=已过期未交
    private String status;
}