package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.Data;

@Data
public class AttachmentVO {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String mimeType;
}