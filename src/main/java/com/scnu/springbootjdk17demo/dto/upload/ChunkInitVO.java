package com.scnu.springbootjdk17demo.dto.upload;

import lombok.Data;
import java.util.List;

@Data
public class ChunkInitVO {
    private Long attachmentId;
    private List<Integer> uploadedChunks;   // 已经上传过的分片下标（断点续传用）
    private Boolean fastUpload;             // 秒传（已经有完整文件）
    private String fileName;
    private Long fileSize;
}