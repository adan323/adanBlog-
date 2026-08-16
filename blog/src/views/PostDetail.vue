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

      <!-- 封面图：自适应容器包裹（容器宽度贴合图片，竖图/横图都完整显示无留白） -->
      <div v-if="post.coverUrl" class="mb-10 animate-fade-in">
        <!-- 加载/失败占位 -->
        <div v-if="coverState !== 'ok'" class="skeleton flex items-center justify-center rounded-2xl" style="height: 320px; width: 100%">
          <span class="relative z-10 flex items-center gap-2 text-[13px] text-slate-500 dark:text-slate-400 bg-white/70 dark:bg-slate-900/70 backdrop-blur-sm px-4 py-2 rounded-full shadow-sm">
            <svg v-if="coverState !== 'error'" class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" d="M12 3a9 9 0 109 9h-3a6 6 0 11-6-6V3z"/></svg>
            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/></svg>
            {{ coverState === 'error' ? '封面加载失败' : '封面加载中…' }}
          </span>
        </div>
        <!-- 探测图：始终渲染触发加载状态（注意不能用 display:none，否则不触发加载） -->
        <img v-if="coverState !== 'ok'" :src="post.coverUrl" alt="" aria-hidden="true"
             class="block w-0 h-0 opacity-0 pointer-events-none overflow-hidden"
             @load="coverState = 'ok'" @error="coverState = 'error'">
        <!-- 封面图：w-fit 自适应容器 + 居中，容器宽度 = 图片宽度，无固定比例无留白 -->
        <div v-else class="w-fit mx-auto group relative">
          <img :src="post.coverUrl" :alt="post.title" @click="openCover"
               class="block max-w-full w-auto h-auto max-h-[70vh] rounded-2xl shadow-lg cursor-zoom-in transition-shadow hover:shadow-xl">
          <!-- 悬停提示 -->
          <div class="absolute top-3 right-3 opacity-0 group-hover:opacity-100 transition-opacity">
            <span class="flex items-center gap-1 text-[11.5px] text-white bg-black/50 backdrop-blur px-2.5 py-1 rounded-full">
              <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
              点击查看大图
            </span>
          </div>
        </div>
      </div>

      <!-- 正文 + 目录 -->
      <div class="flex gap-10">
        <!-- 正文 -->
        <article class="flex-1 min-w-0 pb-16">
          <div class="post-content" ref="articleBody">
            <MdPreview
              :model-value="post.content"
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
const coverState = ref('')
const isDark = ref(false)

const toc = computed(() => extractToc(post.value?.content || ''))

// MdPreview 标题锚点 id 与 extractToc 保持一致（TOC 跳转依赖）
function headingId({ text }) {
  return slugify(text)
}

function onHtmlChanged() {
  initScrollSpy()
}

async function load() {
  loading.value = true
  error.value = ''
  post.value = null
  coverState.value = ''
  try {
    post.value = await api.getArticle(route.params.slug)
  } catch (e) {
    error.value = e.message || '文章加载失败'
  } finally {
    loading.value = false
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

/** 打开封面大图（新标签页） */
function openCover() {
  if (coverState.value !== 'ok' || !post.value?.coverUrl) return
  window.open(post.value.coverUrl, '_blank', 'noopener')
}

// 主题跟随：监听 <html> 的 dark class 变化
let themeObserver = null
function initThemeObserver() {
  isDark.value = document.documentElement.classList.contains('dark')
  themeObserver = new MutationObserver(() => {
    isDark.value = document.documentElement.classList.contains('dark')
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['class'] })
}

onMounted(() => {
  initThemeObserver()
})

onUnmounted(() => {
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
  if (themeObserver) themeObserver.disconnect()
})
</script>
