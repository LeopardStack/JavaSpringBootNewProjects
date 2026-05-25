package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.config.MinioConfig;
import com.scnu.springbootjdk17demo.dto.upload.*;
import com.scnu.springbootjdk17demo.dto.teacher.UploadResultVO;
import com.scnu.springbootjdk17demo.entity.Attachment;
import com.scnu.springbootjdk17demo.mapper.AttachmentMapper;
import io.minio.*;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 分片断点续传：基于 MinIO composeObject 实现
 * 临时分片路径：temp/{uploadId}/chunk-{index}
 * 完成后合并到：attachments/yyyy/MM/{uuid}{ext}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkedUploadService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final AttachmentMapper attachmentMapper;

    /** 初始化：找已有的 / 新建一个 */
    public ChunkInitVO init(ChunkInitRequest req, Long uploaderId) {
        DataSourceContextHolder.set("primary");
        try {
            // 查同 fingerprint 的记录
            Attachment exist = attachmentMapper.selectOne(
                    new LambdaQueryWrapper<Attachment>()
                            .eq(Attachment::getFileHash, req.getFileHash())
                            .eq(Attachment::getFileSize, req.getFileSize())
                            .orderByDesc(Attachment::getCreatedAt)
                            .last("LIMIT 1"));

            ChunkInitVO vo = new ChunkInitVO();
            vo.setFileName(req.getFileName());
            vo.setFileSize(req.getFileSize());

            if (exist != null && exist.getStatus() == 1) {
                // 秒传：直接复用
                vo.setAttachmentId(exist.getId());
                vo.setUploadedChunks(List.of());
                vo.setFastUpload(true);
                return vo;
            }

            if (exist != null && exist.getStatus() == 0) {
                // 续传：返回已上传的分片
                vo.setAttachmentId(exist.getId());
                vo.setUploadedChunks(parseBitmap(exist.getChunkBitmap()));
                vo.setFastUpload(false);
                return vo;
            }

            // 新建
            Attachment a = new Attachment();
            a.setFileName(req.getFileName());
            a.setFileSize(req.getFileSize());
            a.setFileHash(req.getFileHash());
            a.setBucket(minioConfig.getBucket());
            a.setObjectKey(genObjectKey(req.getFileName()));
            a.setUploaderId(uploaderId);
            a.setUploadId(UUID.randomUUID().toString().replace("-", ""));
            a.setChunkSize(req.getChunkSize());
            a.setChunkTotal(req.getChunkTotal());
            a.setChunkUploaded(0);
            a.setChunkBitmap("");
            a.setStatus(0);
            a.setCreatedAt(OffsetDateTime.now());
            attachmentMapper.insert(a);

            vo.setAttachmentId(a.getId());
            vo.setUploadedChunks(List.of());
            vo.setFastUpload(false);
            return vo;
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 上传单个分片 */
    public void uploadChunk(Long attachmentId, Integer chunkIndex, MultipartFile chunk) throws Exception {
        DataSourceContextHolder.set("primary");
        Attachment a;
        try {
            a = attachmentMapper.selectById(attachmentId);
            if (a == null) throw new IllegalArgumentException("上传记录不存在");
            if (a.getStatus() == 1) throw new IllegalStateException("文件已完成上传");
            if (chunkIndex < 0 || chunkIndex >= a.getChunkTotal()) throw new IllegalArgumentException("分片下标越界");
        } finally { DataSourceContextHolder.clear(); }

        // 推到 MinIO 的 temp 目录
        String chunkKey = "temp/" + a.getUploadId() + "/chunk-" + chunkIndex;
        try (InputStream in = chunk.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(a.getBucket())
                            .object(chunkKey)
                            .stream(in, chunk.getSize(), -1)
                            .build()
            );
        }

        // 更新 bitmap
        DataSourceContextHolder.set("primary");
        try {
            // 重新拉一次防止并发
            Attachment fresh = attachmentMapper.selectById(attachmentId);
            Set<Integer> set = new TreeSet<>(parseBitmap(fresh.getChunkBitmap()));
            set.add(chunkIndex);
            fresh.setChunkBitmap(set.stream().map(String::valueOf).collect(Collectors.joining(",")));
            fresh.setChunkUploaded(set.size());
            attachmentMapper.updateById(fresh);
        } finally { DataSourceContextHolder.clear(); }
    }

    /** 完成上传：合并所有分片 */
    @Transactional
    public UploadResultVO complete(Long attachmentId) throws Exception {
        DataSourceContextHolder.set("primary");
        Attachment a;
        try {
            a = attachmentMapper.selectById(attachmentId);
            if (a == null) throw new IllegalArgumentException("上传记录不存在");
            if (a.getStatus() == 1) {
                return new UploadResultVO(a.getId(), a.getFileName(), a.getFileSize(), a.getMimeType());
            }
            Set<Integer> uploaded = new TreeSet<>(parseBitmap(a.getChunkBitmap()));
            if (uploaded.size() != a.getChunkTotal()) {
                throw new IllegalStateException("尚有 " + (a.getChunkTotal() - uploaded.size()) + " 个分片未上传");
            }
        } finally { DataSourceContextHolder.clear(); }

        // 用 composeObject 把 temp/{uploadId}/chunk-0..N-1 合并
        List<ComposeSource> sources = IntStream.range(0, a.getChunkTotal())
                .mapToObj(i -> ComposeSource.builder()
                        .bucket(a.getBucket())
                        .object("temp/" + a.getUploadId() + "/chunk-" + i)
                        .build())
                .toList();

        minioClient.composeObject(
                ComposeObjectArgs.builder()
                        .bucket(a.getBucket())
                        .object(a.getObjectKey())
                        .sources(sources)
                        .build()
        );

        // 清理 temp 分片
        List<DeleteObject> toDelete = IntStream.range(0, a.getChunkTotal())
                .mapToObj(i -> new DeleteObject("temp/" + a.getUploadId() + "/chunk-" + i))
                .toList();
        // 触发遍历（returnsLazyIterator）
        for (var r : minioClient.removeObjects(
                RemoveObjectsArgs.builder().bucket(a.getBucket()).objects(toDelete).build())) {
            r.get();
        }

        // 更新 DB
        DataSourceContextHolder.set("primary");
        try {
            a.setStatus(1);
            a.setCompletedAt(OffsetDateTime.now());
            attachmentMapper.updateById(a);
        } finally { DataSourceContextHolder.clear(); }

        log.info("分片上传完成: id={}, name={}, size={}", a.getId(), a.getFileName(), a.getFileSize());
        return new UploadResultVO(a.getId(), a.getFileName(), a.getFileSize(), a.getMimeType());
    }

    private List<Integer> parseBitmap(String bitmap) {
        if (bitmap == null || bitmap.isEmpty()) return List.of();
        return Arrays.stream(bitmap.split(","))
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .toList();
    }

    private String genObjectKey(String fileName) {
        String ext = fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.'))
                : "";
        return "attachments/"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"))
                + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
    }
}