<template>
  <div class="book-room-container">
    <el-card class="room-list">
      <template #header>
        <div class="card-header">
          <span>可预订房间</span>
          <el-button type="primary" text @click="loadRooms">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>

      <el-table 
        :data="rooms" 
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无可预订房间"
      >
        <el-table-column label="房间号" prop="roomNumber" width="120" />
        <el-table-column label="房间类型" prop="type" width="120" />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleBook(row)"
            >
              预订
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预订对话框 -->
    <el-dialog 
      v-model="showBookDialog" 
      title="预订房间" 
      width="500px"
    >
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="房间号">
          <span>{{ selectedRoom?.roomNumber }}</span>
        </el-form-item>
        <el-form-item label="房间类型">
          <span>{{ selectedRoom?.type }}</span>
        </el-form-item>
        <el-form-item label="价格">
          <span>¥{{ selectedRoom?.price }}</span>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="form.checkInDate"
            type="date"
            placeholder="选择入住日期"
            :disabled-date="disablePastDates"
          />
        </el-form-item>
        <el-form-item label="退房日期" prop="checkOutDate">
          <el-date-picker
            v-model="form.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            :disabled-date="disableInvalidDates"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBookDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确认预订
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

const rooms = ref([])
const loading = ref(false)
const showBookDialog = ref(false)
const selectedRoom = ref(null)
const formRef = ref()

const form = ref({
  checkInDate: '',
  checkOutDate: ''
})

const rules = {
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择退房日期', trigger: 'change' }
  ]
}

// 创建axios实例并配置
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 加载可预订房间列表
const loadRooms = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/rooms', {
      params: {
        status: '空闲'
      }
    })
    if (response.data.code === 200) {
      rooms.value = response.data.data
    }
  } catch (error) {
    if (error.response?.status === 403) {
      ElMessage.error('无权访问，请确认您的权限')
    } else {
      ElMessage.error('获取房间列表失败')
    }
  } finally {
    loading.value = false
  }
}

// 处理预订按钮点击
const handleBook = (room) => {
  selectedRoom.value = room
  form.value = {
    checkInDate: '',
    checkOutDate: ''
  }
  showBookDialog.value = true
}

// 禁用过去的日期
const disablePastDates = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

// 禁用无效的退房日期（早于入住日期）
const disableInvalidDates = (date) => {
  if (!form.value.checkInDate) return disablePastDates(date)
  return date <= form.value.checkInDate
}

// 提交预订
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await request.post('/api/bookings', {
          roomId: selectedRoom.value.id,
          checkInDate: form.value.checkInDate,
          checkOutDate: form.value.checkOutDate
        })
        
        if (response.data.code === 200) {
          ElMessage.success('预订成功')
          showBookDialog.value = false
          loadRooms()  // 刷新房间列表
        }
      } catch (error) {
        if (error.response?.status === 403) {
          ElMessage.error('无权进行预订，请确认您的权限')
        } else {
          ElMessage.error('预订失败')
        }
      }
    }
  })
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
.book-room-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

:deep(.el-form-item__label) {
  font-weight: bold;
}
</style> 