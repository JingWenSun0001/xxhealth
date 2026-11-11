import axios from 'axios'

// 用户端仍使用 /tijian；开发代理会转发到共享后端的 /tijiancms。
const request = axios.create({
  baseURL: '/tijian',
  timeout: 10000
})

export default request
