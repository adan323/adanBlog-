# adanBlog

个人技术博客系统，前后端一体部署。基于 **Spring Boot 3 + Vue 3 + md-editor-v3**，单 jar 架构，内嵌 Tomcat 伺服三端（前台 / 后台 / API）。

## 技术栈

| 层 | 技术 | 说明 |
|---|---|---|
| 后端 | Spring Boot 3 + Spring MVC | 单 jar 可执行，内嵌 Tomcat，systemd 管理 |
| 数据库 | H2 文件模式 | 零独立进程，数据落盘 `/var/lib/adan-blog/blog` |
| 鉴权 | JWT | 后台接口 Bearer 鉴权，登录 7 天有效 |
| 前台前端 | Vue 3 + Vite + Tailwind CSS | 打包进 jar 静态目录（`/`） |
| 后台前端 | Vue 3 + Element Plus + md-editor-v3 | 打包进 jar 静态目录（`/admin/`） |
| Markdown | md-editor-v3 6.x | 前后台同一解析内核（MdEditor / MdPreview） |
| 图表 | Mermaid / ECharts / KaTeX / highlight.js | 扩展资源全部自托管 `vendor/`，不依赖 CDN |
| 音乐 | aplayer + Emby 音乐库 | 吸底播放器，Emby 做数据源（列表/音频流/封面代理） |

## 核心功能

### 博客前台
- 文章列表 / 详情 / 标签 / 归档 / 搜索 / 关于页，SPA 深链接 fallback
- **md-editor-v3 全功能渲染**：思维导图、流程图、时序图、甘特图、饼图（mermaid）、数据图表（echarts）、公式（katex）、代码高亮（highlight.js 82 主题）、admonition 提示框、任务列表、表格、上下标、脚注替代方案
- **动态数据图表**：文章里写 `{{数据源.字段}}` 占位符，渲染时替换为实时数据
  - `{{traffic.*}}`：博客访问量统计（每日 PV / 总览）
  - `{{weather.*}}`：IP 自动定位城市的一周天气（uapis.cn 免 key API，30 分钟缓存）
  - 数据源注册表驱动，新增数据源只需加一个 `load()` 函数（见 `blog/src/utils/editor-config.js`）
- **全局音乐播放器**：aplayer 吸底模式（默认收起窄条），Emby 音乐库 210 首，含歌词（同名 .lrc）与封面
- 封面图、阅读进度条、深色模式、响应式布局（PC 侧栏目录 / 移动端适配）
- mermaid 渲染竞态根治：骨架屏 + 预加载，就绪后才渲染 MdPreview

### 管理后台（`/admin/`）
- 文章 CRUD + **md-editor-v3 全功能编辑器**（44 种工具栏、图片裁剪上传、双全屏、prettier 格式化）
- 编辑器预览与前台渲染完全一致（同一解析内核 + 同一数据源替换逻辑）
- 标签管理、站点设置、访问统计

### 后端 API（`/api/**`）
- 公开：文章/标签/归档/搜索/设置/统计（`/api/public/**` 免鉴权）
- 管理：文章/标签/设置 CRUD + 上传（JWT 鉴权）
- 代理：Emby 音频流 / 封面（隐藏 api_key）、音乐列表、lrc 歌词

## 项目结构

```
adan-blog/
├── admin/                          # 管理后台前端 (Vue3 + Element Plus)
│   └── src/utils/editor-config.js  # md-editor-v3 扩展配置 + 数据源注册表
├── blog/                           # 博客前台前端 (Vue3 + Tailwind)
│   └── src/utils/editor-config.js  # 与 admin 一致，渲染时替换动态数据
└── system/                         # Spring Boot 后端
    ├── src/main/java/com/adan/blog/
    │   ├── config/                 # WebConfig(静态资源缓存) / SecurityConfig(JWT)
    │   ├── controller/             # Public / Admin / SpaController
    │   ├── service/                # 文章/标签/设置/访问日志/音乐(Emby代理)
    │   └── dto/ entity/ repository/ security/
    └── src/main/resources/         # application.properties + 静态资源(构建时注入)
```

## 构建与部署

```bash
# 前端构建（产物进 jar 静态目录）
cd admin && npm install && npm run build
cd blog  && npm install && npm run build

# 后端打包（Maven 会把 admin/dist、blog/dist 复制进 jar）
cd system && mvn clean package -DskipTests

# 部署（服务器）
scp system/target/adan-blog-1.0.0.jar root@host:/opt/adan-blog/
systemctl restart adan-blog
```

> 部署链：`mvn clean package` 会把两个前端 dist 打进 jar 的 `classpath:/static/`，所以**改前端后必须重新打包**，仅解压静态目录不生效（易踩坑，需 `clean` 防旧 hash 残留）。

## 配置（`application.properties`）

| 配置项 | 说明 |
|---|---|
| `adan.blog.upload-dir` | 图片上传目录 |
| `adan.blog.emby.base-url/api-key/user-id/music-lib-id/music-dir` | Emby 音乐库（播放器数据源） |
| `adan.blog.jwt.secret/expire-hours` | JWT 密钥与有效期 |

## 特点与设计取舍

- **单 jar 零运维**：内嵌 Tomcat，无 nginx 也无需外部容器，1G 小 VPS 无压力
- **CDN 全部本地化**：mermaid/katex/echarts/highlight/prettier/cropper/screenfull 全自托管，国内访问稳定
- **API key 不落前端**：Emby 等第三方密钥只存服务端，前端走代理接口
- **占位符双模式替换**：正文（text）与 echarts 代码块（json）分别处理引号，避免 `{{ }}` 语法崩溃

## License

个人项目，仅供学习交流。
