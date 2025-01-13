import request from '@/utils/request'

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/api/users/current',
    method: 'get'
  })
}

// 获取用户列表
export function searchUsers(params) {
  return request({
    url: '/api/management/users',
    method: 'get',
    params
  })
}

// 创建用户
export function createUser(data) {
  return request({
    url: '/api/management/users',
    method: 'post',
    data
  })
}

// 更新用户
export function updateUser(id, data) {
  return request({
    url: `/api/management/users/${id}`,
    method: 'put',
    data
  })
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/management/users/${id}`,
    method: 'delete'
  })
}

// 更新用户状态
export function updateUserStatus(id, enabled) {
  return request({
    url: `/api/management/users/${id}/status`,
    method: 'patch',
    params: { enabled }
  })
}

// 检查用户名是否存在
export function checkUsername(username) {
  return request({
    url: '/api/management/users/check-username',
    method: 'get',
    params: { username }
  })
}

// 获取所有角色
export function getAllRoles() {
  return request({
    url: '/api/management/users/roles',
    method: 'get'
  })
}

// 更新用户资料
export function updateProfile(data) {
  return request({
    url: '/api/users/profile',
    method: 'put',
    data
  })
} 