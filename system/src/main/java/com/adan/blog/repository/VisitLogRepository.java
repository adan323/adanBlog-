package com.adan.blog.repository;

import com.adan.blog.entity.VisitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitLogRepository extends JpaRepository<VisitLog, Long> {

    /** 近 N 天每日访问量（H2 用 CAST 转日期，DATE() 函数不存在） */
    @Query("SELECT CAST(v.createdAt AS date), COUNT(v) FROM VisitLog v WHERE v.createdAt >= :since GROUP BY CAST(v.createdAt AS date) ORDER BY CAST(v.createdAt AS date)")
    List<Object[]> countDaily(@Param("since") LocalDateTime since);

    /** IP 归属地 TOP N */
    @Query("SELECT v.location, COUNT(v) FROM VisitLog v WHERE v.createdAt >= :since AND v.location IS NOT NULL GROUP BY v.location ORDER BY COUNT(v) DESC")
    List<Object[]> countByLocation(@Param("since") LocalDateTime since);

    /** 浏览器分布 */
    @Query("SELECT v.browser, COUNT(v) FROM VisitLog v WHERE v.createdAt >= :since AND v.browser IS NOT NULL GROUP BY v.browser ORDER BY COUNT(v) DESC")
    List<Object[]> countByBrowser(@Param("since") LocalDateTime since);

    /** 平台分布 */
    @Query("SELECT v.platform, COUNT(v) FROM VisitLog v WHERE v.createdAt >= :since AND v.platform IS NOT NULL GROUP BY v.platform ORDER BY COUNT(v) DESC")
    List<Object[]> countByPlatform(@Param("since") LocalDateTime since);

    long countByCreatedAtAfter(LocalDateTime since);
}
