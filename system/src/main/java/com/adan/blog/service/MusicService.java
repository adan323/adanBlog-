package com.adan.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 音乐文件服务：扫描配置的音乐目录，返回 aplayer 可用的歌曲列表。
 * 支持 mp3/flac/m4a/wav/ogg，自动配对同名 .lrc 歌词。
 * 音频文件通过 /music/** 静态映射直接提供（见 WebConfig）。
 */
@Service
@RequiredArgsConstructor
public class MusicService {

    private static final Set<String> AUDIO_EXT = Set.of("mp3", "flac", "m4a", "wav", "ogg");

    @Value("${adan.blog.music-dir:/root/Music}")
    private String musicDir;

    /** 扫描目录返回歌曲列表（按文件名排序）。每个条目：
     * {title, artist, url, lrc, cover(null)} */
    public List<Map<String, Object>> list() {
        File dir = new File(musicDir);
        if (!dir.isDirectory()) return List.of();

        File[] files = dir.listFiles();
        if (files == null) return List.of();

        // 音频文件按文件名排序（aplayer 列表顺序稳定）
        List<File> audios = new ArrayList<>();
        for (File f : files) {
            if (!f.isFile()) continue;
            String ext = extOf(f.getName());
            if (AUDIO_EXT.contains(ext)) audios.add(f);
        }
        audios.sort(Comparator.comparing(File::getName));

        // 歌词按 base 名索引（便于配对）
        Map<String, File> lrcMap = new HashMap<>();
        for (File f : files) {
            if (!f.isFile()) continue;
            if ("lrc".equals(extOf(f.getName()))) {
                lrcMap.put(baseOf(f.getName()), f);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (File audio : audios) {
            String base = baseOf(audio.getName());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", titleOf(base));
            item.put("artist", artistOf(base));
            item.put("url", "/music/" + urlEncode(audio.getName()));
            File lrc = lrcMap.get(base);
            item.put("lrc", lrc != null ? "/music/" + urlEncode(lrc.getName()) : "");
            result.add(item);
        }
        return result;
    }

    /** 去掉扩展名的文件名：`01-01-TheFatRat-Monody-LLS` */
    private String baseOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }

    /** 扩展名（小写） */
    private String extOf(String name) {
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(dot + 1).toLowerCase() : "";
    }

    /** 标题：去掉歌手前缀。约定 `歌手-歌名` 或 `01-01-歌手-歌名`，取最后一段 */
    private String titleOf(String base) {
        String[] parts = base.split("-");
        return parts.length > 0 ? parts[parts.length - 1].trim() : base;
    }

    /** 歌手：标题前一段（若存在） */
    private String artistOf(String base) {
        String[] parts = base.split("-");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim();
        }
        return "";
    }

    private String urlEncode(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
