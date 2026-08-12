package com.adan.blog.config;

import com.adan.blog.entity.AdminUser;
import com.adan.blog.entity.Article;
import com.adan.blog.entity.Setting;
import com.adan.blog.entity.Tag;
import com.adan.blog.repository.AdminUserRepository;
import com.adan.blog.repository.ArticleRepository;
import com.adan.blog.repository.SettingRepository;
import com.adan.blog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** 首次启动种子数据：管理员账号 + 站点设置 + 示例文章 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final SettingRepository settingRepository;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 管理员：admin / Adan@Blog2026 (首次登录后建议修改)
        if (adminUserRepository.count() == 0) {
            AdminUser admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("Adan@Blog2026"));
            admin.setCreatedAt(LocalDateTime.now());
            adminUserRepository.save(admin);
            log.info("已创建管理员账号: admin");
        }

        // 站点设置
        if (settingRepository.count() == 0) {
            settingRepository.save(new Setting("site_title", "adan 的博客"));
            settingRepository.save(new Setting("site_subtitle", "记录代码与生活的碎片"));
            settingRepository.save(new Setting("site_description", "一个关于技术、设计与生活的个人博客"));
            settingRepository.save(new Setting("author_name", "adan"));
            settingRepository.save(new Setting("author_bio", "写代码的人，偶尔写点别的。"));
            settingRepository.save(new Setting("author_avatar", ""));
            settingRepository.save(new Setting("github_url", "https://github.com"));
            settingRepository.save(new Setting("twitter_url", ""));
            settingRepository.save(new Setting("email", "contact@adan.ltd"));
            settingRepository.save(new Setting("icp", ""));
            log.info("已初始化站点设置");
        }

        // 示例文章（仅当一篇都没有时）
        if (articleRepository.count() == 0) {
            createSampleArticle("欢迎来到我的博客",
                    "welcome",
                    "这是博客系统的第一篇文章，介绍一下这个站点和它的技术栈。",
                    "### 关于这个博客\n\n这是我的个人博客，使用以下技术构建：\n\n- **后端**：Java 17 + Spring Boot 3.3\n- **前端**：Vue 3 + Vite + Tailwind CSS\n- **数据库**：H2 嵌入式（文件模式）\n\n```java\n@SpringBootApplication\npublic class AdanBlogApplication {\n    public static void main(String[] args) {\n        SpringApplication.run(AdanBlogApplication.class, args);\n    }\n}\n```\n\n> 简约而不简单。\n\n支持 **Markdown 语法**、代码高亮和数学公式：\n\n$$E = mc^2$$\n\n欢迎常来。",
                    List.of("随笔", "技术"), "/uploads/demo/mountain.jpg", 3);

            createSampleArticle("Vue 3 组合式 API 实战笔记",
                    "vue3-composition-api",
                    "记录 Vue 3 组合式 API 的核心概念与常见实战模式。",
                    "## 为什么用组合式 API\n\n组合式 API 让逻辑复用变得更简单，把相关代码聚在一起。\n\n### 核心 API\n\n| API | 用途 |\n|---|---|\n| `ref` | 响应式基础类型 |\n| `reactive` | 响应式对象 |\n| `computed` | 计算属性 |\n| `watch` | 侦听变化 |\n\n```js\nimport { ref, computed } from 'vue'\n\nconst count = ref(0)\nconst double = computed(() => count.value * 2)\n```\n\n## 实战建议\n\n1. 按功能拆分 composables\n2. 用 `defineProps` 定义 props\n3. 使用 `<script setup>` 语法糖",
                    List.of("技术", "前端"), "/uploads/demo/code.jpg", 5);

            createSampleArticle("春天里的十件小事",
                    "spring-small-things",
                    "一些关于春天的碎碎念，适合在午后读。",
                    "## 一、晒太阳\n\n午后的阳光穿过窗帘，在木地板上留下长方形的光斑。\n\n## 二、听雨\n\n春雨敲在窗台上，滴滴答答，像一首没有歌词的歌。\n\n## 三、散步\n\n沿着河堤走，柳树发了新芽，空气里是泥土和青草的味道。\n\n---\n\n> 生活不是赶路，是散步。",
                    List.of("随笔", "生活"), "/uploads/demo/spring.jpg", 7);
            log.info("已创建 3 篇示例文章");
        }
    }

    private void createSampleArticle(String title, String slug, String summary, String content,
                                     List<String> tagNames, String coverUrl, int views) {
        Article a = new Article();
        a.setTitle(title);
        a.setSlug(slug);
        a.setSummary(summary);
        a.setContent(content);
        a.setStatus("published");
        a.setViews((long) views);
        a.setCoverUrl(coverUrl);
        for (String name : tagNames) {
            Tag tag = tagRepository.findByName(name).orElseGet(() -> {
                Tag t = new Tag();
                t.setName(name);
                t.setSlug(name);
                return tagRepository.save(t);
            });
            a.getTags().add(tag);
        }
        articleRepository.save(a);
    }
}
