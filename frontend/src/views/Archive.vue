<template>
  <div class="container-blog py-12">
    <header class="pt-6 pb-10">
      <h1 class="text-3xl font-bold tracking-tight mb-3">归档</h1>
      <p class="text-slate-500 dark:text-slate-400 text-[14.5px]">按时间顺序记录的所有文章</p>
    </header>

    <div v-if="loading" class="space-y-4">
      <div v-for="i in 5" :key="i" class="skeleton h-14 rounded-xl"></div>
    </div>

    <div v-else-if="error" class="text-center py-20 text-slate-400">{{ error }}</div>

    <div v-else class="relative">
      <!-- 时间线 -->
      <div class="border-l-2 border-slate-200 dark:border-slate-800 ml-2">
        <div v-for="group in grouped" :key="group.year" class="mb-10">
          <div class="flex items-center gap-3 ml-[-9px] mb-6">
            <span class="w-4 h-4 rounded-full bg-blue-500 border-4 border-blue-200 dark:border-blue-900"></span>
            <span class="text-xl font-bold">{{ group.year }}</span>
            <span class="text-[12.5px] text-slate-400">{{ group.total }} 篇</span>
          </div>
          <div v-for="month in group.months" :key="month.month" class="ml-8 mb-8">
            <div class="text-[13px] font-semibold text-slate-500 dark:text-slate-400 mb-3">{{ month.month }} 月</div>
            <div class="space-y-2.5">
              <RouterLink v-for="post in month.posts" :key="post.id" :to="`/post/${post.slug}`"
                          class="flex items-baseline gap-4 group p-3 -mx-3 rounded-lg hover:bg-slate-50 dark:hover:bg-slate-900/50 transition-colors">
                <span class="text-[13px] text-slate-400 font-mono w-10 shrink-0 tabular-nums">{{ post.day }}</span>
                <span class="text-[15px] group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors line-clamp-1">{{ post.title }}</span>
              </RouterLink>
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
import { parseDate } from '../utils/markdown'

const loading = ref(true)
const error = ref('')
const archive = ref([])
const grouped = ref([])

onMounted(async () => {
  try {
    const data = await api.getArchive()
    archive.value = data.months || []
    // 拉取全部文章按时间分组（简单方案：翻页拉）
    const all = []
    let page = 1
    let last = false
    while (!last) {
      const d = await api.listArticles(page, 50)
      all.push(...d.content)
      last = d.last
      page++
      if (page > 10) break
    }
    // 按 年>月>日 分组
    const yearMap = new Map()
    for (const p of all) {
      const d = parseDate(p.createdAt)
      if (!d) continue
      const y = d.getFullYear()
      const m = d.getMonth() + 1
      const day = d.getDate()
      if (!yearMap.has(y)) yearMap.set(y, new Map())
      const monthMap = yearMap.get(y)
      if (!monthMap.has(m)) monthMap.set(m, [])
      monthMap.get(m).push({ ...p, day: String(day).padStart(2, '0') })
    }
    const result = []
    for (const [y, monthMap] of [...yearMap.entries()].sort((a, b) => b[0] - a[0])) {
      const months = []
      let total = 0
      for (const [m, posts] of [...monthMap.entries()].sort((a, b) => b[0] - a[0])) {
        posts.sort((a, b) => parseDate(b.createdAt) - parseDate(a.createdAt))
        months.push({ month: m, posts })
        total += posts.length
      }
      result.push({ year: y, months, total })
    }
    grouped.value = result
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
})
</script>
