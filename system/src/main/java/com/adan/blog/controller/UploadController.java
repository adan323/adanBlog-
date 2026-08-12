package com.adan.blog.controller;

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

/** 文件上传（封面图） */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UploadController {

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
        // 按日期分目录存储
        String dateDir = LocalDate.now().toString();
        Path dir = Paths.get(uploadDir, dateDir);
        Files.createDirectories(dir);
        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = dir.resolve(filename);
        file.transferTo(target);
        String url = "/uploads/" + dateDir + "/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }
}
