import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    ElMessage.error('请求失败：' + error.message)
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    const data = response.data
    if (data && typeof data === 'object' && data.code !== undefined) {
      if (data.code !== 200 && data.code !== 0) {
        ElMessage.warning(data.msg || data.message || '请求失败')
      }
    }
    return data
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      ElMessage.warning('登录已过期，请重新登录')
      setTimeout(() => {
        window.location.href = '/login'
      }, 1000)
    } else if (error.response?.status === 403) {
      ElMessage.error('无权限访问')
    } else if (error.response?.status === 404) {
      ElMessage.error('接口不存在：' + error.config?.url)
    } else if (error.response?.status === 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else if (!error.response) {
      ElMessage.error('网络错误，请检查后端服务是否启动')
    } else {
      ElMessage.error('请求失败：' + (error.response?.data?.message || error.message))
    }
    return Promise.reject(error)
  }
)

export default api
