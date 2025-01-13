import request from '@/utils/request'

const BASE_URL = 'http://localhost:8080/api/rooms'

// 获取所有房间
export function getAllRooms(params = {}) {
  // 移除空值参数
  const validParams = {}
  Object.keys(params).forEach(key => {
    if (params[key] !== '' && params[key] != null) {
      validParams[key] = params[key]
    }
  })
  
  return request({
    url: BASE_URL,
    method: 'get',
    params: validParams  // 只传递有值的参数
  })
}

// 获取可用房间
export function getAvailableRooms(params) {
  return request({
    url: `${BASE_URL}/available`,
    method: 'get',
    params,
    headers: {
      'Content-Type': 'application/json'
    }
  })
}

// 获取房间详情
export function getRoomById(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'get'
  })
}

// 创建房间
export function createRoom(data) {
  return request({
    url: BASE_URL,
    method: 'post',
    data
  })
}

// 更新房间
export function updateRoom(id, data) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'put',
    data
  })
}

// 删除房间
export function deleteRoom(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'delete'
  })
}

// 更新房间状态
export function updateRoomStatus(id, status) {
  return request({
    url: `http://localhost:8080/api/rooms/${id}/status`,
    method: 'patch',
    params: { status }
  })
}

// 检查房间是否有预订记录
export function checkRoomBookings(id) {
  return request({
    url: `${BASE_URL}/${id}/check-bookings`,
    method: 'get'
  })
} 