import axios from 'axios'

// 后端 context-path 是 /tijian，开发环境由 vue.config.js 代理到 8080
const request = axios.create({
  baseURL: '/tijian',
  timeout: 10000
})

export default request
