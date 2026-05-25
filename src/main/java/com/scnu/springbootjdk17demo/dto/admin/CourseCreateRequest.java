package com.scnu.springbootjdk17demo.dto.admin;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseCreateRequest {
    @NotBlank private String code;
    @NotBlank private String name;
    @NotNull  private Long semesterId;
    @NotNull  private Long teacherId;
    private String courseType = "offline";
    private BigDecimal credit;
    private Integer totalHours;
    private String coverUrl;
    private String description;
    private Integer status = 1;

    @NotEmpty private List<Long> classIds;
    private List<ScheduleItemDTO> schedules;
}