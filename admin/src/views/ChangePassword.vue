<template>
  <div>
    <h2 class="page-title">修改密码</h2>

    <el-card shadow="never" class="pwd-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" label-position="left">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="当前登录密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="至少 8 位" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">确认修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '../api'

const formRef = ref(null)
const saving = ref(false)
const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const rules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少 8 位', trigger: 'blur' },
  ],
  confirm: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_, value, cb) => {
        if (value !== form.newPassword) cb(new Error('两次输入的密码不一致'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    await adminApi.changePassword(form.oldPassword, form.newPassword)
    ElMessage.success('密码已修改，请重新登录')
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    setTimeout(() => (window.location.href = '/admin/login'), 800)
  } catch (e) {
    ElMessage.error(e.message || '修改失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.page-title { font-size: 20px; font-weight: 600; color: #0f172a; margin: 0 0 16px; }
.pwd-card { border-radius: 12px; max-width: 520px; }
</style>
