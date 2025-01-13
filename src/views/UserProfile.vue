<template>
  <div class="user-profile">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h3>个人信息</h3>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload"
          >
            <el-avatar
              v-if="userForm.avatar"
              :size="100"
              :src="userForm.avatar"
            />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item label="用户名">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="booking-card">
      <template #header>
        <div class="card-header">
          <h3>我的预订</h3>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="当前预订" name="current">
          <el-table :data="currentBookings" v-loading="loading">
            <el-table-column prop="roomNumber" label="房间号" />
            <el-table-column prop="checkInDate" label="入住日期" />
            <el-table-column prop="checkOutDate" label="退房日期" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewBookingDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="历史预订" name="history">
          <el-table :data="historyBookings" v-loading="loading">
            <el-table-column prop="roomNumber" label="房间号" />
            <el-table-column prop="checkInDate" label="入住日期" />
            <el-table-column prop="checkOutDate" label="退房日期" />
            <el-table-column prop="status" label="状态">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewBookingDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCurrentUser, updateUserProfile } from '@/api/user'
import { getUserBookings } from '@/api/booking'

const formRef = ref(null)
const loading = ref(false)
const activeTab = ref('current')

// 用户表单数据
const userForm = ref({
  username: '',
  name: '',
  phone: '',
  email: '',
  avatar: ''
})

// 预订数据
const currentBookings = ref([])
const historyBookings = ref([])

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取用户信息
const loadUserInfo = async () => {
  try {
    const response = await getCurrentUser()
    const userData = response.data
    userForm.value = {
      username: userData.username,
      name: userData.name,
      phone: userData.phone,
      email: userData.email,
      avatar: userData.avatar
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 获取预订信息
const loadBookings = async () => {
  loading.value = true
  try {
    const response = await getUserBookings()
    const bookings = response.data
    currentBookings.value = bookings.filter(booking => 
      ['PENDING', 'CONFIRMED'].includes(booking.status)
    )
    historyBookings.value = bookings.filter(booking => 
      ['COMPLETED', 'CANCELLED'].includes(booking.status)
    )
  } catch (error) {
    ElMessage.error('获取预订信息失败')
  } finally {
    loading.value = false
  }
}

// 处理头像上传
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    userForm.value.avatar = response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 上传前校验
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    await updateUserProfile(userForm.value)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 查看预订详情
const viewBookingDetail = (booking) => {
  // TODO: 实现查看预订详情的功能
  console.log('查看预订详情:', booking)
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'CONFIRMED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待确认',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

// 初始化
onMounted(() => {
  loadUserInfo()
  loadBookings()
})
</script>

<style scoped>
.user-profile {
  padding: 20px;
  display: flex;
  gap: 20px;
}

.profile-card {
  flex: 1;
  max-width: 500px;
}

.booking-card {
  flex: 2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.avatar-uploader {
  text-align: center;
  margin-bottom: 20px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
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

@media (max-width: 768px) {
  .user-profile {
    flex-direction: column;
  }
  
  .profile-card,
  .booking-card {
    max-width: none;
  }
}
</style> 