<template>
  <div class="user-table">
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
            :disabled="!showActions || row.role === 'ADMIN'"
            @change="(val) => handleStatusChange(row.id, val)"
          />
        </template>
      </el-table-column>
      <el-table-column v-if="showActions" label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button
            type="primary"
            link
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            type="danger"
            link
            :disabled="row.role === 'ADMIN'"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { updateUserStatus, deleteUser } from '@/api/user'

const props = defineProps({
  userList: {
    type: Array,
    required: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update', 'edit', 'page-change'])

const currentPage = ref(1)
const pageSize = ref(10)

// 获取角色显示名称
const getRoleDisplayName = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'STAFF': '员工'
  }
  return roleMap[role] || role
}

// 处理状态变更
const handleStatusChange = async (id, enabled) => {
  try {
    await updateUserStatus(id, enabled)
    ElMessage.success('状态更新成功')
    emit('update')
  } catch (error) {
    ElMessage.error('状态更新失败')
  }
}

// 处理编辑
const handleEdit = (row) => {
  emit('edit', row)
}

// 处理删除
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
      emit('update')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 处理每页条数变化
const handleSizeChange = (val) => {
  pageSize.value = val
  emit('page-change', { page: currentPage.value, size: val })
}

// 处理页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  emit('page-change', { page: val, size: pageSize.value })
}
</script>

<style scoped>
.user-table {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 