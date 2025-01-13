import request from '@/utils/request'

const BASE_URL = 'http://localhost:8080/api/bookings'

// 获取预订列表
export function getBookings(params) {
  return request({
    url: BASE_URL,
    method: 'get',
    params
  })
}

// 获取预订详情
export function getBooking(id) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'get'
  })
}

// 创建预订
export function createBooking(data) {
  return request({
    url: BASE_URL,
    method: 'post',
    data
  })
}

// 更新预订
export function updateBooking(id, data) {
  return request({
    url: `${BASE_URL}/${id}`,
    method: 'put',
    data
  })
}

// 确认预订
export function confirmBooking(id) {
  return request({
    url: `${BASE_URL}/${id}/confirm`,
    method: 'patch'
  })
}

// 取消预订
export function cancelBooking(id) {
  return request({
    url: `${BASE_URL}/${id}/cancel`,
    method: 'patch'
  })
}

// 办理入住
export function checkIn(id) {
  return request({
    url: `${BASE_URL}/${id}/check-in`,
    method: 'patch'
  })
}

// 检查房间可用性
export function checkRoomAvailability(params) {
  return request({
    url: `${BASE_URL}/room-available`,
    method: 'get',
    params
  })
}

// 获取用户预订列表
export function getUserBookings() {
  return request({
    url: '/api/bookings/my',
    method: 'get'
  })
} 