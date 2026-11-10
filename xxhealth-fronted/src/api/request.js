import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  // dev 环境由 vue.config.js 代理到 http://localhost:8088/tijiancms
  baseURL: '/api',
  timeout: 15000
})

// 响应拦截：统一拆解后端 Result { code, message, data }
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res && typeof res === 'object' && Object.prototype.hasOwnProperty.call(res, 'code')) {
      if (res.code === 200) {
        return res.data
      }
      if (res.code === 401) {
        sessionStorage.removeItem('doctor')
        if (!window.location.hash.includes('/doctor/login')) {
          window.location.hash = '#/doctor/login'
        }
      }
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const msg =
      (error.response && error.response.data && error.response.data.message) ||
      '网络异常，请确认后端服务已启动（http://localhost:8088/tijiancms）'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
