<template>
  <div class="container-blog py-12 fade-in-page">
    <header class="pt-6 pb-12 text-center">
      <div class="w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-500 to-indigo-500 flex items-center justify-center text-white text-3xl font-bold mx-auto mb-5 shadow-lg animate-float">
        {{ (settings.author_name || 'a').charAt(0).toUpperCase() }}
      </div>
      <h1 class="text-3xl font-bold tracking-tight mb-3">{{ settings.author_name || '关于我' }}</h1>
      <p class="text-slate-500 dark:text-slate-400 text-[15px] max-w-lg mx-auto">{{ settings.author_bio || '' }}</p>
    </header>

    <div class="max-w-2xl mx-auto space-y-10 pb-8">
      <!-- 简介 -->
      <section class="p-7 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
        <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
          <span class="w-1.5 h-5 bg-blue-500 rounded-full"></span>关于这个博客
        </h2>
        <p class="text-[14.5px] leading-relaxed text-slate-600 dark:text-slate-400">
          这里是我的个人博客，记录技术学习、生活感悟和一些有趣的想法。
          内容方向比较随性——可能今天在聊代码，明天就在写生活。
        </p>
      </section>

      <!-- 技术栈 -->
      <section class="p-7 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
        <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
          <span class="w-1.5 h-5 bg-indigo-500 rounded-full"></span>本站技术栈
        </h2>
        <div class="flex flex-wrap gap-2.5">
          <span v-for="tech in techStack" :key="tech" class="text-[12.5px] px-3.5 py-1.5 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">{{ tech }}</span>
        </div>
      </section>

      <!-- 联系 -->
      <section class="p-7 rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
        <h2 class="text-lg font-semibold mb-4 flex items-center gap-2">
          <span class="w-1.5 h-5 bg-emerald-500 rounded-full"></span>联系方式
        </h2>
        <div class="space-y-3 text-[14px]">
          <a v-if="settings.email" :href="'mailto:' + settings.email" class="flex items-center gap-3 text-slate-600 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
            <svg class="w-4.5 h-4.5 w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/></svg>
            {{ settings.email }}
          </a>
          <a v-if="settings.github_url" :href="settings.github_url" target="_blank" rel="noopener" class="flex items-center gap-3 text-slate-600 dark:text-slate-400 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
            <svg class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 2C6.48 2 2 6.48 2 12c0 4.42 2.87 8.17 6.84 9.5.5.09.68-.22.68-.48v-1.7c-2.78.6-3.37-1.34-3.37-1.34-.45-1.16-1.11-1.47-1.11-1.47-.91-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.89 1.52 2.34 1.08 2.91.83.09-.65.35-1.09.63-1.34-2.22-.25-4.56-1.11-4.56-4.94 0-1.09.39-1.98 1.03-2.68-.1-.25-.45-1.27.1-2.64 0 0 .84-.27 2.75 1.02a9.58 9.58 0 015 0c1.91-1.29 2.75-1.02 2.75-1.02.55 1.37.2 2.39.1 2.64.64.7 1.03 1.59 1.03 2.68 0 3.84-2.34 4.68-4.57 4.93.36.31.68.92.68 1.85V21c0 .27.18.58.69.48A10 10 0 0022 12c0-5.52-4.48-10-10-10z"/></svg>
            GitHub
          </a>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../api'

const settings = ref({})
const techStack = ['Java 17', 'Spring Boot 3.3', 'Vue 3', 'Vite', 'Tailwind CSS', 'H2 Database', 'Markdown']

onMounted(async () => {
  try {
    settings.value = await api.getSettings()
  } catch {}
})
</script>
