import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { hasPermission } from '@/utils/permission'
import Layout from '@/layout/index.vue'
import LoginView from '@/views/LoginView.vue'
import UserManagementView from '@/views/system/user/index.vue'
import RoomManagementView from '@/views/RoomManagementView.vue'
import BookingManagementView from '@/views/BookingManagementView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        requiresAuth: false,
        title: '登录'
      }
    },
    {
      path: '/',
      component: Layout,
      redirect: '/welcome',
      meta: {
        requiresAuth: true
      },
      children: [
        {
          path: 'welcome',
          name: 'Welcome',
          component: () => import('@/views/WelcomeView.vue'),
          meta: {
            requiresAuth: true,
            title: '首页',
            icon: 'Monitor'
          }
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/dashboard/index.vue'),
          meta: {
            requiresAuth: true,
            permission: 'statistics:view',
            title: '数据统计分析',
            icon: 'DataLine'
          }
        },
        {
          path: 'rooms',
          name: 'RoomManagement',
          component: RoomManagementView,
          meta: {
            requiresAuth: true,
            permission: 'room:manage',
            title: '房间管理',
            icon: 'House'
          }
        },
        {
          path: 'bookings',
          name: 'BookingManagement',
          component: BookingManagementView,
          meta: {
            requiresAuth: true,
            permission: 'booking:manage',
            title: '预订管理',
            icon: 'Calendar'
          }
        },
        {
          path: 'users',
          name: 'UserManagement',
          component: UserManagementView,
          meta: {
            requiresAuth: true,
            permission: 'user:manage',
            title: '用户管理',
            icon: 'User'
          }
        },
        {
          path: 'book-room',
          name: 'BookRoom',
          component: () => import('@/views/book-room/index.vue'),
          meta: { 
            title: '预订吧',
            icon: 'Ticket',
            requiresAuth: true
          }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/profile/index.vue'),
          meta: {
            requiresAuth: true,
            title: '个人中心',
            icon: 'User',
            noCache: true
          }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  console.log('路由守卫 - 目标路由:', to.path)
  console.log('路由守卫 - 来源路由:', from.path)
  
  const token = localStorage.getItem('token')
  
  // 检查是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      console.log('路由守卫 - 需要认证，跳转到登录页')
      ElMessage.warning('请先登录')
      next('/login')
      return
    }
    
    // 检查权限（仅检查有permission要求的路由）
    const permission = to.meta.permission
    if (permission && !hasPermission(permission)) {
      console.log('路由守卫 - 权限不足')
      ElMessage.error('无权访问该页面')
      if (from.path === '/login') {
        // 如果是从登录页来的，跳转到首页
        next('/')
      } else {
        // 否则返回上一页
        next(false)
      }
      return
    }
  }
  
  // 已登录用户访问登录页时重定向到首页
  if (to.path === '/login' && token) {
    console.log('路由守卫 - 已登录，自动跳转到首页')
    next('/')
    return
  }
  
  console.log('路由守卫 - 允许访问:', to.path)
  next()
})

export default router 