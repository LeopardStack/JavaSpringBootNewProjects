package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("attachment")
public class Attachment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private String bucket;
    private String objectKey;
    private String fileHash;
    private Long uploaderId;
    private String uploadId;
    private Integer chunkSize;
    private Integer chunkTotal;
    private Integer chunkUploaded;
    private String chunkBitmap;
    private Integer status;            // 0=上传中 1=完成 2=失败
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
}