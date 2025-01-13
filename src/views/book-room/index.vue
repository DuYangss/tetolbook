<template>
  <div class="book-room-container">
    <!-- 房间列表卡片 -->
    <el-card class="room-list">
      <template #header>
        <div class="card-header">
          <span>可预订房间</span>
          <el-button type="primary" @click="loadRooms">
            <el-icon><Refresh /></el-icon>
            刷新列表
          </el-button>
        </div>
      </template>

      <el-table 
        :data="rooms" 
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无可预订房间"
      >
        <el-table-column label="房间号" prop="roomNumber" width="100" />
        <el-table-column label="房间类型" prop="type" width="120" />
        <el-table-column label="价格/晚" width="100">
          <template #default="{ row }">
            ¥{{ row.price }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag type="success" v-if="row.status === '空闲'">{{ row.status }}</el-tag>
            <el-tag type="info" v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="description" min-width="200" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleBook(row)"
              :disabled="row.status !== '空闲'"
            >
              预订
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 预订表单对话框 -->
    <el-dialog 
      v-model="showBookDialog" 
      title="预订房间" 
      width="600px"
      destroy-on-close
    >
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="100px"
        status-icon
      >
        <!-- 房间信息展示 -->
        <el-form-item label="房间信息">
          <div class="room-info">
            <p><strong>房间号：</strong>{{ selectedRoom?.roomNumber }}</p>
            <p><strong>类型：</strong>{{ selectedRoom?.type }}</p>
            <p><strong>价格：</strong>¥{{ selectedRoom?.price }}/晚</p>
          </div>
        </el-form-item>

        <!-- 预订信息表单 -->
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号" />
        </el-form-item>

        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="form.checkInDate"
            type="date"
            placeholder="选择入住日期"
            :disabled-date="disablePastDates"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="退房日期" prop="checkOutDate">
          <el-date-picker
            v-model="form.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            :disabled-date="disableInvalidDates"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="总金额">
          <span class="total-amount">¥{{ totalAmount }}</span>
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="form.remarks"
            type="textarea"
            placeholder="请输入备注信息（选填）"
            :rows="2"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showBookDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确认预订
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

// 状态变量
const rooms = ref([])
const loading = ref(false)
const submitting = ref(false)
const showBookDialog = ref(false)
const selectedRoom = ref(null)
const formRef = ref()

// 表单数据
const form = ref({
  customerName: '',
  phone: '',
  idCard: '',
  checkInDate: '',
  checkOutDate: '',
  remarks: ''
})

// 表单验证规则
const rules = {
  customerName: [
    { required: true, message: '请输入客户姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  idCard: [
    { required: true, message: '请输入身份证号', trigger: 'blur' },
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择退房日期', trigger: 'change' }
  ]
}

// 计算预订天数
const bookingDays = computed(() => {
  const { checkInDate, checkOutDate } = form.value
  if (!checkInDate || !checkOutDate) return 0
  const start = new Date(checkInDate)
  const end = new Date(checkOutDate)
  return Math.ceil((end - start) / (1000 * 60 * 60 * 24))
})

// 计算总金额
const totalAmount = computed(() => {
  if (selectedRoom.value && bookingDays.value > 0) {
    return selectedRoom.value.price * bookingDays.value
  }
  return 0
})

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 403) {
      ElMessage.error('无权限访问，请重新登录')
      // 可以在这里处理登录过期的情况，比如跳转到登录页面
      // router.push('/login')
    }
    return Promise.reject(error)
  }
)

// 加载可预订房间列表
const loadRooms = async () => {
  loading.value = true
  try {
    const response = await request.get('/api/rooms', {  // 使用现有的房间列表接口
      params: {
        status: '空闲'
      }
    })
    console.log('获取房间列表响应:', response.data)
    if (response.data.code === 200) {
      rooms.value = response.data.data
    } else {
      ElMessage.error(response.data.message || '获取房间列表失败')
    }
  } catch (error) {
    console.error('获取房间列表错误:', error)
    ElMessage.error('获取房间列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 处理预订按钮点击
const handleBook = (room) => {
  selectedRoom.value = room
  form.value = {
    customerName: '',
    phone: '',
    idCard: '',
    checkInDate: '',
    checkOutDate: '',
    remarks: ''
  }
  showBookDialog.value = true
}

// 禁用过去的日期
const disablePastDates = (date) => {
  return date < new Date(new Date().setHours(0, 0, 0, 0))
}

// 禁用无效的退房日期
const disableInvalidDates = (date) => {
  if (!form.value.checkInDate) return disablePastDates(date)
  return date <= new Date(form.value.checkInDate)
}

// 提交预订
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const bookingData = {
          room: {
            id: selectedRoom.value.id
          },
          roomType: selectedRoom.value.type,
          customerName: form.value.customerName,
          phone: form.value.phone,
          idCard: form.value.idCard,
          checkInDate: form.value.checkInDate,
          checkOutDate: form.value.checkOutDate,
          remarks: form.value.remarks || '',
          totalAmount: totalAmount.value
        }

        console.log('提交预订数据:', bookingData)
        const response = await request.post('/api/bookings/user-booking', bookingData)
        console.log('预订响应:', response.data)
        
        if (response.data.code === 200) {
          ElMessage.success('预订成功')
          showBookDialog.value = false
          loadRooms()  // 刷新房间列表
        } else {
          ElMessage.error(response.data.message || '预订失败')
        }
      } catch (error) {
        console.error('预订错误:', error.response?.data || error)
        ElMessage.error(error.response?.data?.message || '预订失败，请稍后重试')
      }
    }
  })
}

// 页面加载时获取房间列表
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

.room-info {
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.room-info p {
  margin: 8px 0;
  line-height: 1.4;
}

.room-info strong {
  display: inline-block;
  width: 70px;
  color: #606266;
}

.total-amount {
  font-size: 20px;
  color: #f56c6c;
  font-weight: bold;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

:deep(.el-form-item__label) {
  font-weight: bold;
}
</style> 