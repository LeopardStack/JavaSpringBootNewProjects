package com.scnu.springbootjdk17demo.dto.teacher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class AnnouncementCreateRequest {
    @NotNull  private Long courseId;
    @NotBlank private String title;
    private String content;
    private Integer priority = 0;
    private List<Long> attachmentIds;
}