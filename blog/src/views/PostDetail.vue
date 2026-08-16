<template>
  <div class="container-blog fade-in-page">
    <div v-if="loading" class="py-16">
      <div class="skeleton h-8 w-2/3 mb-6 rounded"></div>
      <div class="skeleton h-4 w-full mb-3 rounded"></div>
      <div class="skeleton h-4 w-full mb-3 rounded"></div>
      <div class="skeleton h-4 w-3/4 rounded"></div>
    </div>

    <div v-else-if="error" class="py-24 text-center">
      <div class="text-5xl mb-5">🔍</div>
      <p class="text-slate-400 mb-6">{{ error }}</p>
      <RouterLink to="/" class="text-blue-600 dark:text-blue-400 text-sm hover:underline">返回首页</RouterLink>
    </div>

    <template v-else-if="post">
      <!-- 文章头部 -->
      <header class="pt-10 pb-8 animate-fade-in">
        <div class="flex flex-wrap items-center gap-2 mb-5">
          <RouterLink v-for="t in post.tags" :key="t" :to="`/tag/${t}`"
                      class="text-[12px] px-3 py-1 rounded-full bg-blue-50 dark:bg-blue-950/60 text-blue-600 dark:text-blue-400 hover:bg-blue-100 dark:hover:bg-blue-900 transition-colors">
            # {{ t }}
          </RouterLink>
        </div>
        <h1 class="text-3xl sm:text-4xl font-bold tracking-tight leading-tight mb-5">{{ post.title }}</h1>
        <div class="flex flex-wrap items-center gap-5 text-[13px] text-slate-400">
          <span class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
            {{ formatDate(post.createdAt) }}
          </span>
          <span class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
            {{ post.views }} 阅读
          </span>
          <span class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            约 {{ readingTime(post.content) }} 分钟
          </span>
        </div>
      </header>

      <!-- 正文 + 目录 -->
      <div class="flex gap-10">
        <!-- 正文（封面图已拼入 markdown 顶部，由 MdPreview 统一渲染/查看） -->
        <article class="flex-1 min-w-0 pb-16">
          <!-- mermaid 预加载骨架屏：含图文章先显示骨架，mermaid 就绪后再渲染 MdPreview，
               保证 md-editor-v3 走原生渲染路径（enableZoom 缩放、主题全部正常） -->
          <div v-if="!mdReady" class="py-8 animate-pulse">
            <div class="skeleton h-6 w-1/3 mb-4 rounded"></div>
            <div class="skeleton h-64 w-full rounded-2xl"></div>
            <div class="skeleton h-4 w-full mt-4 rounded"></div>
            <div class="skeleton h-4 w-3/4 mt-3 rounded"></div>
            <p class="text-[13px] text-slate-400 mt-5 flex items-center gap-2">
              <svg class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" d="M12 3a9 9 0 109 9h-3a6 6 0 11-6-6V3z"/></svg>
              正在加载图表引擎…
            </p>
          </div>
          <div v-else class="post-content" ref="articleBody">
            <MdPreview
              :model-value="previewContent"
              :theme="isDark ? 'dark' : 'light'"
              :md-heading-id="headingId"
              @on-html-changed="onHtmlChanged"
            />
          </div>

          <!-- 上一篇/下一篇 -->
          <nav class="mt-12 pt-8 border-t border-slate-200 dark:border-slate-800 grid sm:grid-cols-2 gap-4">
            <RouterLink v-if="post.next_id" :to="`/post/${post.next_slug || ''}`" class="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:border-blue-400 dark:hover:border-blue-700 transition-colors">
              <div class="text-[12px] text-slate-400 mb-1.5">← 较新的文章</div>
              <div class="text-[14.5px] font-medium group-hover:text-blue-600 dark:group-hover:text-blue-400 line-clamp-1">{{ post.next_title }}</div>
            </RouterLink>
            <RouterLink v-if="post.prev_id" :to="`/post/${post.prev_slug || ''}`" class="group p-4 rounded-xl border border-slate-200 dark:border-slate-800 hover:border-blue-400 dark:hover:border-blue-700 transition-colors sm:text-right">
              <div class="text-[12px] text-slate-400 mb-1.5">较旧的文章 →</div>
              <div class="text-[14.5px] font-medium group-hover:text-blue-600 dark:group-hover:text-blue-400 line-clamp-1">{{ post.prev_title }}</div>
            </RouterLink>
          </nav>
        </article>

        <!-- 目录 TOC -->
        <aside v-if="toc.length > 0" class="hidden lg:block w-52 shrink-0 pt-2">
          <div class="sticky top-24">
            <div class="text-[12px] font-semibold text-slate-400 mb-3 tracking-widest uppercase">目录</div>
            <nav class="space-y-1.5 border-l border-slate-200 dark:border-slate-800">
              <a v-for="item in toc" :key="item.id" :href="'#' + item.id"
                 @click.prevent="scrollToHeading(item.id)"
                 class="block text-[13px] leading-snug transition-colors border-l-2 -ml-px pl-3 py-0.5"
                 :class="activeHeading === item.id
                   ? 'border-blue-500 text-blue-600 dark:text-blue-400 font-medium'
                   : item.level === 3 ? 'pl-6 border-transparent text-slate-500 dark:text-slate-400 hover:text-slate-700 dark:hover:text-slate-200'
                                     : 'border-transparent text-slate-600 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100'">
                {{ item.text }}
              </a>
            </nav>
          </div>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import '../utils/editor-config'
