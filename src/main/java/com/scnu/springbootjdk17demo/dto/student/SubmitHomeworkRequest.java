package com.scnu.springbootjdk17demo.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class SubmitHomeworkRequest {
    @NotNull private Long homeworkId;
    private String content;
    private List<Long> attachmentIds;
}