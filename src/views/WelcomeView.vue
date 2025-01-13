<template>
  <div class="welcome-container">
    <!-- 欢迎卡片 -->
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="welcome-card" shadow="hover">
          <div class="welcome-header">
            <el-avatar :size="80" :icon="User" class="user-avatar" />
            <div class="welcome-text">
              <h2>欢迎回来，{{ userInfo.name || '尊敬的用户' }}</h2>
              <p>祝您入住愉快！</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快捷入口 -->
    <el-row :gutter="20" class="shortcut-row">
      <el-col :span="8">
        <el-card class="shortcut-card" shadow="hover" @click="handleNavigate('/book-room')">
          <div class="shortcut-content">
            <el-icon class="shortcut-icon"><Calendar /></el-icon>
            <h3>预订房间</h3>
            <p>浏览房间信息并在线预订</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="shortcut-card" shadow="hover" @click="handleNavigate('/profile')">
          <div class="shortcut-content">
            <el-icon class="shortcut-icon"><Document /></el-icon>
            <h3>我的预订</h3>
            <p>查看和管理您的预订记录</p>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="shortcut-card" shadow="hover" @click="handleNavigate('/profile')">
          <div class="shortcut-content">
            <el-icon class="shortcut-icon"><User /></el-icon>
            <h3>个人中心</h3>
            <p>管理您的个人信息</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 酒店信息展示 -->
    <el-row :gutter="20" class="info-row">
      <el-col :span="12">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>酒店设施</span>
            </div>
          </template>
          <div class="facility-list">
            <div class="facility-item">
              <el-icon><WifiIcon /></el-icon>
              <span>免费WiFi</span>
            </div>
            <div class="facility-item">
              <el-icon><Van /></el-icon>
              <span>免费停车</span>
            </div>
            <div class="facility-item">
              <el-icon><Food /></el-icon>
              <span>餐厅</span>
            </div>
            <div class="facility-item">
              <el-icon><Service /></el-icon>
              <span>24小时服务</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>联系我们</span>
            </div>
          </template>
          <div class="contact-list">
            <p><el-icon><Location /></el-icon> 地址：XX市XX区XX路XX号</p>
            <p><el-icon><Phone /></el-icon> 电话：400-XXX-XXXX</p>
            <p><el-icon><Message /></el-icon> 邮箱：service@hotel.com</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getProfile } from '@/api/user'
import {
  User,
  Calendar,
  Document,
  Location,
  Phone,
  Message,
  Service,
  Van,
  Food
} from '@element-plus/icons-vue'

const router = useRouter()
const userInfo = ref({})

// 获取用户信息
const loadUserInfo = async () => {
  try {
    const res = await getProfile()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 页面导航
const handleNavigate = (path) => {
  router.push(path)
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.welcome-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.welcome-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  color: white;
}

.welcome-header {
  display: flex;
  align-items: center;
  padding: 20px;
}

.user-avatar {
  margin-right: 20px;
  border: 4px solid rgba(255, 255, 255, 0.3);
}

.welcome-text {
  flex: 1;
}

.welcome-text h2 {
  margin: 0 0 10px 0;
  font-size: 24px;
}

.welcome-text p {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.shortcut-row {
  margin-bottom: 20px;
}

.shortcut-card {
  height: 200px;
  cursor: pointer;
  transition: transform 0.3s;
}

.shortcut-card:hover {
  transform: translateY(-5px);
}

.shortcut-content {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.shortcut-icon {
  font-size: 40px;
  color: #1890ff;
  margin-bottom: 15px;
}

.shortcut-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.shortcut-content p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.info-row {
  margin-bottom: 20px;
}

.info-card {
  height: 100%;
}

.card-header {
  font-size: 16px;
  font-weight: bold;
}

.facility-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  padding: 10px;
}

.facility-item {
  display: flex;
  align-items: center;
  font-size: 16px;
}

.facility-item .el-icon {
  margin-right: 10px;
  font-size: 20px;
  color: #1890ff;
}

.contact-list {
  padding: 10px;
}

.contact-list p {
  margin: 15px 0;
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #606266;
}

.contact-list .el-icon {
  margin-right: 10px;
  font-size: 18px;
  color: #1890ff;
}
</style> 