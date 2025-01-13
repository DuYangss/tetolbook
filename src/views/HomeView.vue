<template>
  <el-container class="home-container">
    <!-- 侧边栏 -->
    <el-aside width="200px">
      <div class="logo">
        <el-icon :size="32"><House /></el-icon>
        <span>酒店管理系统</span>
      </div>
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        :router="true"
        :collapse="isCollapse"
      >
        <el-menu-item index="/">
          <el-icon><House /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <el-menu-item 
          index="/rooms"
          v-if="hasPermission('room:manage')"
        >
          <el-icon><OfficeBuilding /></el-icon>
          <template #title>房间管理</template>
        </el-menu-item>

        <el-menu-item 
          index="/bookings"
          v-if="hasPermission('booking:manage')"
        >
          <el-icon><Calendar /></el-icon>
          <template #title>预订管理</template>
        </el-menu-item>
       
        <el-menu-item 
          index="/users"
          v-if="hasPermission('user:manage')"
        >
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>

      </el-menu>
    </el-aside>

    <!-- 主要内容区 -->
    <el-container>
      <!-- 头部 -->
      <el-header>
        <div class="header-left">
          <el-icon 
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar 
                :size="32" 
                :icon="User"
              />
              <span class="username">{{ userInfo.name }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <router-link to="/profile">
                  <el-dropdown-item>
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                </router-link>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { House, OfficeBuilding, Calendar, User, Fold, Expand, SwitchButton } from '@element-plus/icons-vue'
import { hasPermission } from '@/utils/permission'

const router = useRouter()
const isCollapse = ref(false)
const userInfo = ref({
  name: localStorage.getItem('name') || '未知用户'
})

// 切换菜单折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    try {
      await ElMessageBox.confirm(
        '确定要退出登录吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      // 清除用户信息
      localStorage.clear()
      
      // 跳转到登录页
      router.push('/login')
    } catch (error) {
      // 用户取消操作
    }
  }
}

// 处理退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 清除用户信息
    localStorage.clear()
    
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}

onMounted(() => {
  // 从localStorage获取用户信息
  const name = localStorage.getItem('name')
  if (name) {
    userInfo.value.name = name
  }
})
</script>

<style scoped>
.home-container {
  height: 100vh;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-menu-bg-color);
  padding: 10px;
  box-sizing: border-box;
  border-bottom: 1px solid var(--el-border-color-light);
}

.logo .el-icon {
  margin-right: 10px;
  color: var(--el-text-color-primary);
}

.logo span {
  font-size: 16px;
  font-weight: bold;
  color: var(--el-text-color-primary);
  white-space: nowrap;
}

.el-aside {
  background-color: var(--el-menu-bg-color);
  border-right: 1px solid var(--el-border-color-light);
}

.el-menu {
  border-right: none;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: var(--el-text-color-regular);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 8px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.el-main {
  background-color: var(--el-bg-color-page);
  padding: 20px;
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 