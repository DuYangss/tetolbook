<template>
  <div class="room-management">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <div class="left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>添加房间
        </el-button>
      </div>
      <div class="right">
        <el-select v-model="filterType" placeholder="房间类型" clearable>
          <el-option label="标准间" value="标准间" />
          <el-option label="大床房" value="大床房" />
          <el-option label="套房" value="套房" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="房间状态" clearable>
          <el-option label="空闲" value="空闲" />
          <el-option label="已预订" value="已预订" />
          <el-option label="已入住" value="已入住" />
          <el-option label="维护中" value="维护中" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
    </div>

    <!-- 房间列表 -->
    <el-table 
      :data="roomList" 
      v-loading="loading" 
      border 
      style="width: 100%"
      row-key="id"
      :height="tableHeight"
      highlight-current-row
      :default-sort="{ prop: 'roomNumber', order: 'ascending' }"
    >
      <el-table-column prop="roomNumber" label="房间号" width="120" sortable />
      <el-table-column prop="type" label="房间类型" width="120" sortable />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="120">
        <template #default="{ row }">
          ¥{{ row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <el-image
            :src="getImageUrl(row.imageUrl)"
            :preview-src-list="[getImageUrl(row.imageUrl)]"
            fit="cover"
            style="width: 80px; height: 80px"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" @click="handleEdit(row)" link>
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button type="danger" @click="handleDelete(row)" link>
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model="currentPage"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 添加/编辑房间对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加房间' : '编辑房间'"
      width="500px"
    >
      <el-form
        ref="roomFormRef"
        :model="roomForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="房间号" prop="roomNumber">
          <el-input v-model="roomForm.roomNumber" placeholder="请输入房间号" />
        </el-form-item>
        <el-form-item label="房间类型" prop="type">
          <el-select v-model="roomForm.type" placeholder="请选择房间类型">
            <el-option label="标准间" value="标准间" />
            <el-option label="大床房" value="大床房" />
            <el-option label="套房" value="套房" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="roomForm.status" placeholder="请选择房间状态">
            <el-option label="空闲" value="空闲" />
            <el-option label="已预订" value="已预订" />
            <el-option label="已入住" value="已入住" />
            <el-option label="维护中" value="维护中" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number 
            v-model="roomForm.price"
            :min="0"
            :precision="2"
            :step="10"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roomForm.description"
            type="textarea"
            placeholder="请输入房间描述"
          />
        </el-form-item>
        <el-form-item label="房间图片">
          <el-upload
            class="room-image-upload"
            action="http://localhost:8080/api/upload/image"
            :headers="getUploadHeaders()"
            name="file"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
          >
            <img v-if="roomForm.imageUrl" :src="getImageUrl(roomForm.imageUrl)" class="upload-image">
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
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
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, Search, Picture } from '@element-plus/icons-vue'
import {
  getAllRooms,
  createRoom,
  updateRoom,
  deleteRoom,
  checkRoomBookings
} from '@/api/room'

// 状态和数据
const loading = ref(false)
const roomList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterType = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const dialogType = ref('add')
const roomFormRef = ref(null)
const tableHeight = ref('calc(100vh - 280px)')
const roomForm = ref({
  roomNumber: '',
  type: '',
  status: '空闲',
  price: 0,
  description: '',
  imageUrl: ''
})

// 表单验证规则
const rules = {
  roomNumber: [
    { required: true, message: '请输入房间号', trigger: 'blur' },
    { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择房间状态', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入房间价格', trigger: 'blur' }
  ]
}

// 获取房间列表
const fetchRoomList = async () => {
  loading.value = true
  try {
    const res = await getAllRooms({
      type: filterType.value,
      status: filterStatus.value
    })
    console.log('房间列表响应:', res)
    if (res.data) {
      roomList.value = Array.isArray(res.data) ? res.data : []
      total.value = roomList.value.length
    } else {
      roomList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取房间列表失败:', error)
    ElMessage.error('获取房间列表失败')
  } finally {
    loading.value = false
  }
}

// 状态标签类型
const getStatusType = (status) => {
  const map = {
    '空闲': 'success',
    '已预订': 'warning',
    '已入住': 'info',
    '维护中': 'danger'
  }
  return map[status] || 'info'
}

// 处理添加
const handleAdd = () => {
  dialogType.value = 'add'
  roomForm.value = {
    roomNumber: '',
    type: '',
    status: '空闲',
    price: 0,
    description: '',
    imageUrl: ''
  }
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  roomForm.value = { ...row }
  dialogVisible.value = true
}

// 处理删除
const handleDelete = async (row) => {
  try {
    // 先检查房间是否有关联的预订记录
    const checkResult = await checkRoomBookings(row.id)
    if (checkResult.data?.hasBookings) {
      ElMessageBox.confirm(
        '该房间存在关联的预订记录，删除可能会影响到历史数据。是否确认删除？',
        '警告',
        {
          confirmButtonText: '确认删除',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(async () => {
        await deleteRoom(row.id)
        ElMessage.success('删除成功')
        fetchRoomList()
      }).catch(() => {
        ElMessage.info('已取消删除')
      })
    } else {
      // 如果没有预订记录，直接确认删除
      await ElMessageBox.confirm(
        '确认删除该房间吗？',
        '提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      await deleteRoom(row.id)
      ElMessage.success('删除成功')
      fetchRoomList()
    }
  } catch (error) {
    console.error('删除房间失败:', error)
    ElMessage.error(error.message || '删除失败，请稍后重试')
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchRoomList()
}

// 处理重置
const handleReset = () => {
  filterType.value = ''
  filterStatus.value = ''
  currentPage.value = 1
  fetchRoomList()
}

// 处理提交
const handleSubmit = async () => {
  if (!roomFormRef.value) return
  
  await roomFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (dialogType.value === 'add') {
          await createRoom(roomForm.value)
          ElMessage.success('添加成功')
        } else {
          await updateRoom(roomForm.value.id, roomForm.value)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchRoomList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 处理上传成功
const handleUploadSuccess = (response) => {
  console.log('上传响应:', response)
  if (response.code === 200) {
    const imageUrl = response.data
    console.log('图片URL:', imageUrl)
    console.log('完整图片URL:', getImageUrl(imageUrl))
    roomForm.value.imageUrl = imageUrl
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

// 处理上传失败
const handleUploadError = (error) => {
  console.error('上传失败:', error)
  ElMessage.error('图片上传失败，请重试')
}

// 上传前校验
const beforeUpload = (file) => {
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

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRoomList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRoomList()
}

// 计算表格高度
const updateTableHeight = () => {
  const windowHeight = window.innerHeight
  const offset = 280 // 顶部操作栏 + 底部分页 + 边距的总高度
  tableHeight.value = `${windowHeight - offset}px`
}

// 错误处理函数
const handleError = (e) => {
  if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
    const resizeObserverErrDiv = document.getElementById('webpack-dev-server-client-overlay-div')
    const resizeObserverErr = document.getElementById('webpack-dev-server-client-overlay')
    if (resizeObserverErr) {
      resizeObserverErr.style.display = 'none'
    }
    if (resizeObserverErrDiv) {
      resizeObserverErrDiv.style.display = 'none'
    }
  }
}

// 获取完整的图片 URL
const getImageUrl = (url) => {
  if (!url) {
    console.log('图片URL为空')
    return ''
  }
  if (url.startsWith('http')) {
    console.log('已经是完整URL:', url)
    return url
  }
  const fullUrl = `http://localhost:8080${url}`
  console.log('转换后的完整URL:', fullUrl)
  return fullUrl
}

// 在 script setup 部分添加
const getUploadHeaders = () => {
  const token = localStorage.getItem('token')
  return token ? {
    Authorization: `Bearer ${token}`
  } : {}
}

// 监听窗口大小变化
onMounted(async () => {
  // 处理 ResizeObserver 错误
  window.addEventListener('error', handleError)

  // 初始化表格高度
  updateTableHeight()
  window.addEventListener('resize', updateTableHeight)

  await nextTick()
  fetchRoomList()
})

// 清理事件监听器
onUnmounted(() => {
  window.removeEventListener('error', handleError)
  window.removeEventListener('resize', updateTableHeight)
})
</script>

<style scoped>
.room-management {
  padding: 24px;
  height: 100%;
  box-sizing: border-box;
  background-color: #f5f7fa;
}

.operation-bar {
  margin-bottom: 24px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.operation-bar:hover {
  box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.1);
}

.right {
  display: flex;
  gap: 12px;
}

/* 表格样式 */
:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

:deep(.el-table th) {
  background-color: #f5f7fa !important;
  font-weight: 600;
  color: #333;
}

:deep(.el-table--border .el-table__cell) {
  border-right: 1px solid #ebeef5;
}

:deep(.el-table__row) {
  transition: all 0.3s ease;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa !important;
  transform: translateY(-2px);
}

/* 图片样式 */
.room-image-upload {
  border: 2px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.room-image-upload:hover {
  border-color: var(--el-color-primary);
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

.upload-image {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.upload-image:hover {
  transform: scale(1.05);
}

.upload-icon {
  font-size: 32px;
  color: #909399;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
  transition: all 0.3s ease;
}

.upload-icon:hover {
  color: var(--el-color-primary);
  transform: scale(1.1);
}

/* 分页样式 */
.pagination {
  margin-top: 24px;
  padding: 16px;
  display: flex;
  justify-content: flex-end;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

/* 对话框样式 */
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  margin: 0;
  padding: 20px 24px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-dialog__body) {
  padding: 24px;
}

:deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid #ebeef5;
  background-color: #f5f7fa;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-select__wrapper) {
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover),
:deep(.el-select__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
}

/* 标签样式 */
:deep(.el-tag) {
  border-radius: 4px;
  padding: 4px 8px;
  font-weight: 500;
}

/* 图片预览样式 */
.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 24px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.image-error:hover {
  background: #ebeef5;
}

/* 按钮组样式 */
:deep(.el-button-group) {
  display: flex;
  gap: 8px;
}

:deep(.el-button.is-link) {
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

:deep(.el-button.is-link:hover) {
  background-color: #f5f7fa;
  transform: translateY(-2px);
}

/* 表格中的图片单元格样式 */
:deep(.el-image) {
  border-radius: 4px;
  overflow: hidden;
  transition: all 0.3s ease;
}

:deep(.el-image:hover) {
  transform: scale(1.05);
}
</style> 