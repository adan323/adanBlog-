<template>
  <div class="container-blog py-12 fade-in-page">
    <header class="pt-6 pb-10">
      <h1 class="text-3xl font-bold tracking-tight mb-3">标签</h1>
      <p class="text-slate-500 dark:text-slate-400 text-[14.5px]">按主题浏览文章</p>
    </header>

    <div v-if="loading" class="flex flex-wrap gap-3">
      <div v-for="i in 8" :key="i" class="skeleton h-10 w-28 rounded-full"></div>
    </div>

    <div v-else-if="error" class="text-center py-20 text-slate-400">{{ error }}</div>

    <!-- 标签云 -->
    <div v-else class="flex flex-wrap gap-3">
      <RouterLink v-for="tag in tags" :key="tag.id" :to="`/tag/${tag.slug}`"
                  class="group px-5 py-2.5 rounded-full border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 hover:border-blue-400 dark:hover:border-blue-600 hover:shadow-md transition-all duration-300"
                  :style="{ fontSize: fontSize(tag.count) + 'px' }">
        <span class="group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors"># {{ tag.name }}</span>
        <span class="ml-1.5 text-slate-400 text-[0.85em]">{{ tag.count }}</span>
      </RouterLink>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const tags = ref([])
const loading = ref(true)
const error = ref('')

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
