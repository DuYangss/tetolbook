<template>
  <div class="booking-management">
    <!-- 顶部操作栏 -->
    <div class="operation-bar">
      <div class="left">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增预订
        </el-button>
      </div>
      <div class="right">
        <el-select v-model="filterStatus" placeholder="预订状态" clearable>
          <el-option label="待确认" value="pending" />
          <el-option label="已确认" value="confirmed" />
          <el-option label="已入住" value="checked_in" />
          <el-option label="已取消" value="cancelled" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
        />
        <el-input
          v-model="keyword"
          placeholder="搜索客户姓名"
          clearable
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>搜索
        </el-button>
      </div>
    </div>

    <!-- 预订列表 -->
    <el-table
      :data="bookingList"
      v-loading="loading"
      border
      style="width: 100%"
      row-key="id"
      :height="tableHeight"
      highlight-current-row
    >
      <el-table-column prop="bookingNumber" label="预订号" width="150" sortable />
      <el-table-column prop="customerName" label="客户姓名" width="120" />
      <el-table-column prop="phone" label="联系电话" width="120" />
      <el-table-column prop="roomType" label="房间类型" width="120" />
      <el-table-column prop="checkInDate" label="入住日期" width="120" sortable />
      <el-table-column prop="checkOutDate" label="退房日期" width="120" sortable />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template #default="{ row }">
          ¥{{ row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button-group>
            <el-button type="primary" @click="handleEdit(row)" link>
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-if="row.status === 'pending'"
              type="success"
              @click="handleConfirm(row)"
              link
            >
              <el-icon><Check /></el-icon>
            </el-button>
            <el-button
              v-if="row.status === 'confirmed'"
              type="warning"
              @click="handleCheckIn(row)"
              link
            >
              <el-icon><Key /></el-icon>
            </el-button>
            <el-button
              v-if="['pending', 'confirmed'].includes(row.status)"
              type="danger"
              @click="handleCancel(row)"
              link
            >
              <el-icon><Close /></el-icon>
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

    <!-- 添加/编辑预订对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增预订' : '编辑预订'"
      width="600px"
    >
      <el-form
        ref="bookingFormRef"
        :model="bookingForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="客户姓名" prop="customerName">
          <el-input v-model="bookingForm.customerName" placeholder="请输入客户姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="bookingForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="bookingForm.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="房间类型" prop="roomType">
          <el-select 
            v-model="bookingForm.roomType" 
            placeholder="请选择房间类型"
            @change="fetchAvailableRooms"
          >
            <el-option label="标准间" value="标准间" />
            <el-option label="大床房" value="大床房" />
            <el-option label="套房" value="套房" />
          </el-select>
        </el-form-item>
        <el-form-item label="入住日期" prop="checkInDate">
          <el-date-picker
            v-model="bookingForm.checkInDate"
            type="date"
            placeholder="选择入住日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledCheckInDate"
          />
        </el-form-item>
        <el-form-item label="退房日期" prop="checkOutDate">
          <el-date-picker
            v-model="bookingForm.checkOutDate"
            type="date"
            placeholder="选择退房日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledCheckOutDate"
          />
        </el-form-item>
        <el-form-item label="住宿天数" v-if="bookingDays > 0">
          <span>{{ bookingDays }}晚</span>
        </el-form-item>
        <el-form-item label="选择房间" prop="roomId">
          <el-select 
            v-model="bookingForm.roomId" 
            placeholder="请选择房间"
            :disabled="!bookingForm.checkInDate || !bookingForm.checkOutDate"
            @change="handleRoomChange"
          >
            <el-option
              v-for="room in availableRooms"
              :key="room.id"
              :label="`${room.roomNumber} (￥${room.price}/晚)`"
              :value="room.id"
            />
          </el-select>
          <div class="form-tip" v-if="!bookingForm.checkInDate || !bookingForm.checkOutDate">
            请先选择入住和退房日期
          </div>
        </el-form-item>
        <el-form-item label="总金额" prop="totalAmount">
          <el-input-number
            v-model="bookingForm.totalAmount"
            :min="0"
            :precision="2"
            :step="100"
            :disabled="true"
          />
          <div class="form-tip" v-if="bookingDays > 0 && selectedRoom">
            计算方式：{{ bookingDays }}晚 × ￥{{ selectedRoom.price }}/晚
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input
            v-model="bookingForm.remarks"
            type="textarea"
            placeholder="请输入备注信息"
          />
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
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Search,
  Check,
  Close,
  Key
} from '@element-plus/icons-vue'
import {
  getBookings,
  createBooking,
  updateBooking,
  confirmBooking,
  cancelBooking,
  checkIn
} from '@/api/booking'
import { getAvailableRooms } from '@/api/room'
import { debounce } from 'lodash'

// 状态和数据
const loading = ref(false)
const bookingList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const filterStatus = ref('')
const dateRange = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const dialogType = ref('add')
const bookingFormRef = ref(null)
const tableHeight = ref('calc(100vh - 280px)')
const selectedRoom = ref(null)
const bookingDays = computed(() => {
  const { checkInDate, checkOutDate } = bookingForm.value
  if (!checkInDate || !checkOutDate) return 0
  const start = new Date(checkInDate)
  const end = new Date(checkOutDate)
  return Math.ceil((end - start) / (1000 * 60 * 60 * 24))
})

// 表单数据
const bookingForm = ref({
  customerName: '',
  phone: '',
  idCard: '',
  roomId: null,
  roomType: '',
  checkInDate: '',
  checkOutDate: '',
  totalAmount: 0,
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
  roomType: [
    { required: true, message: '请选择房间类型', trigger: 'change' }
  ],
  checkInDate: [
    { required: true, message: '请选择入住日期', trigger: 'change' }
  ],
  checkOutDate: [
    { required: true, message: '请选择退房日期', trigger: 'change' }
  ],
  totalAmount: [
    { required: true, message: '请输入总金额', trigger: 'blur' }
  ],
  roomId: [
    { required: true, message: '请选择房间', trigger: 'change' }
  ]
}

// 可用房间列表
const availableRooms = ref([])

// 获取可用房间
const fetchAvailableRooms = async () => {
  if (!bookingForm.value.roomType || !bookingForm.value.checkInDate || !bookingForm.value.checkOutDate) {
    availableRooms.value = [];
    return;
  }
  
  try {
    const params = {
      roomType: bookingForm.value.roomType,
      checkInDate: bookingForm.value.checkInDate,
      checkOutDate: bookingForm.value.checkOutDate
    };
    const response = await getAvailableRooms(params);
    if (response.code === 200) {
      availableRooms.value = response.data || [];
    } else {
      availableRooms.value = [];
      ElMessage.warning(response.message || '获取房间列表失败');
    }
  } catch (error) {
    availableRooms.value = [];
    console.error('获取可用房间失败:', error);
    ElMessage.error('获取可用房间失败');
  }
};

// 使用防抖包装fetchAvailableRooms
const debouncedFetchRooms = debounce(fetchAvailableRooms, 300);

// 监听房型、入住日期和退房日期的变化
watch([
  () => bookingForm.value.roomType,
  () => bookingForm.value.checkInDate,
  () => bookingForm.value.checkOutDate
], () => {
  bookingForm.value.roomId = null; // 清空已选择的房间
  debouncedFetchRooms();
});

// 日期禁用函数
const disabledCheckInDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

const disabledCheckOutDate = (time) => {
  const { checkInDate } = bookingForm.value
  if (!checkInDate) return false
  const start = new Date(checkInDate)
  return time.getTime() <= start.getTime()
}

// 处理房间选择变化
const handleRoomChange = (roomId) => {
  selectedRoom.value = availableRooms.value.find(room => room.id === roomId)
  calculateTotalAmount()
}

// 计算总金额
const calculateTotalAmount = () => {
  if (selectedRoom.value && bookingDays.value > 0) {
    bookingForm.value.totalAmount = selectedRoom.value.price * bookingDays.value
  } else {
    bookingForm.value.totalAmount = 0
  }
}

// 监听日期变化
watch(
  () => [bookingForm.value.checkInDate, bookingForm.value.checkOutDate],
  async ([newCheckIn, newCheckOut], [oldCheckIn, oldCheckOut]) => {
    if (newCheckIn && newCheckOut && (newCheckIn !== oldCheckIn || newCheckOut !== oldCheckOut)) {
      bookingForm.value.roomId = null
      selectedRoom.value = null
      bookingForm.value.totalAmount = 0
      await fetchAvailableRooms()
    }
  }
)

// 获取预订列表
const fetchBookingList = async () => {
  loading.value = true
  try {
    const params = {
      status: filterStatus.value,
      startDate: dateRange.value?.[0],
      endDate: dateRange.value?.[1],
      keyword: keyword.value
    }
    const res = await getBookings(params)
    if (res.data) {
      bookingList.value = Array.isArray(res.data) ? res.data : []
      total.value = bookingList.value.length
    } else {
      bookingList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取预订列表失败:', error)
    ElMessage.error('获取预订列表失败')
  } finally {
    loading.value = false
  }
}

// 状态标签类型
const getStatusType = (status) => {
  const map = {
    'pending': 'warning',
    'confirmed': 'success',
    'checked_in': 'info',
    'cancelled': 'danger'
  }
  return map[status] || 'info'
}

// 状态文本
const getStatusText = (status) => {
  const map = {
    'pending': '待确认',
    'confirmed': '已确认',
    'checked_in': '已入住',
    'cancelled': '已取消'
  }
  return map[status] || status
}

// 处理添加
const handleAdd = () => {
  dialogType.value = 'add'
  bookingForm.value = {
    customerName: '',
    phone: '',
    idCard: '',
    roomId: null,
    roomType: '',
    checkInDate: '',
    checkOutDate: '',
    totalAmount: 0,
    remarks: ''
  }
  dialogVisible.value = true
}

// 处理编辑
const handleEdit = (row) => {
  dialogType.value = 'edit'
  bookingForm.value = { ...row }
  dialogVisible.value = true
}

// 处理确认
const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确认后将锁定该房间，确定要确认该预订吗？',
      '确认预订',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await confirmBooking(row.id)
    ElMessage.success('预订确认成功')
    fetchBookingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认预订失败:', error)
      ElMessage.error('确认预订失败')
    }
  }
}

