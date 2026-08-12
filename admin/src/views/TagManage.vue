<template>
  <div>
    <h2 class="page-title">标签管理</h2>

    <el-card shadow="never" class="table-card">
      <el-table :data="tags" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="标签名" min-width="180">
          <template #default="{ row }">
            <el-tag effect="light" type="primary"># {{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="slug" label="Slug" min-width="180" />
        <el-table-column prop="count" label="文章数" width="120" align="center" />
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewTag(row)">查看文章</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="empty-hint" v-if="!loading && tags.length === 0">
        暂无标签，在写文章时添加标签即可自动创建
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '../api'

const tags = ref([])
const loading = ref(true)

function viewTag(row) {
  window.open(`/tag/${row.slug}`, '_blank')
}

onMounted(async () => {
  try {
    tags.value = await adminApi.getTags()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0 0 16px; }
.table-card { border-radius: 12px; }
.empty-hint {
  text-align: center;
  padding: 40px 0;
  color: #94a3b8;
  font-size: 14px;
}
</style>
