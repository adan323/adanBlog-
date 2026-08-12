<template>
  <div>
    <!-- Hero 区：视差效果 + 打字机标题 -->
    <section class="relative overflow-hidden bg-gradient-to-br from-blue-50 via-white to-indigo-50 dark:from-slate-900 dark:via-slate-950 dark:to-slate-900 pt-14 pb-20">
      <!-- 背景装饰 -->
      <div class="absolute inset-0 pointer-events-none" :style="{ transform: `translateY(${parallaxY}px)` }">
        <div class="absolute -top-20 -right-20 w-72 h-72 rounded-full bg-blue-200/40 dark:bg-blue-900/20 blur-3xl"></div>
        <div class="absolute top-1/3 -left-24 w-64 h-64 rounded-full bg-indigo-200/40 dark:bg-indigo-900/20 blur-3xl"></div>
        <div class="absolute bottom-0 right-1/4 w-48 h-48 rounded-full bg-purple-200/30 dark:bg-purple-900/15 blur-3xl"></div>
      </div>

      <div class="container-blog relative">
        <div class="text-center pt-12 pb-6">
          <div class="inline-flex items-center gap-2 text-[12.5px] text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/50 border border-blue-100 dark:border-blue-900 px-4 py-1.5 rounded-full mb-6 animate-fade-up">
            <span class="w-1.5 h-1.5 rounded-full bg-blue-500 animate-pulse"></span>
            欢迎来到 {{ settings.site_title || '我的博客' }}
          </div>
          <h1 class="text-4xl sm:text-5xl font-bold tracking-tight leading-tight mb-5 animate-fade-up" style="animation-delay:.1s">
            <span class="type-cursor">{{ typedText }}</span>
          </h1>
          <p class="text-slate-500 dark:text-slate-400 text-[16px] max-w-xl mx-auto animate-fade-up" style="animation-delay:.2s">
            {{ settings.site_subtitle || '记录代码与生活的碎片' }}
          </p>
          <div class="flex items-center justify-center gap-8 mt-10 text-center animate-fade-up" style="animation-delay:.3s">
            <div>
              <div class="text-2xl font-bold text-ink dark:text-slate-100">{{ stats.published || 0 }}</div>
              <div class="text-[12.5px] text-slate-400 mt-1">篇文章</div>
            </div>
            <div class="w-px h-10 bg-slate-200 dark:bg-slate-700"></div>
            <div>
              <div class="text-2xl font-bold text-ink dark:text-slate-100">{{ stats.views || 0 }}</div>
              <div class="text-[12.5px] text-slate-400 mt-1">总阅读</div>
            </div>
            <div class="w-px h-10 bg-slate-200 dark:bg-slate-700"></div>
            <div>
              <div class="text-2xl font-bold text-ink dark:text-slate-100">{{ stats.tags || 0 }}</div>
              <div class="text-[12.5px] text-slate-400 mt-1">个标签</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 文章列表 -->
    <section class="container-blog py-12">
      <!-- 骨架屏 -->
      <div v-if="loading && articles.length === 0" class="grid gap-8 sm:grid-cols-2">
        <div v-for="i in 4" :key="i" class="card-hover rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
          <div class="skeleton h-48"></div>
          <div class="p-6">
            <div class="skeleton h-5 w-3/4 mb-3 rounded"></div>
            <div class="skeleton h-4 w-full mb-2 rounded"></div>
            <div class="skeleton h-4 w-2/3 rounded"></div>
          </div>
        </div>
      </div>

      <div v-else-if="articles.length === 0" class="text-center py-24 text-slate-400">
        <div class="text-5xl mb-4">📭</div>
        <p>还没有文章，敬请期待</p>
      </div>

      <!-- 文章卡片瀑布流 -->
      <div v-else class="grid gap-7 sm:grid-cols-2">
        <article v-for="(post, i) in articles" :key="post.id"
                 class="card-hover group rounded-2xl overflow-hidden border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 cursor-pointer shadow-sm hover:shadow-xl"
                 :style="{ animationDelay: (i % 2) * 0.08 + 's' }"
                 @click="goPost(post.slug)">
          <!-- 封面：有封面才加载，加载失败自动隐藏 -->
          <div v-if="post.coverUrl && !coverFailed[post.id]" class="relative h-48 overflow-hidden bg-slate-100 dark:bg-slate-800">
            <img :src="post.coverUrl" :alt="post.title" loading="lazy" @error="onCoverError(post.id)"
                 class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105">
            <!-- 悬停遮罩 -->
            <div class="absolute inset-0 bg-gradient-to-t from-black/50 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          </div>
          <!-- 内容 -->
          <div class="p-6">
            <div class="flex items-center gap-3 text-[12.5px] text-slate-400 mb-3">
              <span>{{ formatDate(post.createdAt) }}</span>
              <span class="w-1 h-1 rounded-full bg-slate-300"></span>
              <span>{{ post.views }} 阅读</span>
            </div>
            <h2 class="text-[17.5px] font-semibold leading-snug mb-2.5 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors line-clamp-2">
              {{ post.title }}
            </h2>
            <p class="text-[13.5px] text-slate-500 dark:text-slate-400 leading-relaxed line-clamp-2 mb-4">
              {{ post.summary }}
            </p>
            <div class="flex items-center justify-between">
              <div class="flex gap-2 flex-wrap">
                <span v-for="t in (post.tags || []).slice(0, 3)" :key="t"
                      class="text-[11.5px] px-2.5 py-1 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400 hover:bg-blue-50 hover:text-blue-600 dark:hover:bg-blue-950 transition-colors">
                  # {{ t }}
                </span>
              </div>
              <span class="text-blue-600 dark:text-blue-400 text-[13px] font-medium opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap ml-2">阅读全文 →</span>
            </div>
          </div>
        </article>
      </div>

      <!-- 加载更多 -->
      <div v-if="hasMore" class="text-center pt-10">
        <button @click="loadMore" :disabled="loadingMore"
                class="px-7 py-2.5 rounded-full border border-slate-300 dark:border-slate-700 text-[13.5px] text-slate-500 dark:text-slate-400 hover:border-blue-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors disabled:opacity-50">
          {{ loadingMore ? '加载中…' : '加载更多 ↓' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { formatDate } from '../utils/markdown'

const router = useRouter()
const articles = ref([])
const page = ref(1)
const hasMore = ref(true)
const loading = ref(true)
const loadingMore = ref(false)
const settings = ref({})
const stats = ref({})
const typedText = ref('')
const parallaxY = ref(0)
const coverFailed = ref({})

/** 封面加载失败时隐藏该封面区域 */
function onCoverError(id) {
  coverFailed.value = { ...coverFailed.value, [id]: true }
}

const typeLines = ['记录代码与生活的点滴', '写写技术，也写写生活', 'Stay hungry, stay foolish.']

let typeTimer = null
let scrollHandler = null

function typeWriter() {
  let line = 0
  let char = 0
  let deleting = false
  typeTimer = setInterval(() => {
    const current = typeLines[line]
    if (!deleting) {
      char++
      typedText.value = current.slice(0, char)
      if (char === current.length) {
        deleting = true
        setTimeout(() => {}, 1500)
        clearInterval(typeTimer)
        setTimeout(() => {
          deleting = false
          char = 0
          line = (line + 1) % typeLines.length
          typedText.value = ''
          typeWriter()
        }, 1800)
      }
    }
  }, 90)
}

async function loadArticles() {
  try {
    const data = await api.listArticles(page.value, 6)
    articles.value.push(...data.content)
    hasMore.value = !data.last
  } catch (e) {
    console.error('加载文章失败', e)
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  page.value++
  await loadArticles()
  loadingMore.value = false
}

function goPost(slug) {
  router.push(`/post/${slug}`)
}

onMounted(async () => {
  await loadArticles()
  typeWriter()
  try {
    settings.value = await api.getSettings()
    stats.value = await api.getStats()
  } catch {}
  // 视差滚动
  scrollHandler = () => {
    parallaxY.value = window.scrollY * 0.25
  }
  window.addEventListener('scroll', scrollHandler, { passive: true })
})

onUnmounted(() => {
  if (typeTimer) clearInterval(typeTimer)
  if (scrollHandler) window.removeEventListener('scroll', scrollHandler)
})
</script>
