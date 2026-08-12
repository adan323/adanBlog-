<template>
  <div class="container-blog py-12 fade-in-page">
    <header class="pt-6 pb-10">
      <RouterLink to="/tags" class="text-[13px] text-blue-600 dark:text-blue-400 hover:underline mb-4 inline-block">← 全部标签</RouterLink>
      <h1 class="text-3xl font-bold tracking-tight mb-3">
        标签：<span class="text-blue-600 dark:text-blue-400"># {{ tagName }}</span>
      </h1>
    </header>

    <div v-if="loading" class="grid gap-5 sm:grid-cols-2">
      <div v-for="i in 4" :key="i" class="skeleton h-32 rounded-2xl"></div>
    </div>

    <div v-else-if="articles.length === 0" class="text-center py-20 text-slate-400">该标签下暂无文章</div>

    <div v-else class="space-y-4">
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
const tagName = ref('')

async function load() {
  loading.value = true
  articles.value = []
  try {
    const tags = await api.getTags()
    const t = tags.find((x) => x.slug === route.params.slug)
    tagName.value = t ? t.name : route.params.slug
    const data = await api.listArticles(1, 50, route.params.slug)
    articles.value = data.content
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch(() => route.params.slug, load, { immediate: true })
</script>
