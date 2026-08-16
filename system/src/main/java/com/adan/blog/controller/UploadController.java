package com.adan.blog.controller;

import com.adan.blog.service.OssStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/** 文件上传（封面图/正文图）：启用 OSS 时直传阿里云 OSS，否则存本地磁盘 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UploadController {

    private final OssStorageService ossStorageService;

    @Value("${adan.blog.upload-dir}")
    private String uploadDir;

    private static final java.util.Set<String> ALLOWED = java.util.Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "文件为空"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED.contains(contentType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "仅支持 JPG/PNG/WebP/GIF 图片"));
        }
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };

        // OSS 优先
        if (ossStorageService.enabled()) {
            try {
                String url = ossStorageService.upload(file.getInputStream(), contentType, ext);
                return ResponseEntity.ok(Map.of("url", url));
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(Map.of("error", "OSS 上传失败: " + e.getMessage()));
            }
        }

        // 本地磁盘回退
        String dateDir = LocalDate.now().toString();
        Path dir = Paths.get(uploadDir, dateDir);
        Files.createDirectories(dir);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(filename);
        file.transferTo(target);
        String url = "/uploads/" + dateDir + "/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
