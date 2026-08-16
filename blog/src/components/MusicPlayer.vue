<template>
  <!-- 全局音乐播放器：aplayer 吸底模式（fixed:true，整条固定在页面底部左侧） -->
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
      // 吸底模式：整条播放器固定在页面底部
      fixed: true,
      audio,
      lrcType: 3,            // 3 = lrc 文件链接
      listFolded: true,
      order: 'list',
      preload: 'metadata',
      volume: 0.7,
      theme: '#3b82f6',
    })
    // fixed 模式默认是窄条（源码 mini: fixed||narrow），初始化后展开为完整条
    player.setMode('normal')
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
/* aplayer 吸底模式自带 position:fixed;bottom:0;z-index:99（见 APlayer.min.css .aplayer-fixed），
   这里无需额外定位；只提升一点层级避免被文章内容覆盖 */
.music-player :deep(.aplayer-fixed),
.music-player :deep(.aplayer.aplayer-fixed .aplayer-body) {
  z-index: 100;
}
</style>
