<template>
  <Teleport to="body">
    <!-- 不用 <Transition>：与页面级 Transition 嵌套会卡死路由切换 -->
    <div v-if="visible" class="lightbox lb-fade" @click.self="close">
        <!-- 关闭按钮 -->
        <button class="lb-btn lb-close" @click="close" title="关闭 (Esc)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" d="M6 6l12 12M18 6L6 18"/></svg>
        </button>

        <!-- 上一张 -->
        <button v-if="images.length > 1" class="lb-btn lb-prev" @click="prev" title="上一张 (←)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15 19l-7-7 7-7"/></svg>
        </button>

        <!-- 图片（完整显示，保持原始比例） -->
        <div class="lb-viewport">
          <img :src="images[current]" class="lb-image" :alt="'图片 ' + (current + 1)"
               draggable="false" loading="lazy" @dragstart.prevent>
        </div>

        <!-- 下一张 -->
        <button v-if="images.length > 1" class="lb-btn lb-next" @click="next" title="下一张 (→)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
        </button>

        <!-- 底部工具栏 -->
        <div class="lb-footer">
          <span v-if="images.length > 1" class="lb-count">{{ current + 1 }} / {{ images.length }}</span>
          <span class="lb-sep" v-if="images.length > 1"></span>
          <button class="lb-download" @click="download" title="保存图片">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 16v2a2 2 0 002 2h12a2 2 0 002-2v-2M7 10l5 5 5-5M12 15V3"/></svg>
            保存图片
          </button>
        </div>
      </div>
    </Teleport>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  images: { type: Array, default: () => [] },
  initialIndex: { type: Number, default: 0 },
})

const emit = defineEmits(['update:visible', 'change'])

const current = ref(0)

watch(() => props.visible, (v) => {
  if (v) current.value = props.initialIndex
})

watch(() => props.initialIndex, (v) => {
  current.value = v
})

function close() {
  emit('update:visible', false)
}

function prev() {
  if (props.images.length <= 1) return
  current.value = (current.value - 1 + props.images.length) % props.images.length
  emit('change', current.value)
}

function next() {
  if (props.images.length <= 1) return
  current.value = (current.value + 1) % props.images.length
  emit('change', current.value)
}

/** 保存图片到本地 */
async function download() {
  const url = props.images[current]
  if (!url) return
  const filename = url.split('/').pop().split('?')[0] || `image-${current + 1}.jpg`
  try {
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
    window.open(url, '_blank')
  }
}

function onKeydown(e) {
  if (!props.visible) return
  if (e.key === 'Escape') close()
  else if (e.key === 'ArrowLeft') prev()
  else if (e.key === 'ArrowRight') next()
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
}
.lb-viewport {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: default;
  user-select: none;
  -webkit-user-select: none;
}
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
  /* 完整显示：保持原始比例，max 约束 + contain 兜底 */
  min-width: 0;
  min-height: 0;
  max-width: 92vw;
  max-height: 88vh;
  width: auto;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0,0,0,.5);
}
/* SVG（mermaid 大图）模式已移除（mermaid 全站弃用） */
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

/* 淡入动画（替代 Transition，避免嵌套卡死路由切换） */
.lb-fade { animation: lbFadeIn .2s ease; }
@keyframes lbFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@media (max-width: 768px) {
  .lb-btn { width: 38px; height: 38px; }
  .lb-close { top: 14px; right: 14px; }
  .lb-prev { left: 10px; }
  .lb-next { right: 10px; }
  .lb-footer { font-size: 12px; padding: 6px 14px; gap: 8px; }
}
</style>
