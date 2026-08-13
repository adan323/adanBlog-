<template>
  <div class="container-blog py-12 fade-in-page">
    <header class="pt-6 pb-10">
      <h1 class="text-3xl font-bold tracking-tight mb-3">标签</h1>
      <p class="text-slate-500 dark:text-slate-400 text-[14.5px]">按主题浏览文章，共 {{ tags.length }} 个标签</p>
    </header>

    <div v-if="loading" class="flex flex-wrap gap-3">
      <div v-for="i in 8" :key="i" class="skeleton h-10 w-28 rounded-full"></div>
    </div>

    <div v-else-if="error" class="text-center py-20 text-slate-400">{{ error }}</div>

    <template v-else>
      <!-- 本地过滤：标签多时快速定位 -->
      <div class="relative mb-8 max-w-sm">
        <svg class="w-4 h-4 text-slate-400 absolute left-4 top-1/2 -translate-y-1/2" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-4.35-4.35M17 10.5a6.5 6.5 0 11-13 0 6.5 6.5 0 0113 0z"/>
        </svg>
        <input v-model="keyword" type="text" placeholder="搜索标签…"
               class="w-full pl-11 pr-4 py-2.5 rounded-full border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-[14px] placeholder:text-slate-400 focus:outline-none focus:border-blue-400 dark:focus:border-blue-600 transition-colors">
      </div>

      <div v-if="filtered.length === 0" class="text-center py-16 text-slate-400">没有匹配的标签</div>

      <!-- 标签云（后端已按文章数降序） -->
      <div v-else class="flex flex-wrap gap-3">
        <RouterLink v-for="tag in filtered" :key="tag.id" :to="`/tag/${tag.slug}`"
                    class="group px-5 py-2.5 rounded-full border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 hover:border-blue-400 dark:hover:border-blue-600 hover:shadow-md transition-all duration-300"
                    :style="{ fontSize: fontSize(tag.count) + 'px' }">
          <span class="group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors"># {{ tag.name }}</span>
          <span class="ml-1.5 text-slate-400 text-[0.85em]">{{ tag.count }}</span>
        </RouterLink>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { api } from '../api'

const tags = ref([])
const loading = ref(true)
const error = ref('')
const keyword = ref('')

const filtered = computed(() => {
  const kw = keyword.value.trim()
  if (!kw) return tags.value
  return tags.value.filter((t) => t.name.includes(kw))
})

function fontSize(count) {
  return Math.min(20, Math.max(13, 13 + count * 1.5))
}

onMounted(async () => {
  try {
    tags.value = await api.getTags()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})
</script>