import { api } from '../api'
import { extractToc, readingTime, formatDate, slugify } from '../utils/markdown'

const route = useRoute()
const post = ref(null)
const loading = ref(true)
const error = ref('')
const articleBody = ref(null)
const activeHeading = ref('')
const isDark = ref(false)
// 正文就绪标记：含 mermaid 的文章需等 mermaid.min.js 预加载完成（骨架屏期间），
// 确保 MdPreview 挂载时 mermaid 实例已就绪 → md-editor-v3 走原生渲染路径
// （enableZoom 缩放、主题全部正常），从根上消除"图表时灵时不灵"竞态。
const mdReady = ref(false)

const toc = computed(() => extractToc(post.value?.content || ''))

/** 正文 markdown：封面图以 HTML 拼入顶部（保留居中/限高样式），由 MdPreview 统一渲染和点击查看 */
const previewContent = computed(() => {
  const content = post.value?.content || ''
  const cover = post.value?.coverUrl
  if (!cover) return content
  const alt = (post.value?.title || '封面').replace(/"/g, '&quot;')
  return `<div class="md-cover-wrap"><img class="md-cover-img" src="${cover}" alt="${alt}" /></div>\n\n${content}`
})

// MdPreview 标题锚点 id 与 extractToc 保持一致（TOC 跳转依赖）
function headingId({ text }) {
  return slugify(text)
}

function onHtmlChanged() {
  initScrollSpy()
}

/** 预加载 mermaid：注入 script 等 window.mermaid 就绪（含 mermaid 文章在骨架屏阶段调用） */
let mermaidPreloadPromise = null
function preloadMermaid() {
  if (window.mermaid) return Promise.resolve()
  if (mermaidPreloadPromise) return mermaidPreloadPromise
  mermaidPreloadPromise = new Promise((resolve) => {
    const s = document.createElement('script')
    s.src = '/vendor/mermaid.min.js'
    s.onload = () => resolve()
    s.onerror = () => resolve() // 加载失败也别卡死，让 MdPreview 自己降级
    document.head.appendChild(s)
  })
  return mermaidPreloadPromise
}

/** 文章正文是否含 mermaid 图 */
function hasMermaid(content) {
  return /```mermaid|```mindmap/.test(content || '')
}

async function load() {
  loading.value = true
  error.value = ''
  post.value = null
  mdReady.value = false
  try {
    post.value = await api.getArticle(route.params.slug)
    if (hasMermaid(post.value?.content)) {
      await preloadMermaid()
    }
  } catch (e) {
    error.value = e.message || '文章加载失败'
  } finally {
    loading.value = false
    mdReady.value = true
  }
}

function scrollToHeading(id) {
  const el = document.getElementById(id)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    history.replaceState(null, '', '#' + id)
  }
}

// 滚动高亮目录
let scrollHandler = null
function initScrollSpy() {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
  scrollHandler = () => {
    const headings = articleBody.value?.querySelectorAll('h2, h3') || []
    let current = ''
    for (const h of headings) {
      if (h.getBoundingClientRect().top <= 120) {
        current = h.id
      }
    }
    if (current !== activeHeading.value) activeHeading.value = current
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
  scrollHandler()
}

watch(() => route.params.slug, () => {
  loading.value = true
  post.value = null
  load()
}, { immediate: true })

// 主题跟随：监听 <html> 的 dark class 变化
let themeObserver = null
function initThemeObserver() {
  isDark.value = document.documentElement.classList.contains('dark')
  themeObserver = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains('dark')
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
}

/**
 * mermaid 图全屏查看器已移除（2026-08）：改用"骨架屏 + 预加载 mermaid 就绪后再
 * 渲染 MdPreview"，md-editor-v3 走原生渲染路径，enableZoom 缩放原生自带。
 */

onMounted(() => {
  initThemeObserver()
})

onUnmounted(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
  if (themeObserver) themeObserver.disconnect()
})
</script>
