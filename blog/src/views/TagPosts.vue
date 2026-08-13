<template>
  <div class="container-blog py-12 fade-in-page">
    <header class="pt-6 pb-10">
      <RouterLink to="/tags" class="text-[13px] text-blue-600 dark:text-blue-400 hover:underline mb-4 inline-block">← 全部标签</RouterLink>
      <h1 class="text-3xl font-bold tracking-tight mb-3">
        标签：<span class="text-blue-600 dark:text-blue-400"># {{ tagName }}</span>
      </h1>
      <p class="text-slate-500 dark:text-slate-400 text-[14.5px]">共 {{ total }} 篇文章</p>
    </header>

    <div v-if="loading" class="grid gap-5 sm:grid-cols-2">
      <div v-for="i in 4" :key="i" class="skeleton h-32 rounded-2xl"></div>
    </div>

    <div v-else-if="articles.length === 0" class="text-center py-20 text-slate-400">该标签下暂无文章</div>

    <div v-else>
      <div class="space-y-4">
        <RouterLink v-for="post in articles" :key="post.id" :to="`/post/${post.slug}`"
                    class="flex items-center gap-5 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:border-blue-400 dark:hover:border-blue-700 hover:shadow-md transition-all group">
          <div class="flex-1 min-w-0">
            <h2 class="text-[16.5px] font-semibold mb-1.5 group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors line-clamp-1">{{ post.title }}</h2>
            <p class="text-[13px] text-slate-400 line-clamp-1">{{ post.summary }}</p>
          </div>
          <div class="shrink-0 text-right">
            <div class="text-[12.5px] text-slate-400 font-mono">{{ formatDate(post.createdAt) }}</div>
            <div class="text-[11.5px] text-slate-400 mt-1">{{ post.views }} 阅读</div>
          </div>
        </RouterLink>
      </div>

      <!-- 加载更多（大数据量分页） -->
      <div v-if="hasMore" class="text-center pt-10">
        <button @click="loadMore" :disabled="loadingMore"
                class="px-7 py-2.5 rounded-full border border-slate-300 dark:border-slate-700 text-[13.5px] text-slate-500 dark:text-slate-400 hover:border-blue-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors disabled:opacity-50">
          {{ loadingMore ? '加载中…' : '加载更多 ↓' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api'
import { formatDate } from '../utils/markdown'

const route = useRoute()
const articles = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const tagName = ref('')
const total = ref(0)
const page = ref(1)
const pageSize = 15
const hasMore = ref(true)

async function load(reset) {
  if (reset) {
    loading.value = true
    loadingMore.value = false
    articles.value = []
    page.value = 1
    hasMore.value = true
    tagName.value = ''
    total.value = 0
  }
  try {
    const data = await api.listArticles(page.value, pageSize, route.params.slug)
    articles.value.push(...data.content)
    hasMore.value = !data.last
    total.value = data.totalElements
    // 标签名只取一次（轻量接口，避免全量拉标签列表）
    if (!tagName.value) {
      try {
        const t = await api.getTag(route.params.slug)
        tagName.value = t.name
      } catch {
        tagName.value = route.params.slug
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  page.value++
  await load(false)
}

watch(() => route.params.slug, () => load(true), { immediate: true })
</script>
