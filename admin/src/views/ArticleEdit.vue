<template>
  <div>
    <div class="edit-header">
      <h2 class="page-title">{{ isEdit ? '编辑文章' : '写新文章' }}</h2>
      <div class="edit-actions">
        <el-button @click="$router.push('/articles')">返回列表</el-button>
        <el-button :loading="saving" @click="save('draft')">保存草稿</el-button>
        <el-button type="primary" :loading="saving" @click="save('published')">
          {{ isEdit ? '更新并发布' : '发布文章' }}
        </el-button>
      </div>
    </div>

    <el-card shadow="never" class="edit-card">
      <!-- 基本信息 -->
      <div class="form-section">
        <div class="form-row">
          <el-input v-model="form.title" size="large" placeholder="文章标题" class="title-input" />
        </div>
        <div class="form-row two-col">
          <div>
            <label class="field-label">Slug（URL 标识，留空自动生成）</label>
            <el-input v-model="form.slug" placeholder="例如：my-first-post" />
          </div>
          <div>
            <label class="field-label">标签（逗号分隔）</label>
            <el-input v-model="tagInput" placeholder="技术, 生活, 随笔" @change="parseTags" />
          </div>
        </div>
        <div class="form-row">
          <label class="field-label">摘要</label>
          <el-input v-model="form.summary" type="textarea" :rows="2" placeholder="文章摘要，显示在列表卡片上" maxlength="500" show-word-limit />
        </div>
        <div class="form-row">
          <label class="field-label">封面图</label>
          <div class="cover-row">
            <el-input v-model="form.coverUrl" placeholder="封面图 URL，或上传图片" />
            <el-upload
              :show-file-list="false"
              :http-request="doUpload"
              accept="image/*"
            >
              <el-button>上传图片</el-button>
            </el-upload>
            <div v-if="form.coverUrl" class="cover-preview">
              <img :src="form.coverUrl" alt="封面预览" />
              <el-button link type="danger" size="small" @click="form.coverUrl = ''">移除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- md-editor-v3 编辑器：支持 Mermaid（思维导图/流程图等）、Katex 公式、代码高亮 -->
      <MdEditor
        v-model="form.content"
        style="height: 640px"
        class="md-editor"
        :toolbars="toolbars"
        :on-upload-img="onUploadImg"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import '../utils/editor-config'
import { adminApi } from '../api'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const tagInput = ref('')

const form = reactive({
  title: '',
  slug: '',
  summary: '',
  content: '',
  coverUrl: '',
  status: 'draft',
  tags: [],
})

// 精简工具栏：'-' 分隔工具，'=' 分左右区；含 Mermaid 图、公式、图片、代码块等
const toolbars = [
  'bold', 'italic', 'strikeThrough', '-',
  'title', 'quote', 'unorderedList', 'orderedList', 'task', '-',
  'link', 'image', 'table', 'code', 'codeRow', 'mermaid', 'katex', '-',
  'revoke', 'next', '=',
  'preview', 'fullscreen', 'htmlPreview', 'catalog',
]

function parseTags() {
  form.tags = tagInput.value
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
}

async function save(status) {
  if (!form.title.trim()) {
    ElMessage.warning('请填写文章标题')
    return
  }
  saving.value = true
  parseTags()
  const payload = { ...form, status }
  try {
    if (isEdit.value) {
      await adminApi.updateArticle(route.params.id, payload)
      ElMessage.success('文章已更新')
    } else {
      const created = await adminApi.createArticle(payload)
      ElMessage.success('文章已创建')
      router.replace(`/articles/${created.id}/edit`)
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function doUpload({ file }) {
  try {
    const res = await adminApi.upload(file)
    form.coverUrl = res.url
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error(e.message || '上传失败')
  }
}

// md-editor-v3 图片上传：files 为选中文件数组，callback 接收上传后的 URL 数组
async function onUploadImg(files, callback) {
  try {
    const urls = []
    for (const file of files) {
      const res = await adminApi.upload(file)
      urls.push(res.url)
    }
    callback(urls)
    ElMessage.success('图片上传成功')
  } catch (e) {
    ElMessage.error(e.message || '图片上传失败')
    callback([])
  }
}

onMounted(async () => {
  if (isEdit.value) {
    try {
      const data = await adminApi.getArticle(route.params.id)
      Object.assign(form, {
        title: data.title,
        slug: data.slug,
        summary: data.summary || '',
        content: data.content || '',
        coverUrl: data.coverUrl || '',
        status: data.status,
        tags: data.tags || [],
      })
      tagInput.value = (data.tags || []).join(', ')
    } catch (e) {
      ElMessage.error(e.message)
    }
  }
})
</script>

<style scoped>
.edit-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0; }
.edit-actions { display: flex; gap: 10px; }
.edit-card { border-radius: 12px; }
.form-section { margin-bottom: 20px; }
.form-row { margin-bottom: 16px; }
.form-row.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.title-input :deep(.el-input__wrapper) {
  font-size: 18px;
  font-weight: 600;
}
.field-label {
  display: block;
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}
.cover-row {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  flex-wrap: wrap;
}
.cover-row .el-input { flex: 1; min-width: 240px; }
.cover-preview {
  width: 100%;
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.cover-preview img {
  max-height: 120px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}
.md-editor {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  margin-top: 4px;
}
@media (max-width: 900px) {
  .form-row.two-col { grid-template-columns: 1fr; }
}
</style>
