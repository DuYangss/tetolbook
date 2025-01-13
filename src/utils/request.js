import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
  withCredentials: true
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    
    if (error.response?.status === 401 || error.response?.status === 403) {
      // 清除所有用户信息
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('name')
      localStorage.removeItem('role')
      
      // 如果不是登录页面，才显示提示并跳转
      if (!router.currentRoute.value.path.includes('/login')) {
        ElMessage.error('登录已过期，请重新登录')
        router.push('/login')
      }
    } else {
      // 其他错误显示错误信息
      const message = error.response?.data?.message || '请求失败'
      ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

export default service 