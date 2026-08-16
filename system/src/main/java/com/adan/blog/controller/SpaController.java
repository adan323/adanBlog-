package com.adan.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** SPA history 路由 fallback — 非 API 路径回 index.html */
@Controller
public class SpaController {

    /** 博客前台 SPA */
    @GetMapping({"/", "/post/**", "/archive", "/tags", "/tag/**", "/about"})
    public String forwardBlog() {
        return "forward:/index.html";
    }

    /** 管理后台 SPA（根路径 /admin、/admin/ 及无扩展名子路径回 admin/index.html） */
    @GetMapping({
        "/admin", "/admin/",
        "/admin/{path:[^\\\\.]*}",
        "/admin/{path:[^\\\\.]*}/{p2:[^\\\\.]*}",
        "/admin/{path:[^\\\\.]*}/{p2:[^\\\\.]*}/{p3:[^\\\\.]*}",
        "/admin/{path:[^\\\\.]*}/{p2:[^\\\\.]*}/{p3:[^\\\\.]*}/{p4:[^\\\\.]*}"
    })
    public String forwardAdmin() {
        return "forward:/admin/index.html";
    }
}
