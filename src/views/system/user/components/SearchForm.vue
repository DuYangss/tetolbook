<template>
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
</template>

<script setup>
import { ref } from 'vue'

defineOptions({
  name: 'UserSearchForm'
})

const emit = defineEmits(['search'])

const searchForm = ref({
  role: '',
  keyword: ''
})

const handleSearch = () => {
  emit('search', { ...searchForm.value })
}

const handleReset = () => {
  searchForm.value = {
    role: '',
    keyword: ''
  }
  handleSearch()
}

defineProps({
  roles: {
    type: Array,
    default: () => []
  }
})
</script> 