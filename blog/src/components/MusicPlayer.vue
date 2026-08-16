<template>
  <!-- 全局音乐播放器：aplayer 原生 mini 形态（右下角小圆盘，点击展开完整播放器） -->
  <div ref="playerRef" class="music-player"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import APlayer from 'aplayer'
import 'aplayer/dist/APlayer.min.css'

const playerRef = ref(null)
let player = null

onMounted(async () => {
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
      cover: m.cover || '',
    }))
    player = new APlayer({
      container: playerRef.value,
      // 原生 mini 形态：右下角小圆盘，点击展开
      mini: true,
      audio,
      lrcType: 3,            // 3 = lrc 文件链接
      listFolded: true,
      order: 'list',
      preload: 'metadata',
      volume: 0.7,
      theme: '#3b82f6',
    })
  } catch (e) {
    console.error('music player init failed:', e)
  }
})

onUnmounted(() => {
  if (player) {
    player.destroy()
    player = null
  }
})
</script>

<style scoped>
.music-player {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 60;
}
</style>