// 处理取消
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      '取消后将释放该房间，确定要取消该预订吗？',
      '取消预订',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await cancelBooking(row.id)
    ElMessage.success('预订取消成功')
    fetchBookingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消预订失败:', error)
      ElMessage.error('取消预订失败')
    }
  }
}

// 处理入住
const handleCheckIn = async (row) => {
  try {
    await ElMessageBox.confirm(
      '入住后将更新房间状态为已入住，确定要办理入住吗？',
      '办理入住',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await checkIn(row.id)
    ElMessage.success('入住办理成功')
    fetchBookingList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('入住办理失败:', error)
      ElMessage.error('入住办理失败')
    }
  }
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchBookingList()
}

// 处理提交
const handleSubmit = async () => {
  if (!bookingFormRef.value) return
  
  await bookingFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const bookingData = { ...bookingForm.value }
        // 从可用房间列表中找到选中的房间对象
        const selectedRoomObj = availableRooms.value.find(room => room.id === bookingForm.value.roomId)
        if (!selectedRoomObj) {
          throw new Error('未找到选中的房间')
        }
        // 将 roomId 替换为完整的 room 对象
        bookingData.room = selectedRoomObj
        delete bookingData.roomId

        if (dialogType.value === 'add') {
          await createBooking(bookingData)
          ElMessage.success('添加成功')
        } else {
          await updateBooking(bookingData.id, bookingData)
          ElMessage.success('更新成功')
        }
        dialogVisible.value = false
        fetchBookingList()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error('操作失败')
      }
    }
  })
}

// 分页相关
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBookingList()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBookingList()
}

// 计算表格高度
const updateTableHeight = () => {
  const windowHeight = window.innerHeight
  const offset = 280 // 顶部操作栏 + 底部分页 + 边距的总高度
  tableHeight.value = `${windowHeight - offset}px`
}

// 监听窗口大小变化
onMounted(() => {
  updateTableHeight()
  window.addEventListener('resize', updateTableHeight)
  fetchBookingList()
})

// 清理事件监听器
onUnmounted(() => {
  window.removeEventListener('resize', updateTableHeight)
})
</script>

<style scoped>
.booking-management {
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

/* 日期选择器样式 */
:deep(.el-date-editor) {
  width: 240px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

:deep(.el-input-number.is-disabled .el-input-number__decrease),
:deep(.el-input-number.is-disabled .el-input-number__increase) {
  display: none;
}
</style> 