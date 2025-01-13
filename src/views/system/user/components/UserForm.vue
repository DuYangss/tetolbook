<template>
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
</template>

<script setup>
import { ref, watch } from 'vue'

defineOptions({
  name: 'UserFormDialog'
})

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  type: {
    type: String,
    default: 'create'
  },
  editData: {
    type: Object,
    default: () => ({})
  },
  roles: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const dialogVisible = ref(props.visible)
const formType = ref(props.type)

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

// 监听visible变化
watch(
  () => props.visible,
  (val) => {
    dialogVisible.value = val
  }
)

// 监听dialogVisible变化
watch(
  () => dialogVisible.value,
  (val) => {
    emit('update:visible', val)
    if (!val) {
      formRef.value?.resetFields()
    }
  }
)

// 监听type变化
watch(
  () => props.type,
  (val) => {
    formType.value = val
    if (val === 'edit' && props.editData) {
      formData.value = { ...props.editData }
    } else {
      formData.value = {
        username: '',
        password: '',
        name: '',
        role: '',
        phone: '',
        email: '',
        enabled: true
      }
    }
  },
  { immediate: true }
)

// 处理表单提交
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    emit('success', formData.value)
    dialogVisible.value = false
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 