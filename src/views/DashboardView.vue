<template>
  <div class="dashboard">
    <!-- 数据概览卡片 -->
    <div class="overview-cards">
      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Calendar /></el-icon>
            <span>今日预订</span>
          </div>
        </template>
        <div class="card-value">{{ overview.todayBookings || 0 }}</div>
        <div class="card-label">预订数量</div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><Money /></el-icon>
            <span>今日收入</span>
          </div>
        </template>
        <div class="card-value">¥{{ overview.todayIncome?.toFixed(2) || '0.00' }}</div>
        <div class="card-label">收入金额</div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><House /></el-icon>
            <span>房间状态</span>
          </div>
        </template>
        <div class="card-value">{{ overview.occupancyRate?.toFixed(1) || '0.0' }}%</div>
        <div class="card-label">入住率</div>
      </el-card>

      <el-card class="overview-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>入住情况</span>
          </div>
        </template>
        <div class="card-value">{{ overview.checkedInToday || 0 }}</div>
        <div class="card-label">今日入住</div>
      </el-card>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 收入统计图表 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>收入统计</span>
            <el-radio-group v-model="incomeTimeRange" size="small" @change="fetchIncomeData">
              <el-radio-button label="week">周</el-radio-button>
              <el-radio-button label="month">月</el-radio-button>
              <el-radio-button label="year">年</el-radio-button>
            </el-radio-group>
          </div>
        </template>
        <div class="chart-container">
          <v-chart class="chart" :option="incomeChartOption" autoresize />
        </div>
      </el-card>

      <!-- 房型统计图表 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>房型统计</span>
          </div>
        </template>
        <div class="chart-container">
          <v-chart class="chart" :option="roomTypeChartOption" autoresize />
        </div>
      </el-card>

      <!-- 预订趋势图表 -->
      <el-card class="chart-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>预订趋势</span>
            <el-date-picker
              v-model="trendDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              size="small"
              @change="fetchTrendData"
            />
          </div>
        </template>
        <div class="chart-container">
          <v-chart class="chart" :option="trendChartOption" autoresize />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import VChart from 'vue-echarts'
import { Calendar, Money, House, User } from '@element-plus/icons-vue'
import { getOverview, getIncomeStatistics, getRoomTypeStatistics, getBookingTrends } from '@/api/statistics'
import { ElMessage } from 'element-plus'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 数据状态
const overview = ref({})
const incomeTimeRange = ref('week')
const trendDateRange = ref([])
const incomeChartOption = ref({})
const roomTypeChartOption = ref({})
const trendChartOption = ref({})

// 获取概览数据
const fetchOverview = async () => {
  try {
    const res = await getOverview()
    if (res.code === 200) {
      overview.value = res.data
    }
  } catch (error) {
    console.error('获取概览数据失败:', error)
    ElMessage.error('获取概览数据失败')
  }
}

// 获取收入统计数据
const fetchIncomeData = async () => {
  try {
    const res = await getIncomeStatistics(incomeTimeRange.value)
    if (res.code === 200) {
      const { data } = res.data
      incomeChartOption.value = {
        title: {
          text: '收入统计',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}<br/>收入: ¥{c}'
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date)
        },
        yAxis: {
          type: 'value',
          name: '收入(元)'
        },
        series: [{
          data: data.map(item => item.income),
          type: 'line',
          smooth: true,
          areaStyle: {}
        }]
      }
    }
  } catch (error) {
    console.error('获取收入统计失败:', error)
    ElMessage.error('获取收入统计失败')
  }
}

// 获取房型统计数据
const fetchRoomTypeData = async () => {
  try {
    const res = await getRoomTypeStatistics()
    if (res.code === 200) {
      const { details } = res.data
      roomTypeChartOption.value = {
        title: {
          text: '房型预订占比',
          left: 'center'
        },
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [{
          type: 'pie',
          radius: '50%',
          data: details.map(item => ({
            name: item.roomType,
            value: item.bookingCount
          })),
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
    }
  } catch (error) {
    console.error('获取房型统计失败:', error)
    ElMessage.error('获取房型统计失败')
  }
}

// 获取预订趋势数据
const fetchTrendData = async () => {
  if (!trendDateRange.value || !trendDateRange.value[0] || !trendDateRange.value[1]) {
    return
  }
  
  try {
    const [startDate, endDate] = trendDateRange.value
    const res = await getBookingTrends(startDate, endDate)
    if (res.code === 200) {
      const data = res.data
      trendChartOption.value = {
        title: {
          text: '预订趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: data.map(item => item.date)
        },
        yAxis: {
          type: 'value',
          name: '预订数量'
        },
        series: [{
          data: data.map(item => item.bookingCount),
          type: 'bar'
        }]
      }
    }
  } catch (error) {
    console.error('获取预订趋势失败:', error)
    ElMessage.error('获取预订趋势失败')
  }
}

// 初始化日期范围
const initDateRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(start.getDate() - 30)
  trendDateRange.value = [start, end]
}

// 自动刷新定时器
let refreshTimer = null

onMounted(() => {
  initDateRange()
  fetchOverview()
  fetchIncomeData()
  fetchRoomTypeData()
  fetchTrendData()
  
  // 设置自动刷新
  refreshTimer = setInterval(() => {
    fetchOverview()
  }, 60000) // 每分钟刷新一次概览数据
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.overview-card {
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin: 16px 0 8px;
}

.card-label {
  font-size: 14px;
  color: #909399;
}

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 24px;
}

.chart-card {
  transition: all 0.3s ease;
}

.chart-card:hover {
  transform: translateY(-5px);
}

.chart-container {
  height: 400px;
  padding: 16px;
}

.chart {
  width: 100%;
  height: 100%;
}

:deep(.el-card__header) {
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style> 