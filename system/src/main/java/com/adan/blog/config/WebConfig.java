package com.adan.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

/**
 * 静态资源缓存策略：
 * - index.html / admin/index.html：不缓存（SPA 更新后用户能立即拿到新版）
 * - 带 hash 的 assets/*：长缓存（7 天），文件名变则重新拉取
 * - uploads/*：短缓存（1 小时）
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/index.html", "/admin/index.html")
                .addResourceLocations("classpath:/static/", "classpath:/static/admin/")
                .setCacheControl(CacheControl.noCache());

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/")
                .setCacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic());

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("classpath:/static/uploads/", "file:/var/lib/adan-blog/uploads/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)));
    }
}
