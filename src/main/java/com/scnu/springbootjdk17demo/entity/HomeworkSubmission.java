package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@TableName("homework_submission")
public class HomeworkSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String content;
    private BigDecimal score;
    private String feedback;
    private Integer isLate;
    private OffsetDateTime submittedAt;
    private OffsetDateTime gradedAt;
    private Long graderId;
}