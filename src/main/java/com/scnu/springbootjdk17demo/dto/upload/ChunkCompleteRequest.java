package com.scnu.springbootjdk17demo.dto.upload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChunkCompleteRequest {
    @NotNull private Long attachmentId;
}