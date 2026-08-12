import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('./views/Home.vue') },
  { path: '/post/:slug', name: 'post', component: () => import('./views/PostDetail.vue') },
  { path: '/archive', name: 'archive', component: () => import('./views/Archive.vue') },
  { path: '/tags', name: 'tags', component: () => import('./views/Tags.vue') },
  { path: '/tag/:slug', name: 'tag', component: () => import('./views/TagPosts.vue') },
  { path: '/about', name: 'about', component: () => import('./views/About.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) return savedPosition
    return { top: 0, behavior: 'instant' }
  },
})

export default router
