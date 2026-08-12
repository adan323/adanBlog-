package com.adan.blog.controller;

import com.adan.blog.dto.ArticleDetail;
import com.adan.blog.dto.ArticleRequest;
import com.adan.blog.dto.ArticleSummary;
import com.adan.blog.service.ArticleService;
import com.adan.blog.service.SettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 管理 API — 需要 JWT */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ArticleService articleService;
    private final SettingService settingService;

    /** 文章列表（含草稿） */
    @GetMapping("/articles")
    public ResponseEntity<Page<ArticleSummary>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(articleService.listAdmin(page, size, status));
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<ArticleDetail> get(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.getAdminById(id));
    }

    @PostMapping("/articles")
    public ResponseEntity<ArticleDetail> create(@Valid @RequestBody ArticleRequest req) {
        return ResponseEntity.ok(articleService.create(req));
    }

    @PutMapping("/articles/{id}")
    public ResponseEntity<ArticleDetail> update(@PathVariable Long id, @Valid @RequestBody ArticleRequest req) {
        return ResponseEntity.ok(articleService.update(id, req));
    }

    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(articleService.stats());
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> getSettings() {
        return ResponseEntity.ok(settingService.getPublicSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, String> settings) {
        settingService.updateSettings(settings);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
