package com.adan.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 访问日志 — 数据看板统计来源 */
@Data
@Entity
@Table(name = "visit_logs")
public class VisitLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 访问者 IP */
    @Column(length = 64)
    private String ip;

    /** IP 归属地（ip2region 解析，如 中国|广东|深圳） */
    @Column(length = 128)
    private String location;

    /** 浏览器（Chrome / Safari / Edge / Firefox ...） */
    @Column(length = 32)
    private String browser;

    /** 客户端平台（Windows / macOS / Android / iOS / Linux ...） */
    @Column(length = 32)
    private String platform;

    /** 访问的文章标题（冗余存一份方便查看） */
    @Column(length = 255)
    private String articleTitle;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
