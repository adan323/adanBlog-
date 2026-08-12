<template>
  <div>
    <h2 class="page-title">站点设置</h2>

    <el-card shadow="never" class="settings-card" v-loading="loading">
      <el-form :model="form" label-width="120px" label-position="left">
        <el-divider content-position="left">基本信息</el-divider>
        <el-form-item label="站点标题">
          <el-input v-model="form.site_title" placeholder="例如：adan 的博客" />
        </el-form-item>
        <el-form-item label="站点副标题">
          <el-input v-model="form.site_subtitle" placeholder="一句简短的话" />
        </el-form-item>
        <el-form-item label="站点描述">
          <el-input v-model="form.site_description" type="textarea" :rows="2" placeholder="用于 SEO 描述" />
        </el-form-item>

        <el-divider content-position="left">作者信息</el-divider>
        <el-form-item label="作者名">
          <el-input v-model="form.author_name" placeholder="例如：adan" />
        </el-form-item>
        <el-form-item label="作者简介">
          <el-input v-model="form.author_bio" type="textarea" :rows="2" placeholder="关于页面展示的简介" />
        </el-form-item>
        <el-form-item label="作者头像">
          <div class="avatar-row">
            <el-input v-model="form.author_avatar" placeholder="头像图片 URL" />
            <div v-if="form.author_avatar" class="avatar-preview">
              <img :src="form.author_avatar" alt="头像" />
            </div>
          </div>
        </el-form-item>

        <el-divider content-position="left">联系方式</el-divider>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="contact@example.com" />
        </el-form-item>
        <el-form-item label="GitHub">
          <el-input v-model="form.github_url" placeholder="https://github.com/xxx" />
        </el-form-item>
        <el-form-item label="备案号">
          <el-input v-model="form.icp" placeholder="可选，如 粤ICP备00000000号" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api'

const loading = ref(true)
const saving = ref(false)
const form = reactive({
  site_title: '',
  site_subtitle: '',
  site_description: '',
  author_name: '',
  author_bio: '',
  author_avatar: '',
  email: '',
  github_url: '',
  icp: '',
})

async function save() {
  saving.value = true
  try {
    await adminApi.updateSettings({ ...form })
    ElMessage.success('设置已保存')
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  try {
    const data = await adminApi.getSettings()
    Object.assign(form, data)
  } catch (e) {
    ElMessage.error(e.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0 0 16px; }
.settings-card { border-radius: 12px; max-width: 720px; }
.avatar-row { display: flex; gap: 12px; align-items: center; width: 100%; }
.avatar-row .el-input { flex: 1; }
.avatar-preview img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #e2e8f0;
}
</style>
