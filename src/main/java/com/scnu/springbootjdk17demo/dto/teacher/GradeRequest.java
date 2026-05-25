package com.scnu.springbootjdk17demo.dto.teacher;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeRequest {
    @NotNull private BigDecimal score;
    private String feedback;
}