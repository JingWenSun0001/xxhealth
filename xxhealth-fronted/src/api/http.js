import axios from 'axios'

const http = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

http.interceptors.response.use(
  response => {
    const body = response.data
    if (body && typeof body.code === 'number') {
      if (body.code !== 200) {
        return Promise.reject(new Error(body.message || '请求失败'))
      }
      return body.data
    }
    return body
  },
  error => Promise.reject(new Error(error.response?.data?.message || error.message || '网络请求失败'))
)

export default http
