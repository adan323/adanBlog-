package com.adan.blog.service;

import com.adan.blog.entity.Tag;
import com.adan.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listWithCounts() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : tagRepository.findWithCounts()) {
            Tag t = (Tag) row[0];
            long count = ((Number) row[1]).longValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", t.getId());
            m.put("name", t.getName());
            m.put("slug", t.getSlug());
            m.put("count", count);
            result.add(m);
        }
        return result;
    }

    /** 单个标签信息（含文章数），供标签文章页用，避免前端全量拉标签列表 */
    @Transactional(readOnly = true)
    public Map<String, Object> getBySlug(String slug) {
        Tag t = tagRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("name", t.getName());
        m.put("slug", t.getSlug());
        m.put("count", tagRepository.countArticlesBySlug(slug));
        return m;
    }
}
