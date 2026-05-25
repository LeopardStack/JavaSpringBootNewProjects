package com.scnu.springbootjdk17demo.service;

import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.config.MinioConfig;
import com.scnu.springbootjdk17demo.dto.teacher.UploadResultVO;
import com.scnu.springbootjdk17demo.entity.Attachment;
import com.scnu.springbootjdk17demo.mapper.AttachmentMapper;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 文件管理服务 —— Phase 4 仅支持简单整文件上传
 * 分片上传/断点续传留给 Phase 5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final AttachmentMapper attachmentMapper;

    /** 上传单文件 */
    public UploadResultVO upload(MultipartFile file, Long uploaderId) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("文件为空");

        String original = file.getOriginalFilename();
        if (original == null) original = "unknown";

        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String objectKey = "attachments/"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
                + "/" + UUID.randomUUID().toString().replace("-", "")
                + ext;

        // 推送到 MinIO
        try (InputStream in = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectKey)
                            .stream(in, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        // 落库
        DataSourceContextHolder.set("primary");
        try {
            Attachment a = new Attachment();
            a.setFileName(original);
            a.setFileSize(file.getSize());
            a.setMimeType(file.getContentType());
            a.setBucket(minioConfig.getBucket());
            a.setObjectKey(objectKey);
            a.setUploaderId(uploaderId);
            a.setStatus(1);
            a.setCreatedAt(OffsetDateTime.now());
            a.setCompletedAt(OffsetDateTime.now());
            attachmentMapper.insert(a);

            log.info("文件上传成功 id={}, name={}, size={}", a.getId(), original, file.getSize());
            return new UploadResultVO(a.getId(), original, file.getSize(), file.getContentType());
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    /** 生成预签名下载 URL（10 分钟有效） */
    public String presignedDownloadUrl(Long attachmentId) throws Exception {
        DataSourceContextHolder.set("replica");
        Attachment a;
        try {
            a = attachmentMapper.selectById(attachmentId);
        } finally {
            DataSourceContextHolder.clear();
        }
        if (a == null) throw new IllegalArgumentException("附件不存在");

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(a.getBucket())
                        .object(a.getObjectKey())
                        .expiry(10, TimeUnit.MINUTES)
                        .extraQueryParams(java.util.Map.of(
                                "response-content-disposition",
                                "attachment; filename=\"" + java.net.URLEncoder.encode(
                                        a.getFileName(), java.nio.charset.StandardCharsets.UTF_8) + "\""
                        ))
                        .build()
        );

        // 替换为公网/外网 URL（如果配了 publicEndpoint）
        if (minioConfig.getPublicEndpoint() != null
                && !minioConfig.getPublicEndpoint().equals(minioConfig.getEndpoint())) {
            url = url.replace(minioConfig.getEndpoint(), minioConfig.getPublicEndpoint());
        }
        return url;
    }

    /** 删除附件（同时清 MinIO 对象） */
    public void delete(Long attachmentId) throws Exception {
        DataSourceContextHolder.set("primary");
        try {
            Attachment a = attachmentMapper.selectById(attachmentId);
            if (a == null) return;

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(a.getBucket())
                            .object(a.getObjectKey())
                            .build()
            );
            attachmentMapper.deleteById(attachmentId);
        } finally {
            DataSourceContextHolder.clear();
        }
    }
}