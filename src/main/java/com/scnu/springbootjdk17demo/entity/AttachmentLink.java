package com.scnu.springbootjdk17demo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@TableName("attachment_link")
public class AttachmentLink {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long attachmentId;
    private String bizType;
    private Long bizId;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private OffsetDateTime createdAt;
}