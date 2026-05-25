package com.scnu.springbootjdk17demo.dto.teacher;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class HomeworkCreateRequest {
    @NotNull  private Long courseId;
    @NotBlank private String title;
    private String description;
    private OffsetDateTime deadline;
    private BigDecimal maxScore = new BigDecimal("100");
    private Integer allowLate = 0;
    private Integer status = 1;
    private List<Long> attachmentIds;
}