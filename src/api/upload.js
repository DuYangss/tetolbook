import request from '@/utils/request'

const baseURL = 'http://localhost:8080/api/upload'

// 上传图片
export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: `${baseURL}/image`,
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 