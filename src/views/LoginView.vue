<template>
  <div class="login-container">
    <bubble-effect />
    <div class="login-box">
      <el-card class="login-card">
        <template #header>
          <div class="title-container">
            <h2 class="login-title">酒店管理系统</h2>
            <p class="login-subtitle">欢迎回来</p>
          </div>
        </template>
        <el-form :model="loginForm" :rules="rules" ref="formRef">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              prefix-icon="User"
              class="custom-input">
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              class="custom-input">
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="handleLogin" 
              :loading="loading"
              class="login-button">
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import BubbleEffect from '@/components/BubbleEffect.vue'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return
  
  try {
    loading.value = true
    
    // 表单验证
    await formRef.value.validate()
    
    console.log('表单验证通过，开始登录请求')
    
    // 登录请求
    const response = await login({ 
      username: loginForm.value.username, 
      password: loginForm.value.password 
    })
    
    console.log('登录响应数据:', response)
    
    // 验证响应数据
    if (!response?.token) {
      throw new Error('登录响应数据无效')
    }
    
    const { token, username, name, role } = response
    
    // 保存用户信息到localStorage
    localStorage.setItem('token', token)
    localStorage.setItem('username', username)
    localStorage.setItem('name', name)
    localStorage.setItem('role', role)
    
    console.log('用户信息已保存:', {
      token: token.substring(0, 10) + '...',
      username,
      name,
      role
    })
    
    ElMessage.success('登录成功')
    
    // 确保状态更新完成
    await nextTick()
    
    // 路由跳转
    await router.push('/')
  } catch (error) {
    console.error('登录过程出错:', error)
    ElMessage.error(error.message || '登录失败，请稍后重试')
    
    // 登录失败时清除可能已保存的无效数据
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('name')
    localStorage.removeItem('role')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('@/assets/img/login-1203603_1280.webp');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(3px);
}

.login-box {
  position: relative;
  z-index: 1;
  animation: fadeIn 0.8s ease-out;
}

.login-card {
  width: 420px;
  background-color: rgba(255, 255, 255, 0.95);
  background-image: url('@/assets/img/login-card-bg.png');
  background-size: cover;
  background-position: center;
  background-blend-mode: overlay;
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
  transition: transform 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
}

.title-container {
  text-align: center;
  padding: 10px 0;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.login-title {
  font-size: 32px;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  font-family: "Microsoft YaHei", "微软雅黑", sans-serif;
  background: linear-gradient(45deg, #2c3e50, #3498db);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 2px;
  position: relative;
  display: inline-block;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.login-title::after {
  content: '';
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #3498db, transparent);
}

.login-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin: 5px 0 0;
  font-weight: 300;
  letter-spacing: 1px;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

.el-card__header {
  text-align: center;
  padding: 25px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.4);
  border-radius: 15px 15px 0 0;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) !important;
}

.login-button {
  width: 100%;
  height: 40px;
  font-size: 16px;
  border-radius: 8px;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-form-item__error) {
  padding-top: 4px;
}

:deep(.el-input__inner) {
  height: 42px;
}
</style> 