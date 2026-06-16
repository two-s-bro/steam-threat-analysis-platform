import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
  { path: '/iocs', name: 'IocList', component: () => import('../views/IocList.vue') },
  { path: '/iocs/:id', name: 'IocDetail', component: () => import('../views/IocDetail.vue') },
  { path: '/timeline', name: 'Timeline', component: () => import('../views/Timeline.vue') },
  { path: '/components', name: 'ComponentGraph', component: () => import('../views/ComponentGraph.vue') },
  { path: '/yara', name: 'YaraRules', component: () => import('../views/YaraRules.vue') },
  { path: '/logs', name: 'LogViewer', component: () => import('../views/LogViewer.vue') }
]

const router = createRouter({ history: createWebHistory(), routes })
export default router
