import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: AppLayout,
    redirect: '/clients',
    children: [
      {
        path: 'clients',
        name: 'Clients',
        component: () => import('@/views/projectManagement/clients/index.vue'),
        meta: {
          title: '客户管理',
          menu: true,
          order: 1,
        },
      },
      {
        path: 'project-info',
        name: 'ProjectInfo',
        component: () => import('@/views/projectManagement/projectInfo/index.vue'),
        meta: {
          title: '项目基本信息',
          menu: true,
          order: 2,
        },
      },
      {
        path: 'vendors',
        name: 'Vendors',
        component: () => import('@/views/projectManagement/vendor/index.vue'),
        meta: {
          title: '供应商管理',
          menu: true,
          order: 3,
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : ''
  document.title = title ? `${title} - 项目管理` : '项目管理'
})

export default router
