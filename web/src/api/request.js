import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearToken, clearUser } from '../utils/auth'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截：附加 token
request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers['satoken'] = token
  }
  return config
})

// 响应拦截：统一处理 code 与 401
request.interceptors.response.use(
  (response) => {
    // 文件下载（blob）直接返回原始响应
    if (response.config.responseType === 'blob') {
      return response
    }
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const res = error.response
    if (res && (res.status === 401 || res.data?.code === 401)) {
      clearToken()
      clearUser()
      ElMessage.error('登录已失效，请重新登录')
      router.push('/login')
      return Promise.reject(error)
    }
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default request
