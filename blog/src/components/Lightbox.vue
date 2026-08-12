<template>
  <Teleport to="body">
    <Transition name="lightbox">
      <div v-if="visible" class="lightbox" @click.self="close">
        <!-- 关闭按钮 -->
        <button class="lb-btn lb-close" @click="close" title="关闭 (Esc)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        <!-- 上一张 -->
        <button v-if="images.length > 1" class="lb-btn lb-prev" @click="prev" title="上一张 (←)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/></svg>
        </button>

        <!-- 图片容器（拖拽视口）所有交互统一绑在 viewport 上 -->
        <div class="lb-viewport"
             @mousedown="onDragStart" @mousemove="onDragMove" @mouseup="onDragEnd" @mouseleave="onDragEnd"
             @touchstart="onTouchStart" @touchmove="onTouchMove" @touchend="onTouchEnd"
             @dblclick="toggleZoom" @wheel="onWheel">
          <img :src="images[current]" class="lb-image" :class="{ zoomed }"
               :alt="'图片 ' + (current + 1)" :style="imgStyle" draggable="false" loading="lazy"
               @dragstart.prevent>
        </div>

        <!-- 下一张 -->
        <button v-if="images.length > 1" class="lb-btn lb-next" @click="next" title="下一张 (→)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
        </button>

        <!-- 底部工具栏 -->
        <div class="lb-footer">
          <span v-if="images.length > 1" class="lb-count">{{ current + 1 }} / {{ images.length }}</span>
          <span class="lb-hint">
            {{ zoomed ? '拖拽移动 · 滚轮缩放' : '双击放大' }}
          </span>
          <span class="lb-sep"></span>
          <button class="lb-download" @click="download" title="保存图片">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M7 10l5 5 5-5M12 15V3"/></svg>
            保存图片
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  images: { type: Array, default: () => [] },
  initialIndex: { type: Number, default: 0 },
})

const emit = defineEmits(['update:visible', 'change'])

const current = ref(0)
const zoomed = ref(false)
const scale = ref(1)
const panX = ref(0)
const panY = ref(0)

// 拖拽状态
const dragging = ref(false)
const dragStart = ref({ x: 0, y: 0 })
const panStart = ref({ x: 0, y: 0 })
let touchCache = null

const imgStyle = computed(() => {
  const base = {
    transform: `translate(${panX.value}px, ${panY.value}px) scale(${scale.value})`,
  }
  return base
})

watch(() => props.visible, (v) => {
  if (v) {
    current.value = props.initialIndex
    resetView()
  }
})

watch(() => props.initialIndex, (v) => {
  current.value = v
  resetView()
})

watch(() => props.images, () => {
  resetView()
})

function resetView() {
  zoomed.value = false
  scale.value = 1
  panX.value = 0
  panY.value = 0
  dragging.value = false
}

function close() {
  emit('update:visible', false)
}

function prev() {
  if (props.images.length <= 1) return
  current.value = (current.value - 1 + props.images.length) % props.images.length
  resetView()
  emit('change', current.value)
}

function next() {
  if (props.images.length <= 1) return
  current.value = (current.value + 1) % props.images.length
  resetView()
  emit('change', current.value)
}

function toggleZoom() {
  if (scale.value > 1) {
    resetView()
  } else {
    zoomed.value = true
    scale.value = 2
    panX.value = 0
    panY.value = 0
  }
}

/** 滚轮缩放（以光标为中心） */
function onWheel(e) {
  if (!props.visible) return
  e.preventDefault()
  const delta = e.deltaY < 0 ? 0.1 : -0.1
  const newScale = Math.min(5, Math.max(1, +(scale.value + delta).toFixed(2)))
  if (newScale === scale.value) return
  // 视口中心点作为缩放锚点
  const rect = e.currentTarget.getBoundingClientRect()
  const cx = e.clientX - rect.left - rect.width / 2
  const cy = e.clientY - rect.top - rect.height / 2
  const ratio = newScale / scale.value
  panX.value = cx - (cx - panX.value) * ratio
  panY.value = cy - (cy - panY.value) * ratio
  scale.value = newScale
  zoomed.value = scale.value > 1
}

// ===== 鼠标拖拽 =====
function onDragStart(e) {
  if (scale.value <= 1) return
  dragging.value = true
  dragStart.value = { x: e.clientX, y: e.clientY }
  panStart.value = { x: panX.value, y: panY.value }
  e.preventDefault()
}

