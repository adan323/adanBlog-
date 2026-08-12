import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/login', name: 'login', component: () => import('./views/Login.vue') },
  {
    path: '/',
    component: () => import('./layout/AdminLayout.vue'),
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('./views/Dashboard.vue') },
      { path: 'articles', name: 'articles', component: () => import('./views/ArticleList.vue') },
      { path: 'articles/new', name: 'article-new', component: () => import('./views/ArticleEdit.vue') },
      { path: 'articles/:id/edit', name: 'article-edit', component: () => import('./views/ArticleEdit.vue') },
      { path: 'tags', name: 'tags', component: () => import('./views/TagManage.vue') },
      { path: 'settings', name: 'settings', component: () => import('./views/SiteSettings.vue') },
      { path: 'password', name: 'password', component: () => import('./views/ChangePassword.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (to.name !== 'login' && !token) {
    return { name: 'login' }
  }
  if (to.name === 'login' && token) {
    return { name: 'dashboard' }
  }
})

export default router
