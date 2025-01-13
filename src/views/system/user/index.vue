<template>
  <div class="user-manage">
    <div class="header">
      <h2>用户管理</h2>
      <el-button 
        type="primary" 
        @click="handleCreate"
        v-if="hasPermission('user:manage')"
      >创建用户</el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" @submit.prevent="handleSearch">
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择角色" clearable>
            <el-option
              v-for="role in roles"
              :key="role.name"
              :label="role.displayName"
              :value="role.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="searchForm.keyword"
            placeholder="用户名/姓名/手机号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="list-card">
      <el-table
        v-loading="loading"
        :data="userList"
        border
        style="width: 100%"
      >
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="role" label="角色">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'">
              {{ getRoleDisplayName(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              :disabled="!hasPermission('user:manage') || row.role === 'ADMIN'"
              @change="(val) => handleStatusChange(row.id, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              v-if="hasPermission('user:manage')"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              :disabled="row.role === 'ADMIN'"
              @click="handleDelete(row)"
              v-if="hasPermission('user:manage')"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 用户表单弹窗 -->
    <el-dialog
      :title="formType === 'create' ? '创建用户' : '编辑用户'"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="formData.username"
            :disabled="formType === 'edit'"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item
          label="密码"
          prop="password"
          :rules="formType === 'create' ? rules.password : []"
        >
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入姓名"
          />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select
            v-model="formData.role"
            placeholder="请选择角色"
          >
            <el-option
              v-for="role in roles"
              :key="role.name"
              :label="role.displayName"
              :value="role.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="formData.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="formData.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { searchUsers, createUser, updateUser, deleteUser, updateUserStatus, getAllRoles } from '@/api/user'
import { hasPermission } from '@/utils/permission'

// 组件名称
defineOptions({
  name: 'UserManagementView'
})

// 状态
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const formType = ref('create')
const formRef = ref(null)
const roles = ref([])

// 搜索表单
const searchForm = ref({
  role: '',
  keyword: ''
})

// 表单数据
const formData = ref({
  username: '',
  password: '',
  name: '',
  role: '',
  phone: '',
  email: '',
  enabled: true
})

// 表单验证规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 获取角色显示名称
const getRoleDisplayName = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'MANAGER': '经理',
    'STAFF': '员工'
  }
  return roleMap[role] || role
}

// 加载用户列表
const loadUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      role: searchForm.value.role || '',
      keyword: searchForm.value.keyword || ''
    }
    const response = await searchUsers(params)
    const { data } = response
    userList.value = data.content
    total.value = data.totalElements
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 加载角色列表
const loadRoles = async () => {
  try {
    const response = await getAllRoles()
    const roleList = response.data
    roles.value = roleList.map(role => ({
      name: role,
      displayName: getRoleDisplayName(role)
    }))
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  loadUserList()
}

// 处理重置
const handleReset = () => {
  searchForm.value = {
    role: '',
    keyword: ''
  }
  handleSearch()
}

// 处理分页
const handleSizeChange = (val) => {
  pageSize.value = val
  loadUserList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  loadUserList()
}

// 处理创建用户
const handleCreate = () => {
  formType.value = 'create'
  formData.value = {
    username: '',
    password: '',
    name: '',
    role: '',
    phone: '',
    email: '',
    enabled: true
  }
  dialogVisible.value = true
}

// 处理编辑用户
const handleEdit = (row) => {
  formType.value = 'edit'
  formData.value = { ...row }
  formData.value.password = '' // 清空密码
  dialogVisible.value = true
}

// 处理删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该用户吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteUser(row.id)
      ElMessage.success('删除成功')
      loadUserList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 处理状态变更
const handleStatusChange = async (id, enabled) => {
  try {
    await updateUserStatus(id, enabled)
    ElMessage.success('状态更新成功')
  } catch (error) {
    ElMessage.error('状态更新失败')
    loadUserList() // 刷新列表，恢复原状态
  }
}

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    if (formType.value === 'create') {
      await createUser(formData.value)
      ElMessage.success('创建成功')
    } else {
      await updateUser(formData.value.id, formData.value)
      ElMessage.success('更新成功')
    }
    
    dialogVisible.value = false
    loadUserList()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error('提交失败')
  }
}

// 初始化
onMounted(() => {
  loadRoles()
  loadUserList()
})
</script>

<style scoped>
.user-manage {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 