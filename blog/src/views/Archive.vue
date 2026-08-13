<template>
  <div class="container-blog py-12 fade-in-page">
    <header class="pt-6 pb-10">
      <h1 class="text-3xl font-bold tracking-tight mb-3">归档</h1>
      <p class="text-slate-500 dark:text-slate-400 text-[14.5px]">按时间顺序记录的所有文章，共 {{ archive.total || 0 }} 篇</p>
    </header>

    <div v-if="loading" class="space-y-4">
      <div v-for="i in 5" :key="i" class="skeleton h-14 rounded-xl"></div>
    </div>

    <div v-else-if="error" class="text-center py-20 text-slate-400">{{ error }}</div>

    <div v-else-if="!archive.years || archive.years.length === 0" class="text-center py-20 text-slate-400">还没有文章</div>

    <div v-else class="relative">
      <!-- 时间线：年份手风琴，默认展开最新年份，大数据量下 DOM 只渲染展开的部分 -->
      <div class="border-l-2 border-slate-200 dark:border-slate-800 ml-2">
        <div v-for="group in archive.years" :key="group.year" class="mb-10">
          <!-- 年份头：点击展开/收起 -->
          <div class="flex items-center gap-3 ml-[-9px] mb-4 cursor-pointer select-none group"
               @click="toggleYear(group.year)">
            <span class="w-4 h-4 rounded-full bg-blue-500 border-4 border-blue-200 dark:border-blue-900"></span>
            <span class="text-xl font-bold group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">{{ group.year }}</span>
            <span class="text-[12.5px] text-slate-400">{{ group.total }} 篇</span>
            <svg class="w-4 h-4 text-slate-400 transition-transform duration-300"
                 :class="isExpanded(group.year) ? 'rotate-90' : ''"
                 fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/>
            </svg>
          </div>

          <div v-show="isExpanded(group.year)" class="ml-8">
            <div v-for="month in group.months" :key="month.month" class="mb-8">
              <div class="text-[13px] font-semibold text-slate-500 dark:text-slate-400 mb-3">{{ month.month }} 月（{{ month.total }}）</div>
              <div class="space-y-2.5">
                <RouterLink v-for="post in month.posts" :key="post.id" :to="`/post/${post.slug}`"
                            class="flex items-baseline gap-4 group p-3 -mx-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-900/50 transition-colors">
                  <span class="text-[13px] text-slate-400 font-mono w-10 shrink-0 tabular-nums">{{ post.day }}</span>
                  <span class="text-[15px] group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors line-clamp-1">{{ post.title }}</span>
                  <span class="ml-auto text-[11.5px] text-slate-300 dark:text-slate-600 shrink-0 tabular-nums">{{ post.views }} 阅</span>
                </RouterLink>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const loading = ref(true)
const error = ref('')
const archive = ref({ total: 0, years: [] })
// 年份展开状态：默认只展开最新年份（大流量下 DOM 轻量）
const expanded = ref({})

function isExpanded(year) {
  return !!expanded.value[year]
}

function toggleYear(year) {
  expanded.value = { ...expanded.value, [year]: !expanded.value[year] }
}

onMounted(async () => {
  try {
    const data = await api.getArchive()
    archive.value = data
    const years = data.years || []
    if (years.length > 0) {
      expanded.value = { [years[0].year]: true }
    }
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})
</script>
