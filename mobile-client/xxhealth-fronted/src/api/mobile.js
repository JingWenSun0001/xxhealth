import request from '@/utils/request'
import { getSessionStorage } from '@/common'

function userid() {
  const user = getSessionStorage('users')
  return user && user.userid
}

async function post(path, data = {}) {
  const id = userid()
  if (!id) throw new Error('请先登录')
  const response = await request.post('/mobile/workspace' + path, { userid: id, ...data })
  return response.data
}

export const mobileApi = {
  packages: () => post('/packages'),
  package: packageid => post('/packages/detail', { packageid }),
  book: data => post('/book', data),
  appointments: () => post('/appointments'),
  reports: () => post('/reports'),
  report: reportid => post('/reports/detail', { reportid }),
  summary: () => post('/summary'),
  messages: () => post('/messages')
}

