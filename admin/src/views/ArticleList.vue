<template>
  <div>
    <div class="list-header">
      <h2 class="page-title">文章管理</h2>
      <el-button type="primary" @click="$router.push('/articles/new')">
        <el-icon style="margin-right:4px"><EditPen /></el-icon>写文章
      </el-button>
    </div>

    <el-card shadow="never" class="table-card">
      <!-- 筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="load(1)">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="published">已发布</el-radio-button>
          <el-radio-button value="draft">草稿</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="articles" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="240">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title }}</span>
              <el-tag v-if="row.status === 'draft'" size="small" type="warning" effect="plain">草稿</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="200">
          <template #default="{ row }">
            <el-tag v-for="t in (row.tags || [])" :key="t" size="small" class="tag-item" effect="light">{{ t }}</el-tag>
            <span v-if="!row.tags || !row.tags.length" class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="views" label="阅读" width="80" align="center" />
        <el-table-column label="更新时间" width="150">
          <template #default="{ row }">{{ formatDate(row.updatedAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="$router.push(`/articles/${row.id}/edit`)">编辑</el-button>
            <el-button link type="primary" size="small" @click="view(row)">预览</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="load"
          @size-change="load(1)"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '../api'

const router = useRouter()
const articles = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const statusFilter = ref('')

function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

async function load(p = page.value) {
  loading.value = true
  try {
    const data = await adminApi.listArticles(p, pageSize.value, statusFilter.value)
    articles.value = data.content
    total.value = data.totalElements
    page.value = data.number + 1
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
}

function view(row) {
  window.open(`/post/${row.slug}`, '_blank')
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除文章「${row.title}」吗？`, '删除确认', { type: 'warning' })
    await adminApi.deleteArticle(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e.message || '删除失败')
  }
}

onMounted(() => load())
</script>

<style scoped>
.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0; }
.table-card { border-radius: 12px; }
.filter-bar { margin-bottom: 16px; }
.title-cell { display: flex; align-items: center; gap: 8px; }
.title-text { font-weight: 500; color: #0f172a; }
.tag-item { margin-right: 4px; }
.muted { color: #cbd5e1; }
.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
