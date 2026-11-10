import axios from 'axios'

// 统一走开发代理：/api -> http://localhost:8088/tijiancms
// 后端 context-path 为 /tijiancms（见 application.yml）
const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

export default request
