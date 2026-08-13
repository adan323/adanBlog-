package com.adan.blog.repository;

import com.adan.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Optional<Article> findBySlug(String slug);

    Page<Article> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    Page<Article> findByStatusAndTagsSlugOrderByCreatedAtDesc(String status, String tagSlug, Pageable pageable);

    List<Article> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE a.status = :status AND t.slug IN :tagSlugs GROUP BY a.id ORDER BY a.createdAt DESC")
    List<Article> findByStatusAndTagSlugs(@Param("status") String status, @Param("tagSlugs") List<String> tagSlugs);

    @Query("SELECT a FROM Article a WHERE a.status = 'published' AND a.title LIKE %:kw% ORDER BY a.createdAt DESC")
    List<Article> searchByKeyword(@Param("kw") String keyword);

    /** 归档专用轻量投影：只取列表需要的字段，避免把 CLOB 正文拉进内存（大数据量关键） */
    @Query("SELECT a.id, a.title, a.slug, a.createdAt, a.views FROM Article a WHERE a.status = 'published' ORDER BY a.createdAt DESC")
    List<Object[]> findArchiveProjection();

    @Query("SELECT COUNT(a) FROM Article a WHERE a.status = 'published'")
    long countPublished();

    @Query("SELECT COALESCE(SUM(a.views), 0) FROM Article a WHERE a.status = 'published'")
    long sumViews();

    @Query("SELECT a FROM Article a WHERE a.status = 'published' AND a.createdAt < :before ORDER BY a.createdAt DESC")
    List<Article> findPrev(@Param("before") LocalDateTime before, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.status = 'published' AND a.createdAt > :after ORDER BY a.createdAt ASC")
    List<Article> findNext(@Param("after") LocalDateTime after, Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(@Param("id") Long id);
}
