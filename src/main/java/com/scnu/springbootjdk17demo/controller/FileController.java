package com.scnu.springbootjdk17demo.controller;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.teacher.UploadResultVO;
import com.scnu.springbootjdk17demo.dto.upload.*;
import com.scnu.springbootjdk17demo.service.ChunkedUploadService;
import com.scnu.springbootjdk17demo.service.FileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService         fileService;
    private final ChunkedUploadService chunkedUploadService;

    /* ─── 简单上传（保留） ───────────────────────── */
    @PostMapping("/upload")
    public UploadResultVO upload(@RequestParam("file") MultipartFile file,
                                 @AuthenticationPrincipal UserInfoResponse user) throws Exception {
        return fileService.upload(file, user.getId());
    }

    /* ─── 下载（保留） ─────────────────────────── */
    @GetMapping("/{id}/url")
    public Map<String, String> downloadUrl(@PathVariable Long id) throws Exception {
        return Map.of("url", fileService.presignedDownloadUrl(id));
    }

    /* ─── 分片上传 3 接口 ────────────────────────── */
    @PostMapping("/chunk/init")
    public ChunkInitVO chunkInit(@Valid @RequestBody ChunkInitRequest req,
                                 @AuthenticationPrincipal UserInfoResponse user) {
        return chunkedUploadService.init(req, user.getId());
    }

    @PostMapping("/chunk/upload")
    public Map<String, Object> chunkUpload(@RequestParam Long attachmentId,
                                           @RequestParam Integer chunkIndex,
                                           @RequestParam("file") MultipartFile chunk) throws Exception {
        chunkedUploadService.uploadChunk(attachmentId, chunkIndex, chunk);
        return Map.of("code", 200, "chunkIndex", chunkIndex);
    }

    @PostMapping("/chunk/complete")
    public UploadResultVO chunkComplete(@Valid @RequestBody ChunkCompleteRequest req) throws Exception {
        return chunkedUploadService.complete(req.getAttachmentId());
    }
}