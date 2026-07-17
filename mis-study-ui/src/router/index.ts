//createRouter：创建路由实例的核心函数。
// createWebHistory：使用 HTML5 历史模式（地址栏不带 #），适合现代浏览器。
// RouteRecordRaw：TypeScript 类型，定义路由配置的数据结构（routes 数组必须符合这个类型）。
// AppLayout：布局组件，所有子页面都会在这个布局内渲染（比如侧边栏、顶部导航）。
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layout/AppLayout.vue'

//路由配置数组 routes
const routes: RouteRecordRaw[] = [
  {
    path: '/', //根路径，访问网站首页时匹配。
    component: AppLayout, //根路径会渲染 AppLayout 这个布局组件
    redirect: '/clients', //访问 / 时自动重定向到 /clients，所以一打开页面默认显示“客户管理”。

    //子路由，它们的路径会拼在父路径后面（如 /clients、/project-info）。
    // 这些子页面会在 AppLayout 的 <router-view> 位置渲染。
    children: [
      {
        path: 'clients', // 实际路径为 /clients
        name: 'Clients', // 命名路由，方便编程式导航
        //component 使用箭头函数 + import() 实现懒加载：只有访问该页面时才会加载对应的 JS 文件，提升首屏加载速度。
        component: () => import('@/views/projectManagement/clients/index.vue'),
        meta: {
          // 自定义元信息
          title: '客户管理', // 页面标题
          menu: true, // 是否显示在菜单中（用于动态生成菜单）
          order: 1, // 菜单排序
          group: '项目管理',
          groupOrder: 1,
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
          group: '项目管理',
          groupOrder: 1,
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
          group: '项目管理',
          groupOrder: 1,
        },
      },
      {
        path: 'system/dept',
        name: 'SystemDept',
        component: () => import('@/views/system/dept/index.vue'),
        meta: {
          title: '部门管理',
          menu: true,
          order: 4,
          group: '系统管理',
          groupOrder: 2,
        },
      },
      {
        path: 'system/post',
        name: 'SystemPost',
        component: () => import('@/views/system/post/index.vue'),
        meta: {
          title: '岗位管理',
          menu: true,
          order: 5,
          group: '系统管理',
          groupOrder: 2,
        },
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: {
          title: '用户管理',
          menu: true,
          order: 6,
          group: '系统管理',
          groupOrder: 2,
        },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: {
          title: '角色管理',
          menu: true,
          order: 7,
          group: '系统管理',
          groupOrder: 2,
        },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: {
          title: '菜单管理',
          menu: true,
          order: 8,
          group: '系统管理',
          groupOrder: 2,
        },
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: {
          title: '参数设置',
          menu: true,
          order: 9,
          group: '系统管理',
          groupOrder: 2,
        },
      },
    ],
  },
]

//创建路由实例（固定写法）
const router = createRouter({
  //history：采用 createWebHistory，基础路径从环境变量 BASE_URL 读取（默认是 /，如果部署在子目录可以修改）
  history: createWebHistory(import.meta.env.BASE_URL),
  //传入上面定义的路由数组。
  routes,
})

//全局后置钩子 router.afterEach
//动态修改浏览器标签页标题：如果有标题则显示“标题 - 管理信息系统”，否则只显示“管理信息系统”。
router.afterEach((to) => {
  const title = typeof to.meta.title === 'string' ? to.meta.title : ''
  document.title = title ? `${title} - 管理信息系统` : '管理信息系统'
})

//导出路由实例
export default router
