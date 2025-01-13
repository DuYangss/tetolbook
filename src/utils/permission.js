import { getCurrentUser as fetchCurrentUser } from '@/api/user'

let currentUser = null

export async function loadCurrentUser() {
  try {
    currentUser = await fetchCurrentUser()
    return currentUser
  } catch (error) {
    console.error('获取当前用户信息失败:', error)
    return null
  }
}

export function isAdmin() {
  const role = localStorage.getItem('role')
  return role === 'ADMIN'
}

// 权限映射表
const permissionMap = {
  'ADMIN': [
    'user:manage',
    'room:manage',
    'booking:manage',
    'statistics:view'
  ],
  'MANAGER': [
    'room:manage',
    'booking:manage',
    'statistics:view'
  ],
  'STAFF': [
    'booking:manage'
  ]
}

// 检查用户是否有指定权限
export function hasPermission(permission) {
  const role = localStorage.getItem('role')
  if (!role) return false
  
  const permissions = permissionMap[role]
  if (!permissions) return false
  
  return permissions.includes(permission)
}

// 检查用户是否有指定角色
export function hasRole(role) {
  const userRole = localStorage.getItem('role')
  return userRole === role
}

// 检查用户是否已认证
export function isAuthenticated() {
  return !!localStorage.getItem('token')
}

// 获取本地存储的用户信息
export function getLocalUserInfo() {
  return {
    id: localStorage.getItem('id'),
    username: localStorage.getItem('username'),
    name: localStorage.getItem('name'),
    role: localStorage.getItem('role'),
    avatar: localStorage.getItem('avatar')
  }
}

// 清除用户信息
export function clearUserInfo() {
  localStorage.clear()
}

export function getCurrentUserInfo() {
  return currentUser
} 