function onDragMove(e) {
  if (!dragging.value) return
  panX.value = panStart.value.x + (e.clientX - dragStart.value.x)
  panY.value = panStart.value.y + (e.clientY - dragStart.value.y)
}

function onDragEnd() {
  dragging.value = false
}

// ===== 触摸拖拽 =====
function onTouchStart(e) {
  if (scale.value <= 1) return
  const t = e.touches[0]
  touchCache = { x: t.clientX, y: t.clientY, px: panX.value, py: panY.value }
  e.preventDefault()
}

function onTouchMove(e) {
  if (!touchCache) return
  const t = e.touches[0]
  panX.value = touchCache.px + (t.clientX - touchCache.x)
  panY.value = touchCache.py + (t.clientY - touchCache.y)
  e.preventDefault()
}

function onTouchEnd() {
  touchCache = null
}

/** 保存图片到本地 */
async function download() {
  const url = props.images[current]
  if (!url) return
  const filename = url.split('/').pop().split('?')[0] || `image-${current + 1}.jpg`
  try {
    // 同源图片用 fetch blob 下载（可控文件名）
    const res = await fetch(url)
    if (!res.ok) throw new Error('fetch fail')
    const blob = await res.blob()
    const objectUrl = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = objectUrl
    a.download = filename
    document.body.appendChild(a)
    a.click()
    a.remove()
    setTimeout(() => URL.revokeObjectURL(objectUrl), 3000)
  } catch {
    // 跨域/失败 fallback：新窗口打开
    window.open(url, '_blank')
  }
}

function onKeydown(e) {
  if (!props.visible) return
  if (e.key === 'Escape') close()
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
  else if (e.key === '+' || e.key === '=') { if (scale.value < 5) scale.value = +(scale.value + 0.5).toFixed(1) }
  else if (e.key === '-') { if (scale.value > 1) scale.value = +(scale.value - 0.5).toFixed(1) }
  else if (e.key === '0') resetView()
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<style scoped>
.lightbox {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(2, 6, 23, .92);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  overflow: hidden;
  touch-action: none;
}
.lb-viewport {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: grab;
  user-select: none;
  -webkit-user-select: none;
}
.lb-viewport:active { cursor: grabbing; }
.lb-btn {
  position: fixed;
  z-index: 10001;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 1px solid rgba(255,255,255,.15);
  background: rgba(255,255,255,.08);
  color: #e2e8f0;
  cursor: pointer;
  transition: all .2s;
}
.lb-btn:hover { background: rgba(255,255,255,.2); transform: scale(1.05); }
.lb-close { top: 20px; right: 20px; }
.lb-prev { left: 20px; top: 50%; transform: translateY(-50%); }
.lb-next { right: 20px; top: 50%; transform: translateY(-50%); }
.lb-prev:hover, .lb-next:hover { transform: translateY(-50%) scale(1.05); }
.lb-image {
  /* 关键：flex 容器里防止 min-width:auto 撑破，竖图完整显示 */
  min-width: 0;
  min-height: 0;
  max-width: 92vw;
  max-height: 88vh;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0,0,0,.5);
  transition: transform .15s ease-out;
  will-change: transform;
  pointer-events: none;
}
.lb-footer {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  color: #94a3b8;
  font-size: 13px;
  z-index: 10001;
  background: rgba(15, 23, 42, .6);
  padding: 8px 18px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
  white-space: nowrap;
}
.lb-count { color: #e2e8f0; font-weight: 600; }
.lb-hint { opacity: .8; }
.lb-sep { width: 1px; height: 14px; background: rgba(148,163,184,.3); }
.lb-download {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(59, 130, 246, .25);
  color: #bfdbfe;
  border: 1px solid rgba(59, 130, 246, .35);
  padding: 5px 14px;
  border-radius: 999px;
  font-size: 12.5px;
  cursor: pointer;
  transition: all .2s;
}
.lb-download:hover { background: rgba(59, 130, 246, .45); color: #fff; }

/* 过渡动画 */
.lightbox-enter-active, .lightbox-leave-active { transition: opacity .25s ease; }
.lightbox-enter-from, .lightbox-leave-to { opacity: 0; }
@media (max-width: 768px) {
  .lb-btn { width: 38px; height: 38px; }
  .lb-close { top: 14px; right: 14px; }
  .lb-prev { left: 10px; }
  .lb-next { right: 10px; }
  .lb-footer { font-size: 12px; padding: 6px 14px; gap: 8px; }
  .lb-hint { display: none; }
}
</style>
