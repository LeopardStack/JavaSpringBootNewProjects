package com.scnu.springbootjdk17demo.dto.upload;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ChunkInitRequest {
    @NotBlank private String fileName;
    @NotNull  private Long fileSize;
    @NotBlank private String fileHash;   // 客户端 fingerprint（可以是 name+size+lastModified）
    @NotNull  private Integer chunkSize;
    @NotNull  private Integer chunkTotal;
}