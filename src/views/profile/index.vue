<template>
  <div class="profile-container">
    <el-card class="user-info" :body-style="{ padding: '20px' }">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="showEditDialog = true" :icon="Edit">
            编辑
          </el-button>
        </div>
      </template>
      <div class="user-avatar">
        <el-avatar 
          :size="120" 
          :src="userInfo.avatar" 
          :icon="User"
          :style="{
            border: '4px solid var(--el-color-primary-light-8)',
            boxShadow: '0 2px 12px 0 rgba(0,0,0,0.1)'
          }"
        />
      </div>
      <div class="user-details">
        <p><strong>用户名</strong>{{ userInfo.username }}</p>
        <p><strong>姓名</strong>{{ userInfo.name }}</p>
        <p><strong>邮箱</strong>{{ userInfo.email }}</p>
        <p><strong>电话</strong>{{ userInfo.phone }}</p>
      </div>
    </el-card>

    <el-card class="booking-list" :body-style="{ padding: '10px 20px' }">
      <template #header>
        <div class="card-header">
          <span>我的预订</span>
          <el-button type="primary" text @click="loadBookings">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table 
        :data="bookings" 
        style="width: 100%"
        v-loading="loading"
        empty-text="暂无预订记录"
      >
        <el-table-column label="房间号" width="120">
          <template #default="{ row }">
            {{ row.room?.roomNumber || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="bookingNumber" label="预订号" width="180">
          <template #default="{ row }">
            {{ row.bookingNumber || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="房间类型" width="120">
          <template #default="{ row }">
            {{ row.room?.type || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="入住日期" width="120">
          <template #default="{ row }">
            {{ row.checkInDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="退房日期" width="120">
          <template #default="{ row }">
            {{ row.checkOutDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="总金额" width="120">
          <template #default="{ row }">
            {{ row.totalAmount ? `¥${row.totalAmount}` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button 
              v-if="row.status === 'pending' || row.status === 'confirmed'"
              type="danger" 
              size="small" 
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑个人信息对话框 -->
    <el-dialog 
      v-model="showEditDialog" 
      title="编辑个人信息" 
      width="500px"
    >
      <el-form 
        ref="formRef"
        :model="form" 
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="头像">
          <div class="avatar-uploader">
            <el-upload
              class="avatar-uploader"
              action="/api/upload/avatar"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
            >
              <el-avatar
                v-if="form.avatar"
                :size="100"
                :src="form.avatar"
                :icon="User"
              />
              <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">
            确认
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { User, Edit, Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentUser, updateProfile } from '@/api/user'
import { getUserBookings, cancelBooking } from '@/api/booking'

const userInfo = ref({})
const bookings = ref([])
const showEditDialog = ref(false)
const formRef = ref()

const form = ref({
  name: '',
  email: '',
  phone: '',
  avatar: ''
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' }
  ]
}

// 添加 loading 状态
const loading = ref(false)

// 获取用户信息
const loadUserInfo = async () => {
  try {
    console.log('开始加载用户信息')
    const response = await getCurrentUser()
    console.log('用户信息响应:', response)
    if (response.code === 200) {
      console.log('用户数据:', response.data)
      userInfo.value = response.data || {}
      form.value = { ...response.data }
      console.log('设置后的 userInfo:', userInfo.value)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

// 获取预订列表
const loadBookings = async () => {
  loading.value = true
  try {
    console.log('开始加载预订列表')
    const response = await getUserBookings()
    console.log('预订列表响应:', response)
    if (response.code === 200) {
      console.log('预订数据:', response.data)
      bookings.value = response.data || []
      console.log('设置后的 bookings:', bookings.value)
    }
  } catch (error) {
    console.error('获取预订列表失败:', error)
    ElMessage.error('获取预订列表失败')
  } finally {
    loading.value = false
  }
}

// 处理头像上传
const handleAvatarSuccess = (response) => {
  form.value.avatar = response.data
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG 或 PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
  }
  return isJPG && isLt2M
}

// 提交个人信息
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await updateProfile(form.value)
        userInfo.value = response.data
        showEditDialog.value = false
        ElMessage.success('更新成功')
      } catch (error) {
        ElMessage.error('更新失败')
      }
    }
  })
}

// 取消预订
const handleCancel = async (booking) => {
  try {
    await ElMessageBox.confirm('确定要取消该预订吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelBooking(booking.id)
    ElMessage.success('取消成功')
    loadBookings()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'pending': 'warning',
    'confirmed': 'success',
    'checked_in': 'primary',
    'cancelled': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'pending': '待确认',
    'confirmed': '已确认',
    'checked_in': '已入住',
    'cancelled': '已取消'
  }
  return statusMap[status] || status
}

onMounted(() => {
  loadUserInfo()
  loadBookings()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100%;
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 20px;
}

.user-info {
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-avatar {
  text-align: center;
  margin: 30px 0;
  position: relative;
}

.user-avatar::after {
  content: '';
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 1px;
  background-color: var(--el-border-color-lighter);
}

.user-details {
  padding: 0 20px;
}

.user-details p {
  margin: 15px 0;
  display: flex;
  align-items: center;
  color: var(--el-text-color-primary);
}

.user-details p strong {
  min-width: 80px;
  color: var(--el-text-color-secondary);
}

.booking-list :deep(.el-card__header) {
  border-bottom: none;
  padding-bottom: 0;
}

.booking-list :deep(.el-card__body) {
  padding-top: 10px;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader .el-upload {
  border: 2px dashed var(--el-border-color-lighter);
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
}

:deep(.el-table) {
  --el-table-border-color: var(--el-border-color-lighter);
  --el-table-header-bg-color: var(--el-fill-color-light);
}

:deep(.el-table th) {
  font-weight: 600;
}

.dialog-footer {
  padding-top: 20px;
  text-align: right;
}

@media screen and (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
  
  .user-details {
    padding: 0 10px;
  }
}
</style> 