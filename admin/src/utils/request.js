import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

// ====== 请求缓存（用于不常变的数据） ======
const cacheStore = new Map()
const CACHE_TTL = 60000 // 1分钟

export function clearCache() { cacheStore.clear() }

function getCacheKey(config) {
  return `${config.method}:${config.url}:${JSON.stringify(config.params || '')}`
}

// ====== 请求去重（相同请求同时只发一次） ======
const pendingMap = new Map()

// ====== 拦截器 ======
request.interceptors.request.use(config => {
  const token = localStorage.getItem('xixin_admin_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== '0') {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('xixin_admin_token')
      window.location.hash = '#/login'
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status >= 500) {
      ElMessage.error('服务器繁忙，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
