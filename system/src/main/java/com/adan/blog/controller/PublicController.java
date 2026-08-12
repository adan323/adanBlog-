package com.adan.blog.controller;

import com.adan.blog.dto.ArticleDetail;
import com.adan.blog.dto.ArticleSummary;
import com.adan.blog.service.ArticleService;
import com.adan.blog.service.SettingService;
import com.adan.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 公开 API — 博客前台 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicController {

    private final ArticleService articleService;
    private final TagService tagService;
    private final SettingService settingService;

    /** 文章列表（分页） */
    @GetMapping("/articles")
    public ResponseEntity<Page<ArticleSummary>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(articleService.listPublished(page, size, tag));
    }

    /** 文章详情（公开，自动 +1 阅读量） */
    @GetMapping("/articles/{slug}")
    public ResponseEntity<ArticleDetail> detail(@PathVariable String slug) {
        return ResponseEntity.ok(articleService.getBySlug(slug, true));
    }

    /** 搜索 */
    @GetMapping("/search")
    public ResponseEntity<List<ArticleSummary>> search(@RequestParam String q) {
        return ResponseEntity.ok(articleService.search(q));
    }

    /** 归档（按年/月统计） */
    @GetMapping("/archive")
    public ResponseEntity<Map<String, Object>> archive() {
        return ResponseEntity.ok(articleService.archive());
    }

    /** 标签（带文章数） */
    @GetMapping("/tags")
    public ResponseEntity<List<Map<String, Object>>> tags() {
        return ResponseEntity.ok(tagService.listWithCounts());
    }

    /** 站点公开设置 */
    @GetMapping("/settings")
    public ResponseEntity<Map<String, String>> settings() {
        return ResponseEntity.ok(settingService.getPublicSettings());
    }

    /** 站点统计（页脚展示） */
    @GetMapping("/public/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(articleService.stats());
    }
}
