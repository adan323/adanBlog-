package com.adan.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 文章列表项（不含正文） */
@Data
public class ArticleSummary {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverUrl;
    private Long views;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> tags;
}
