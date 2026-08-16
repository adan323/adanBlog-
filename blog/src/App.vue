<template>
  <div class="min-h-screen flex flex-col">
    <!-- 阅读进度条 -->
    <div class="progress-bar" :style="{ width: progress + '%' }" v-if="showProgress"></div>

    <!-- 导航 -->
    <nav class="fixed top-0 inset-x-0 z-50 transition-all duration-300"
         :class="scrolled ? 'bg-white/85 dark:bg-slate-950/85 backdrop-blur-xl shadow-sm' : 'bg-transparent'">
      <div class="container-wide flex items-center justify-between h-16">
        <RouterLink to="/" class="flex items-center gap-2.5 group">
          <span class="w-8 h-8 rounded-lg bg-gradient-to-br from-blue-500 to-indigo-500 flex items-center justify-center text-white font-bold text-sm shadow-md group-hover:scale-110 transition-transform">A</span>
          <span class="font-semibold text-[17px] tracking-tight">{{ settings.site_title || 'adan 的博客' }}</span>
        </RouterLink>

        <!-- 桌面导航 -->
        <div class="hidden md:flex items-center gap-7 text-[14.5px] text-slate-600 dark:text-slate-300">
          <RouterLink to="/" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors" active-class="text-blue-600 dark:text-blue-400 font-medium">首页</RouterLink>
          <RouterLink to="/archive" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors" active-class="text-blue-600 dark:text-blue-400 font-medium">归档</RouterLink>
          <RouterLink to="/tags" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors" active-class="text-blue-600 dark:text-blue-400 font-medium">标签</RouterLink>
          <RouterLink to="/about" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors" active-class="text-blue-600 dark:text-blue-400 font-medium">关于</RouterLink>
          <button @click="toggleTheme" class="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors" :title="isDark ? '切换亮色' : '切换暗色'">
            <svg v-if="!isDark" class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 3v2m0 14v2m9-9h-2M5 12H3m15.36-6.36l-1.42 1.42M7.06 16.94l-1.42 1.42M18.36 18.36l-1.42-1.42M7.06 7.06L5.64 5.64M12 8a4 4 0 100 8 4 4 0 000-8z"/></svg>
            <svg v-else class="w-[18px] h-[18px]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 12.79A9 9 0 1111.21 3 7 7 0 0021 12.79z"/></svg>
          </button>
        </div>

        <!-- 移动端菜单按钮 -->
        <button @click="mobileOpen = !mobileOpen" class="md:hidden p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-800">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
            <path v-if="!mobileOpen" stroke-linecap="round" d="M4 6h16M4 12h16M4 18h16"/>
            <path v-else stroke-linecap="round" d="M6 6l12 12M18 6L6 18"/>
          </svg>
        </button>
      </div>
      <!-- 移动端菜单 -->
      <div v-if="mobileOpen" class="md:hidden bg-white dark:bg-slate-950 border-t border-slate-100 dark:border-slate-800 animate-fade-in">
        <div class="container-wide py-4 flex flex-col gap-4 text-[15px]">
          <RouterLink to="/" @click="mobileOpen=false" class="hover:text-blue-600">首页</RouterLink>
          <RouterLink to="/archive" @click="mobileOpen=false" class="hover:text-blue-600">归档</RouterLink>
          <RouterLink to="/tags" @click="mobileOpen=false" class="hover:text-blue-600">标签</RouterLink>
          <RouterLink to="/about" @click="mobileOpen=false" class="hover:text-blue-600">关于</RouterLink>
          <button @click="toggleTheme" class="text-left hover:text-blue-600">{{ isDark ? '切换到亮色' : '切换到暗色' }}</button>
        </div>
      </div>
    </nav>

    <!-- 主内容 -->
    <!-- 注意：不用 <Transition mode="out-in"> 包 RouterView——
         异步路由组件(懒加载)离开时 out-in 会挂起导致页面空白(Vue 3 已知坑)，
         页面入场动画由各组件自身 CSS 动画实现 -->
    <main class="flex-1 pt-16">
      <RouterView />
    </main>

    <!-- 全局悬浮音乐播放器 -->
    <MusicPlayer />

    <!-- 页脚 -->
    <footer class="border-t border-slate-200 dark:border-slate-800 bg-paper-soft dark:bg-slate-950">
      <div class="container-blog py-12">
        <div class="flex flex-col sm:flex-row items-center justify-between gap-6">
          <div class="text-center sm:text-left">
            <div class="font-semibold text-[15px] mb-1">{{ settings.site_title || 'adan 的博客' }}</div>
            <div class="text-[13px] text-slate-500 dark:text-slate-400">{{ settings.site_subtitle || '' }}</div>
          </div>
          <div class="flex items-center gap-5 text-slate-500 dark:text-slate-400">
            <a v-if="settings.github_url" :href="settings.github_url" target="_blank" rel="noopener" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors text-[13.5px]">GitHub</a>
            <a v-if="settings.email" :href="'mailto:' + settings.email" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors text-[13.5px]">{{ settings.email }}</a>
            <a href="/admin" class="hover:text-blue-600 dark:hover:text-blue-400 transition-colors text-[13.5px]">管理</a>
          </div>
        </div>
        <div class="mt-6 pt-6 border-t border-slate-200/60 dark:border-slate-800/60 text-center text-[12.5px] text-slate-400 dark:text-slate-500">
          © {{ new Date().getFullYear() }} {{ settings.site_title || 'adan 的博客' }} · Powered by Spring Boot & Vue 3
          <span v-if="settings.icp" class="ml-2">{{ settings.icp }}</span>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { api } from './api'
import MusicPlayer from './components/MusicPlayer.vue'

const route = useRoute()
const scrolled = ref(false)
const mobileOpen = ref(false)
const isDark = ref(false)
const settings = ref({})
const progress = ref(0)
const showProgress = ref(false)

function onScroll() {
  scrolled.value = window.scrollY > 20
  // 阅读进度
  const total = document.documentElement.scrollHeight - window.innerHeight
  progress.value = total > 0 ? (window.scrollY / total) * 100 : 0
}

function toggleTheme() {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  // 主题初始化
  const saved = localStorage.getItem('theme')
  isDark.value = saved ? saved === 'dark' : window.matchMedia('(prefers-color-scheme: dark)').matches
  document.documentElement.classList.toggle('dark', isDark.value)
  // 站点设置
  try {
    settings.value = await api.getSettings()
  } catch {}
})

onUnmounted(() => window.removeEventListener('scroll', onScroll))

// 文章页显示进度条
watch(() => route.path, (p) => {
  showProgress.value = p.startsWith('/post/')
})
</script>
