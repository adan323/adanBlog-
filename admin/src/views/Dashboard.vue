<template>
  <div>
    <h2 class="page-title">数据看板</h2>
    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon blue"><el-icon :size="22"><Document /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.total || 0 }}</div>
          <div class="stat-label">文章总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><el-icon :size="22"><CircleCheck /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.published || 0 }}</div>
          <div class="stat-label">已发布</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange"><el-icon :size="22"><View /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.views || 0 }}</div>
          <div class="stat-label">总阅读量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple"><el-icon :size="22"><CollectionTag /></el-icon></div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.tags || 0 }}</div>
          <div class="stat-label">标签数</div>
        </div>
      </div>
    </div>

    <el-card class="quick-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>快捷操作</span>
        </div>
      </template>
      <div class="quick-actions">
        <el-button type="primary" @click="$router.push('/articles/new')">
          <el-icon style="margin-right:4px"><EditPen /></el-icon>写新文章
        </el-button>
        <el-button @click="$router.push('/articles')">
          <el-icon style="margin-right:4px"><Document /></el-icon>管理文章
        </el-button>
        <el-button @click="$router.push('/settings')">
          <el-icon style="margin-right:4px"><Setting /></el-icon>站点设置
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api'

const stats = ref({})

onMounted(async () => {
  try {
    stats.value = await adminApi.getStats()
  } catch (e) {
    console.error(e)
  }
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0 0 20px; }
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  border: 1px solid #eef2f7;
  transition: box-shadow .2s, transform .2s;
}
.stat-card:hover {
  box-shadow: 0 8px 24px rgba(15,23,42,.08);
  transform: translateY(-2px);
}
.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.stat-icon.blue { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.stat-icon.green { background: linear-gradient(135deg, #10b981, #059669); }
.stat-icon.orange { background: linear-gradient(135deg, #f59e0b, #d97706); }
.stat-icon.purple { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }
.stat-value { font-size: 24px; font-weight: 700; color: #0f172a; line-height: 1.2; }
.stat-label { font-size: 13px; color: #94a3b8; margin-top: 2px; }
.quick-card { border-radius: 12px; }
.card-header { font-weight: 600; color: #0f172a; }
.quick-actions { display: flex; gap: 12px; flex-wrap: wrap; }
@media (max-width: 900px) {
  .stat-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
