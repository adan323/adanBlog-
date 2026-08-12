package com.adan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/** 文章创建/更新请求 */
@Data
public class ArticleRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    private String slug;

    private String summary;

    private String content;

    private String coverUrl;

    /** draft / published */
    private String status = "draft";

    private List<String> tags;
}
