package com.scnu.springbootjdk17demo.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResultVO {
    private Long id;
    private String fileName;
    private Long fileSize;
    private String mimeType;
}