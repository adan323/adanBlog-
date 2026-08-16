package com.adan.blog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * 音乐服务：以 Emby 音乐库为数据源（列表/音频流/封面），歌词读同名 .lrc 文件。
 * - Emby API 走服务端调用，api_key 不暴露给浏览器
 * - 音频用 /Audio/{id}/stream.mp3（Emby 转码为浏览器通用格式）
 * - 列表项：{id, title, artist, album, url, lrc, cover}
 */
@Slf4j
@Service
public class MusicService {

    @Value("${adan.blog.emby.base-url:http://adan.ltd:8096}")
    private String embyBase;

    @Value("${adan.blog.emby.api-key:}")
    private String embyKey;

    @Value("${adan.blog.emby.user-id:}")
    private String embyUserId;

    @Value("${adan.blog.emby.music-lib-id:8607}")
    private String musicLibId;

    @Value("${adan.blog.emby.music-dir:/root/song}")
    private String musicDir;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** Emby GET 返回 JSON */
    private Map<String, Object> embyGet(String path) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(embyBase + path + (path.contains("?") ? "&" : "?") + "api_key=" + embyKey))
                .header("X-Emby-Token", embyKey)
                .timeout(java.time.Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new RuntimeException("emby " + resp.statusCode());
        }
        return new com.fasterxml.jackson.databind.ObjectMapper().readValue(resp.body(), Map.class);
    }

    /** 歌曲列表（Emby 音乐库） */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> list() {
        try {
            Map<String, Object> data = embyGet("/Users/" + embyUserId + "/Items?ParentId=" + musicLibId
                    + "&Recursive=true&IncludeItemTypes=Audio&Fields=MediaSources,ImageTags,Artists,Album,Path&Limit=500");
            List<Map<String, Object>> items = (List<Map<String, Object>>) data.getOrDefault("Items", List.of());
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> it : items) {
                String id = String.valueOf(it.get("Id"));
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", id);
                item.put("title", it.getOrDefault("Name", "未知歌曲"));
                item.put("artist", join(it.get("Artists")));
                item.put("album", it.getOrDefault("Album", ""));
                item.put("url", "/api/public/music/audio/" + id);
                // 歌词：Emby 内嵌 lrc 无独立接口，读同名 .lrc 文件
                String path = String.valueOf(it.getOrDefault("Path", ""));
                String lrcPath = lrcPathFor(path);
                item.put("lrc", lrcPath != null ? "/api/public/music/lrc/" + id : "");
                // 封面：Emby 图片接口
                Map<String, Object> imgTags = (Map<String, Object>) it.getOrDefault("ImageTags", Map.of());
                item.put("cover", imgTags.containsKey("Primary") ? "/api/public/music/cover/" + id : "");
                result.add(item);
            }
            return result;
        } catch (Exception e) {
            log.warn("emby music list failed: {}", e.getMessage());
            return List.of();
        }
    }

    /** 音频流：转发 Emby 转码流（mp3，浏览器通用） */
    public byte[] audio(String id) {
        return proxyBytes("/Audio/" + id + "/stream.mp3?static=true");
    }

    /** 封面图：转发 Emby 图片 */
    public byte[] cover(String id) {
        return proxyBytes("/Items/" + id + "/Images/Primary?maxWidth=400&quality=90");
    }

    /** 歌词：读同名 .lrc 文件（UTF-8 文本） */
    public byte[] lrc(String id) {
        try {
            Map<String, Object> data = embyGet("/Users/" + embyUserId + "/Items/" + id + "?Fields=Path");
            String path = String.valueOf(data.getOrDefault("Path", ""));
            String lrcPath = lrcPathFor(path);
            if (lrcPath != null) {
                File f = new File(lrcPath);
                if (f.isFile()) return Files.readAllBytes(f.toPath());
            }
        } catch (Exception e) {
            log.warn("emby lrc failed: {}", e.getMessage());
        }
        return new byte[0];
    }

    /** 通用字节代理（Emby 流/图片） */
    private byte[] proxyBytes(String path) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(embyBase + path + (path.contains("?") ? "&" : "?") + "api_key=" + embyKey))
                    .header("X-Emby-Token", embyKey)
                    .timeout(java.time.Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            return resp.statusCode() == 200 ? resp.body() : new byte[0];
        } catch (Exception e) {
            log.warn("emby proxy failed: {}", e.getMessage());
            return new byte[0];
        }
    }

    /** 从 Emby Path（/root/song/歌名-歌手.flac）推导同名 .lrc 路径 */
    private String lrcPathFor(String path) {
        if (path == null || path.isBlank()) return null;
        String name = path.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        String base = slash >= 0 ? name.substring(slash + 1) : name;
        int dot = base.lastIndexOf('.');
        String stem = dot > 0 ? base.substring(0, dot) : base;
        File f = new File(musicDir, stem + ".lrc");
        return f.isFile() ? f.getAbsolutePath() : null;
    }

    /** 歌手数组 → "a / b" */
    @SuppressWarnings("unchecked")
    private String join(Object artists) {
        if (!(artists instanceof List)) return "";
        List<String> list = (List<String>) artists;
        return String.join(" / ", list);
    }
}
