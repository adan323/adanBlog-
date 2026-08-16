<template>
  <!-- 全局悬浮音乐播放器 -->
  <div class="music-player-fixed">
    <!-- 播放器面板 -->
    <transition name="player-pop">
      <div v-if="open" class="player-panel">
        <div ref="playerRef" class="aplayer-container"></div>
      </div>
    </transition>

    <!-- 悬浮按钮 -->
    <button
      class="music-toggle"
      :class="{ 'is-playing': playing }"
      @click="toggle"
      :title="open ? '收起音乐播放器' : '打开音乐播放器'"
    >
      <svg v-if="!playing" class="w-5 h-5" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 3v10.55A4 4 0 1 0 14 17V7h4V3h-6z" />
      </svg>
      <svg v-else class="w-5 h-5 animate-spin-slow" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 3v10.55A4 4 0 1 0 14 17V7h4V3h-6z" />
      </svg>
      <span v-if="!open" class="music-toggle-dot"></span>
    </button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import APlayer from 'aplayer'
import 'aplayer/dist/APlayer.min.css'

const open = ref(false)
const playing = ref(false)
const playerRef = ref(null)
let player = null

async function loadMusic() {
  try {
    const r = await fetch('/api/public/music')
    if (!r.ok) throw new Error(`music ${r.status}`)
    const list = await r.json()
    if (!list.length) return
    const audio = list.map((m) => ({
      name: m.title || '未知歌曲',
      artist: m.artist || '未知歌手',
      url: m.url,
      lrc: m.lrc || '',
    }))
    player = new APlayer({
      container: playerRef.value,
      audio,
      lrcType: 3,            // 3 = lrc 文件链接
      listFolded: false,
      order: 'list',
      preload: 'metadata',
      volume: 0.7,
      theme: '#3b82f6',
    })
    player.on('play', () => { playing.value = true })
    player.on('pause', () => { playing.value = false })
  } catch (e) {
    console.error('music player init failed:', e)
  }
}

async function toggle() {
  open.value = !open.value
  if (open.value && !player) {
    await nextTick()
    loadMusic()
  }
}

onMounted(() => {
  // 初始化后先预取列表（点开立刻能用），但不自动播放
  fetch('/api/public/music')
    .then((r) => (r.ok ? r.json() : []))
    .then((list) => {
      if (list.length) window.__musicList = list
    })
    .catch(() => {})
})

onUnmounted(() => {
  if (player) {
    player.destroy()
    player = null
  }
})
</script>

<style scoped>
.music-player-fixed {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 60;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}
.music-toggle {
  width: 52px;
  height: 52px;
  border-radius: 9999px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  cursor: pointer;
  border: none;
}
.music-toggle:hover {
  transform: scale(1.08);
  box-shadow: 0 10px 28px rgba(59, 130, 246, 0.45);
}
.music-toggle.is-playing {
  box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15), 0 8px 24px rgba(59, 130, 246, 0.35);
}
.music-toggle-dot {
  position: absolute;
  top: 8px;
  right: 9px;
  width: 9px;
  height: 9px;
  border-radius: 9999px;
  background: #ef4444;
  border: 2px solid #fff;
}
.player-panel {
  width: 340px;
  max-width: calc(100vw - 32px);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 16px 48px rgba(2, 6, 23, 0.25);
  background: #fff;
}
.player-pop-enter-active,
.player-pop-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.player-pop-enter-from,
.player-pop-leave-to {
  opacity: 0;
  transform: translateY(12px) scale(0.97);
}
:deep(.aplayer-container .aplayer) {
  border-radius: 16px;
}
</style>
