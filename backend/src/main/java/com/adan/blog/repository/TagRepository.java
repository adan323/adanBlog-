package com.adan.blog.repository;

import com.adan.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findBySlug(String slug);

    @Query("SELECT t, COUNT(a) as cnt FROM Tag t LEFT JOIN t.articles a GROUP BY t ORDER BY cnt DESC")
    List<Object[]> findWithCounts();
}
