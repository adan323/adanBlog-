package com.adan.blog.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 文章详情 */
@Data
public class ArticleDetail extends ArticleSummary {
    private String content;
    private Long prevId;
    private String prevTitle;
    private String prevSlug;
    private Long nextId;
    private String nextTitle;
    private String nextSlug;
}
