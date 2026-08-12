<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <span class="logo-badge">A</span>
        <h1>adan 博客管理后台</h1>
        <p>登录以继续</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="submit">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password :prefix-icon="Lock" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="submit">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <a href="/" target="_blank">← 返回博客首页</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { adminApi } from '../api'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await adminApi.login(form.username, form.password)
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_username', res.username)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  position: relative;
  overflow: hidden;
}
.login-page::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(59,130,246,.15), transparent 70%);
  top: -150px;
  right: -100px;
}
.login-page::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(99,102,241,.12), transparent 70%);
  bottom: -120px;
  left: -80px;
}
.login-card {
  width: 380px;
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px 28px;
  box-shadow: 0 24px 60px rgba(0,0,0,.3);
  position: relative;
  z-index: 2;
}
.login-logo {
  text-align: center;
  margin-bottom: 28px;
}
.logo-badge {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 26px;
  font-weight: 800;
  margin-bottom: 14px;
  box-shadow: 0 8px 20px rgba(59,130,246,.35);
}
.login-logo h1 { font-size: 18px; color: #0f172a; margin: 0 0 6px; }
.login-logo p { font-size: 13px; color: #94a3b8; margin: 0; }
.login-btn { width: 100%; }
.login-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 13px;
}
.login-footer a { color: #94a3b8; text-decoration: none; }
.login-footer a:hover { color: #3b82f6; }
</style>
