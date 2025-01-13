<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px">
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          :collapse="isCollapse"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          :unique-opened="true"
          :router="true"
        >
          <el-menu-item 
            v-for="item in menuItems" 
            :key="item.path" 
            :index="item.path"
            :data-path="item.path"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-left">
            <el-icon class="toggle-button" @click="toggleSidebar">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <div class="system-title">华星酒店预订系统</div>
          <div class="header-right">
            <el-dropdown>
              <span class="user-info">
                {{ username }}
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { hasPermission } from '@/utils/permission'

const isCollapse = ref(false)
const route = useRoute()
const router = useRouter()

// 获取用户名
const username = computed(() => {
  return localStorage.getItem('username') || '用户'
})

// 计算当前激活的菜单项
const activeMenu = computed(() => route.path)

// 获取路由配置中的所有菜单项（根据用户角色过滤）
const menuItems = computed(() => {
  const mainRoute = router.options.routes.find(route => route.path === '/')
  if (!mainRoute) return []
  
  return mainRoute.children.filter(route => {
    // 隐藏的菜单项不显示
    if (route.meta?.hidden) return false
    
    // 如果是管理员菜单项（需要特殊权限），只对管理员显示
    if (route.meta?.permission) {
      return hasPermission(route.meta.permission)
    }
    
    // 其他菜单项（如首页、预订吧）对所有人显示
    return true
  }).map(route => ({
    path: route.path,
    title: route.meta?.title || '',
    icon: route.meta?.icon || 'Menu'
  }))
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    router.push('/login')
  })
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-container {
  height: 100%;
}

.el-menu-vertical {
  height: 100%;
  border-right: none;
  display: flex;
  flex-direction: column;
}

.el-menu-vertical:not(.el-menu--collapse) {
  width: 200px;
}

.el-menu-item {
  display: flex !important;
  align-items: center !important;
  padding-left: 20px !important;
}

.el-menu-item .el-icon {
  margin-right: 8px;
  width: 24px;
  text-align: center;
}

.el-menu-item span {
  margin-left: 4px;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 确保菜单项在折叠状态下的样式正确 */
.el-menu--collapse .el-menu-item span {
  display: none;
}

.el-menu--collapse .el-menu-item .el-icon {
  margin: 0;
}

.el-header {
  background-color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  height: 60px;
  position: relative;
}

.header-left {
  display: flex;
  align-items: center;
  z-index: 1;
}

.toggle-button {
  font-size: 20px;
  cursor: pointer;
  transition: all .3s;
  transform: rotate(0deg);
}

.header-right {
  display: flex;
  align-items: center;
  z-index: 1;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info .el-icon {
  margin-left: 4px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  flex: 1;
  overflow-y: auto;
  height: calc(100vh - 60px);  /* 减去头部高度 */
}

/* 个人中心菜单项样式 */
.el-menu-item[data-path="/profile"] {
  margin-top: auto;  /* 将个人中心推到底部 */
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.system-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  letter-spacing: 2px;
  font-family: "Microsoft YaHei", "微软雅黑", sans-serif;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
  background: linear-gradient(45deg, #409EFF, #36cfc9);
  background-clip: text;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}
</style> 