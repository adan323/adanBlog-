package com.adan.blog.controller;

import com.adan.blog.dto.ArticleDetail;
import com.adan.blog.dto.ArticleSummary;
import com.adan.blog.service.ArticleService;
import com.adan.blog.service.MusicService;
import com.adan.blog.service.SettingService;
import com.adan.blog.service.TagService;
import com.adan.blog.service.VisitLogService;
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
    private final VisitLogService visitLogService;
    private final MusicService musicService;

    /** 文章列表（分页） */
    @GetMapping("/articles")
    public ResponseEntity<Page<ArticleSummary>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String tag) {
        return ResponseEntity.ok(articleService.listPublished(page, size, tag));
    }

    /** 文章详情（公开，自动 +1 阅读量，并记录访问日志） */
    @GetMapping("/articles/{slug}")
    public ResponseEntity<ArticleDetail> detail(@PathVariable String slug,
                                                @RequestHeader(value = "User-Agent", required = false) String userAgent,
                                                @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
                                                jakarta.servlet.http.HttpServletRequest request) {
        ArticleDetail d = articleService.getBySlug(slug, true);
        // 异步记录访问日志（不阻塞响应）
        String ip = resolveClientIp(forwardedFor, request);
        visitLogService.record(ip, userAgent, d.getTitle());
        return ResponseEntity.ok(d);
    }

    /** 解析真实客户端 IP：优先 X-Forwarded-For（nginx/反代场景），取第一个 */
    private String resolveClientIp(String forwardedFor, jakarta.servlet.http.HttpServletRequest request) {
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            if (!first.isBlank() && !"unknown".equalsIgnoreCase(first)) {
                return first;
            }
        }
        return request.getRemoteAddr();
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

    /** 单个标签信息（带文章数） */
    @GetMapping("/tags/{slug}")
    public ResponseEntity<Map<String, Object>> tag(@PathVariable String slug) {
        return ResponseEntity.ok(tagService.getBySlug(slug));
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

    /**
     * 公开访问趋势（供文章内动态 ECharts 图表用）：
     * 只返回聚合数据（每日 PV + 总览），不含 IP/UA 等明细，可安全公开。
     * 用法：文章 echarts 代码块里写 {{traffic.days}} / {{traffic.views}}
     * 占位符，前端 echartsConfig 钩子拉取本接口后替换。
     */
    @GetMapping("/public/stats/traffic")
    public ResponseEntity<Map<String, Object>> publicTraffic(
            @RequestParam(defaultValue = "14") int days) {
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        List<Map<String, Object>> daily = visitLogService.daily(days);
        result.put("days", daily.stream().map(d -> d.get("date")).toList());
        result.put("views", daily.stream().map(d -> d.get("pv")).toList());
        result.put("overview", visitLogService.overview());
        return ResponseEntity.ok(result);
    }

    /** 音乐列表（aplayer 播放器数据源）：Emby 音乐库，返回 [{id,title,artist,album,url,lrc,cover}] */
    @GetMapping("/public/music")
    public ResponseEntity<List<Map<String, Object>>> music() {
        return ResponseEntity.ok(musicService.list());
    }

    /** 音频流代理：转发 Emby 转码流（mp3），隐藏 api_key */
    @GetMapping(value = "/public/music/audio/{id}", produces = "audio/mpeg")
    public ResponseEntity<byte[]> musicAudio(@PathVariable String id) {
        byte[] body = musicService.audio(id);
        if (body.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=3600")
                .body(body);
    }

    /** 封面代理：转发 Emby 图片 */
    @GetMapping(value = "/public/music/cover/{id}", produces = {"image/jpeg", "image/png"})
    public ResponseEntity<byte[]> musicCover(@PathVariable String id) {
        byte[] body = musicService.cover(id);
        if (body.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=86400")
                .body(body);
    }

    /** 歌词：读同名 .lrc 文件 */
    @GetMapping(value = "/public/music/lrc/{id}", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<byte[]> musicLrc(@PathVariable String id) {
        byte[] body = musicService.lrc(id);
        if (body.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=86400")
                .body(body);
    }
}
