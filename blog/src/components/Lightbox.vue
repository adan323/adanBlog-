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

        <!-- 图片 -->
        <div class="lb-image-wrap">
          <img :src="images[current]" class="lb-image" :class="{ zoomed }" @click="toggleZoom"
               :alt="'图片 ' + (current + 1)" loading="lazy">
        </div>

        <!-- 下一张 -->
        <button v-if="images.length > 1" class="lb-btn lb-next" @click="next" title="下一张 (→)">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
        </button>

        <!-- 计数 + 缩放提示 -->
        <div class="lb-footer">
          <span v-if="images.length > 1" class="lb-count">{{ current + 1 }} / {{ images.length }}</span>
          <span class="lb-hint">{{ zoomed ? '已放大，点击图片还原' : '点击图片缩放' }}</span>
        </div>
      </div>
    </Transition>
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
const zoomed = ref(false)

watch(() => props.visible, (v) => {
  if (v) {
    current.value = props.initialIndex
    zoomed.value = false
  }
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
  zoomed.value = false
  emit('change', current.value)
}

function next() {
  if (props.images.length <= 1) return
  current.value = (current.value + 1) % props.images.length
  zoomed.value = false
  emit('change', current.value)
}

function toggleZoom() {
  zoomed.value = !zoomed.value
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
  cursor: zoom-in;
  backdrop-filter: blur(8px);
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
.lb-image-wrap {
  max-width: 92vw;
  max-height: 88vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
.lb-image {
  max-width: 92vw;
  max-height: 88vh;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 20px 60px rgba(0,0,0,.5);
  transition: transform .3s ease;
  user-select: none;
}
.lb-image.zoomed {
  transform: scale(1.8);
  cursor: zoom-out;
}
.lb-footer {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 16px;
  color: #94a3b8;
  font-size: 13px;
  z-index: 10001;
  background: rgba(15, 23, 42, .6);
  padding: 8px 18px;
  border-radius: 999px;
  backdrop-filter: blur(4px);
}
.lb-count { color: #e2e8f0; font-weight: 600; }
.lb-hint { opacity: .8; }

/* 过渡动画 */
.lightbox-enter-active, .lightbox-leave-active { transition: opacity .25s ease; }
.lightbox-enter-from, .lightbox-leave-to { opacity: 0; }
@media (max-width: 768px) {
  .lb-btn { width: 38px; height: 38px; }
  .lb-close { top: 14px; right: 14px; }
  .lb-prev { left: 10px; }
  .lb-next { right: 10px; }
  .lb-footer { font-size: 12px; padding: 6px 14px; }
}
</style>
