import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers['Authorization'] = `Bearer ${userStore.token}`
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200 || res.code === 0) {
      return res
    }
    ElMessage.error(res.message || '请求失败')
    if (res.code === 401) {
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  error => {
    console.error('Response error:', error)
    if (error.response) {
      if (error.response.status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        const userStore = useUserStore()
        userStore.logout()
        window.location.href = '/login'
      } else if (error.response.status === 403) {
        ElMessage.error('没有权限访问')
      } else if (error.response.status === 500) {
        ElMessage.error('服务器错误')
      }
    } else {
      ElMessage.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default service
