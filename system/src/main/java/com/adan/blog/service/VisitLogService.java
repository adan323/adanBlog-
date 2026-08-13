package com.adan.blog.service;

import com.adan.blog.entity.VisitLog;
import com.adan.blog.repository.VisitLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 访问日志服务：记录每次文章访问的 IP/归属地/浏览器/平台，并提供数据看板统计。
 * IP 归属用 ip2region 离线库（xdb 文件），浏览器/平台用轻量 UA 解析。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitLogService {

    private final VisitLogRepository visitLogRepository;

    @Value("${blog.ip2region-path:/var/lib/adan-blog/ip2region.xdb}")
    private String ip2regionPath;

    // ip2region Searcher（懒加载，xdb 不存在时降级为 unknown）
    private volatile Object searcher; // org.lionsoul.ip2region.Searcher
    private volatile boolean searcherReady = false;
    private volatile boolean searcherFailed = false;

    /** 记录一次访问（前端文章详情被浏览时调用） */
    public void record(String ip, String userAgent, String articleTitle) {
        try {
            VisitLog v = new VisitLog();
            v.setIp(truncate(ip, 64));
            v.setLocation(resolveLocation(ip));
            v.setBrowser(parseBrowser(userAgent));
            v.setPlatform(parsePlatform(userAgent));
            v.setArticleTitle(truncate(articleTitle, 255));
            visitLogRepository.save(v);
        } catch (Exception e) {
            log.warn("记录访问日志失败: {}", e.getMessage());
        }
    }

    // ===== 数据看板统计 =====

    /** 总览：今日访问、近7日、总访问 */
    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        m.put("today", visitLogRepository.countByCreatedAtAfter(todayStart));
        m.put("week", visitLogRepository.countByCreatedAtAfter(weekStart));
        m.put("total", visitLogRepository.count());
        return m;
    }

    /** 近 N 天每日访问量（补全无数据的天） */
    public List<Map<String, Object>> daily(int days) {
        int n = Math.min(Math.max(days, 7), 90);
        LocalDateTime since = LocalDate.now().minusDays(n - 1).atStartOfDay();
        Map<LocalDate, Long> map = new HashMap<>();
        for (Object[] row : visitLogRepository.countDaily(since)) {
            Object d = row[0];
            LocalDate date = (d instanceof LocalDate) ? (LocalDate) d
                    : ((java.sql.Date) d).toLocalDate();
            map.put(date, (Long) row[1]);
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.toString());
            item.put("pv", map.getOrDefault(date, 0L));
            list.add(item);
        }
        return list;
    }

    /** IP 归属地分布 TOP 10 */
    public List<Map<String, Object>> locations(int days) {
        return toNameCount(visitLogRepository.countByLocation(since(days)), 10);
    }

    /** 浏览器分布 */
    public List<Map<String, Object>> browsers(int days) {
        return toNameCount(visitLogRepository.countByBrowser(since(days)), 10);
    }

    /** 客户端平台分布 */
    public List<Map<String, Object>> platforms(int days) {
        return toNameCount(visitLogRepository.countByPlatform(since(days)), 10);
    }

    private LocalDateTime since(int days) {
        return LocalDate.now().minusDays(Math.min(Math.max(days, 1), 90)).atStartOfDay();
    }

    private List<Map<String, Object>> toNameCount(List<Object[]> rows, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < rows.size() && i < limit; i++) {
            Object[] row = rows.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", row[0] == null ? "未知" : row[0].toString());
            item.put("value", row[1]);
            list.add(item);
        }
        return list;
    }

    // ===== UA 解析（轻量，零依赖） =====

    String parseBrowser(String ua) {
        if (ua == null || ua.isBlank()) return "未知";
        String s = ua.toLowerCase();
        // 注意顺序：Opera/Edge/微信 的 UA 里都含 "chrome"，必须优先匹配
        if (s.contains("micromessenger")) return "微信";
        if (s.contains("opr/") || s.contains("opera")) return "Opera";
        if (s.contains("edg/")) return "Edge";
        if (s.contains("chrome/")) return "Chrome";
        if (s.contains("firefox/")) return "Firefox";
        if (s.contains("safari/")) return "Safari";
        if (s.contains("trident/") || s.contains("msie")) return "IE";
        return "其他";
    }

    String parsePlatform(String ua) {
        if (ua == null || ua.isBlank()) return "未知";
        String s = ua.toLowerCase();
        if (s.contains("android")) return "Android";
        if (s.contains("iphone") || s.contains("ipad") || s.contains("ipod")) return "iOS";
        if (s.contains("windows")) return "Windows";
        if (s.contains("mac os") || s.contains("macintosh")) return "macOS";
        if (s.contains("linux")) return "Linux";
        if (s.contains("bot") || s.contains("spider") || s.contains("crawler")) return "爬虫";
        return "其他";
    }

    // ===== ip2region 归属解析 =====

    private String resolveLocation(String ip) {
        if (ip == null || ip.isBlank()) return "未知";
        try {
            Object s = getSearcher();
            if (s == null) return "未知";
            // 反射调用 searcher.search(ip)
            java.lang.reflect.Method m = s.getClass().getMethod("search", String.class);
            Object result = m.invoke(s, ip);
            if (result == null) return "未知";
            String[] parts = result.toString().split("\\|");
            // 格式: 国家|区域|省份|城市|ISP
            StringBuilder sb = new StringBuilder();
            if (parts.length > 2 && !"0".equals(parts[2]) && !parts[2].isBlank()) {
                sb.append(parts[2]); // 省份
            }
            if (parts.length > 3 && !"0".equals(parts[3]) && !parts[3].isBlank()) {
                sb.append(" ").append(parts[3]); // 城市
            }
            if (sb.length() == 0 && parts.length > 0 && !"0".equals(parts[0])) {
                sb.append(parts[0]); // 国家兜底
            }
            return sb.length() == 0 ? "未知" : sb.toString().trim();
        } catch (Exception e) {
            return "未知";
        }
    }

    private Object getSearcher() {
        if (searcherReady) return searcher;
        if (searcherFailed) return null;
        synchronized (this) {
            if (searcherReady) return searcher;
            if (searcherFailed) return null;
            try {
                File f = new File(ip2regionPath);
                if (!f.exists()) {
                    log.warn("ip2region.xdb 不存在({})，IP 归属将显示未知", ip2regionPath);
                    searcherFailed = true;
                    return null;
                }
                byte[] data = Files.readAllBytes(f.toPath());
                Class<?> searcherClass = Class.forName("org.lionsoul.ip2region.xdb.Searcher");
                java.lang.reflect.Method newWithBuffer =
                        searcherClass.getMethod("newWithBuffer", byte[].class);
                searcher = newWithBuffer.invoke(null, (Object) data);
                searcherReady = true;
                log.info("ip2region 加载成功: {} bytes", data.length);
                return searcher;
            } catch (Exception e) {
                log.warn("ip2region 初始化失败: {}", e.getMessage());
                searcherFailed = true;
                return null;
            }
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
