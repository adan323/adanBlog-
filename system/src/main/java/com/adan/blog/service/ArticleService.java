package com.adan.blog.service;

import com.adan.blog.dto.ArticleDetail;
import com.adan.blog.dto.ArticleRequest;
import com.adan.blog.dto.ArticleSummary;
import com.adan.blog.entity.Article;
import com.adan.blog.entity.Tag;
import com.adan.blog.repository.ArticleRepository;
import com.adan.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;

    /** 公开文章分页（按时间倒序） */
    @Transactional(readOnly = true)
    public Page<ArticleSummary> listPublished(int page, int size, String tagSlug) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Article> result = (tagSlug == null || tagSlug.isBlank())
                ? articleRepository.findByStatusOrderByCreatedAtDesc("published", pageable)
                : articleRepository.findByStatusAndTagsSlugOrderByCreatedAtDesc("published", tagSlug, pageable);
        return result.map(this::toSummary);
    }

    /** 管理端文章分页（含草稿） */
    @Transactional(readOnly = true)
    public Page<ArticleSummary> listAdmin(int page, int size, String status) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Article> result = (status == null || status.isBlank())
                ? articleRepository.findAll(pageable)
                : articleRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return result.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public ArticleDetail getBySlug(String slug, boolean incrementView) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        if (!"published".equals(article.getStatus())) {
            throw new RuntimeException("文章不存在");
        }
        if (incrementView) {
            articleRepository.incrementViews(article.getId());
            article.setViews(article.getViews() + 1);
        }
        return toDetail(article);
    }

    @Transactional(readOnly = true)
    public ArticleDetail getAdminById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        return toDetail(article);
    }

    @Transactional
    public ArticleDetail create(ArticleRequest req) {
        Article article = new Article();
        applyRequest(article, req);
        article = articleRepository.save(article);
        return toDetail(article);
    }

    @Transactional
    public ArticleDetail update(Long id, ArticleRequest req) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
        applyRequest(article, req);
        article = articleRepository.save(article);
        return toDetail(article);
    }

    @Transactional
    public void delete(Long id) {
        articleRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ArticleSummary> search(String keyword) {
        return articleRepository.searchByKeyword(keyword).stream()
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    /** 归档：一次返回完整归档树（年 > 月 > 文章轻量列表），单次投影查询 + 内存分组，
     *  不翻页、不拉正文，文章量再大也只有一个查询 */
    public Map<String, Object> archive() {
        List<Object[]> rows = articleRepository.findArchiveProjection();
        Map<Integer, Map<Integer, List<Map<String, Object>>>> yearMap =
                new TreeMap<>(Collections.reverseOrder());
        for (Object[] row : rows) {
            Long id = (Long) row[0];
            String title = (String) row[1];
            String slug = (String) row[2];
            LocalDateTime createdAt = (LocalDateTime) row[3];
            Long views = (Long) row[4];
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", id);
            p.put("title", title);
            p.put("slug", slug);
            p.put("day", createdAt.getDayOfMonth());
            p.put("views", views);
            yearMap.computeIfAbsent(createdAt.getYear(), k -> new TreeMap<>(Collections.reverseOrder()))
                    .computeIfAbsent(createdAt.getMonthValue(), k -> new ArrayList<>())
                    .add(p);
        }
        List<Map<String, Object>> years = new ArrayList<>();
        for (Map.Entry<Integer, Map<Integer, List<Map<String, Object>>>> ye : yearMap.entrySet()) {
            Map<String, Object> yMap = new LinkedHashMap<>();
            yMap.put("year", ye.getKey());
            List<Map<String, Object>> months = new ArrayList<>();
            int yearTotal = 0;
            for (Map.Entry<Integer, List<Map<String, Object>>> me : ye.getValue().entrySet()) {
                Map<String, Object> mMap = new LinkedHashMap<>();
                mMap.put("month", me.getKey());
                mMap.put("total", me.getValue().size());
                mMap.put("posts", me.getValue());
                months.add(mMap);
                yearTotal += me.getValue().size();
            }
            yMap.put("months", months);
            yMap.put("total", yearTotal);
            years.add(yMap);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", rows.size());
        result.put("years", years);
        return result;
    }

    public Map<String, Object> stats() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("total", articleRepository.count());
        s.put("published", articleRepository.countPublished());
        s.put("views", articleRepository.sumViews());
        s.put("tags", tagRepository.count());
        return s;
    }

    private void applyRequest(Article article, ArticleRequest req) {
        article.setTitle(req.getTitle());
        article.setSlug(generateSlug(req.getSlug(), req.getTitle(), article.getId()));
        article.setSummary(req.getSummary());
        article.setContent(req.getContent());
        article.setCoverUrl(req.getCoverUrl());
        if (req.getStatus() != null && (req.getStatus().equals("published") || req.getStatus().equals("draft"))) {
            article.setStatus(req.getStatus());
        }
        // 处理标签
        Set<Tag> tags = new HashSet<>();
        if (req.getTags() != null) {
            for (String name : req.getTags()) {
                if (name == null || name.isBlank()) continue;
                String trimmed = name.trim();
                Tag tag = tagRepository.findByName(trimmed).orElseGet(() -> {
                    Tag t = new Tag();
                    t.setName(trimmed);
                    t.setSlug(slugify(trimmed));
                    return tagRepository.save(t);
                });
                tags.add(tag);
            }
        }
        article.setTags(tags);
    }

    private String generateSlug(String provided, String title, Long id) {
        String base;
        if (provided != null && !provided.isBlank()) {
            base = slugify(provided);
        } else {
            base = slugify(title);
        }
        if (base.isBlank()) base = "post-" + System.currentTimeMillis();
        String candidate = base;
        int n = 1;
        while (true) {
            Optional<Article> existing = articleRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(id)) {
                return candidate;
            }
            candidate = base + "-" + (++n);
        }
    }

    public static String slugify(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .trim();
        // 保留中文、字母数字，其他转 -
        String slug = normalized.replaceAll("[^\\p{IsHan}a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.length() > 190 ? slug.substring(0, 190) : slug;
    }

    private ArticleSummary toSummary(Article a) {
        ArticleSummary s = new ArticleSummary();
        s.setId(a.getId());
        s.setTitle(a.getTitle());
        s.setSlug(a.getSlug());
        s.setSummary(a.getSummary());
        s.setCoverUrl(a.getCoverUrl());
        s.setViews(a.getViews());
        s.setCreatedAt(a.getCreatedAt());
        s.setUpdatedAt(a.getUpdatedAt());
        s.setTags(a.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList()));
        return s;
    }

    private ArticleDetail toDetail(Article a) {
        ArticleDetail d = new ArticleDetail();
        d.setId(a.getId());
        d.setTitle(a.getTitle());
        d.setSlug(a.getSlug());
        d.setSummary(a.getSummary());
        d.setContent(a.getContent());
        d.setCoverUrl(a.getCoverUrl());
        d.setViews(a.getViews());
        d.setCreatedAt(a.getCreatedAt());
        d.setUpdatedAt(a.getUpdatedAt());
        d.setTags(a.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList()));
        // 上一篇 / 下一篇
        Pageable one = PageRequest.of(0, 1);
        List<Article> prev = articleRepository.findPrev(a.getCreatedAt(), one);
        List<Article> next = articleRepository.findNext(a.getCreatedAt(), one);
        if (!prev.isEmpty()) {
            d.setPrevId(prev.get(0).getId());
            d.setPrevTitle(prev.get(0).getTitle());
            d.setPrevSlug(prev.get(0).getSlug());
        }
        if (!next.isEmpty()) {
            d.setNextId(next.get(0).getId());
            d.setNextTitle(next.get(0).getTitle());
            d.setNextSlug(next.get(0).getSlug());
        }
        return d;
    }
}
