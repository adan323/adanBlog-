<template>
  <el-container class="admin-layout">
    <el-aside width="210px" class="admin-aside">
      <div class="admin-logo">
        <span class="logo-badge">A</span>
        <span>adan 博客后台</span>
      </div>
      <el-menu :default-active="activeMenu" router class="admin-menu">
        <el-menu-item index="/dashboard">
          <el-icon><DataBoard /></el-icon><span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/articles">
          <el-icon><Document /></el-icon><span>文章管理</span>
        </el-menu-item>
        <el-menu-item index="/articles/new">
          <el-icon><EditPen /></el-icon><span>写文章</span>
        </el-menu-item>
        <el-menu-item index="/tags">
          <el-icon><CollectionTag /></el-icon><span>标签管理</span>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon><span>站点设置</span>
        </el-menu-item>
        <el-menu-item index="/password">
          <el-icon><Lock /></el-icon><span>修改密码</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="admin-header">
        <div class="flex items-center gap-3">
          <el-tag size="small" type="primary" effect="light">后台管理</el-tag>
          <a href="/" target="_blank" class="view-site">查看博客 →</a>
        </div>
        <el-dropdown @command="handleCommand">
          <span class="user-chip">
            <el-icon><User /></el-icon>
            {{ username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="password">修改密码</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="admin-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('admin_username') || 'admin'

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/articles')) return '/articles'
  return p
})

function handleCommand(cmd) {
  if (cmd === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
    router.push('/login')
  } else if (cmd === 'password') {
    router.push('/password')
  }
}
</script>

<style scoped>
.admin-layout { height: 100vh; }
.admin-aside {
  background: #0f172a;
  display: flex;
  flex-direction: column;
}
.admin-logo {
  height: 60px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 20px;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  border-bottom: 1px solid rgba(148,163,184,.15);
  flex-shrink: 0;
}
.logo-badge {
  width: 30px; height: 30px; border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  display: flex; align-items: center; justify-content: center;
  font-weight: 800; color: #fff;
}
.admin-menu {
  border-right: none;
  background: transparent;
  flex: 1;
  --el-menu-text-color: #94a3b8;
  --el-menu-hover-bg-color: rgba(148,163,184,.1);
  --el-menu-active-color: #60a5fa;
  --el-menu-bg-color: transparent;
}
.admin-menu .el-menu-item.is-active {
  background: rgba(59,130,246,.15);
}
.admin-header {
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}
.view-site {
  font-size: 13px;
  color: #3b82f6;
  text-decoration: none;
}
.view-site:hover { text-decoration: underline; }
.user-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #334155;
  outline: none;
}
.admin-main {
  background: #f5f7fa;
  padding: 24px;
  overflow-y: auto;
}
</style>
