import request from '@/utils/request'

// 获取概览数据
export function getOverview() {
  return request({
    url: '/api/statistics/overview',
    method: 'get'
  })
}

// 获取收入统计
export function getIncomeStatistics(timeRange) {
  return request({
    url: '/api/statistics/income',
    method: 'get',
    params: { timeRange }
  })
}

// 获取房型统计
export function getRoomTypeStatistics() {
  return request({
    url: '/api/statistics/room-types',
    method: 'get'
  })
}

// 获取预订趋势
export function getBookingTrends(startDate, endDate) {
  return request({
    url: '/api/statistics/booking-trends',
    method: 'get',
    params: {
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0]
    }
  })
} 