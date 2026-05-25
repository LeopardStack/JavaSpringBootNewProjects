package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
public class SubmissionRowVO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String className;
    private Long submissionId;          // null 表示未交
    private BigDecimal score;
    private String feedback;
    private OffsetDateTime submittedAt;
    private OffsetDateTime gradedAt;
    private Integer isLate;
}