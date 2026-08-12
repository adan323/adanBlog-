package com.adan.blog.service;

import com.adan.blog.entity.Tag;
import com.adan.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

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
}
