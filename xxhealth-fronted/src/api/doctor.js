import request from './request'

/**
 * 医生登录：根据医生编码和密码进行登录
 * @param {{doccode: string, password: string}} data
 */
export function getDoctorByCodeByPass(data) {
  return request.post('/doctor/getDoctorByCodeByPass', data)
}

/**
 * 修改医生资料（姓名 / 性别 / 科室）
 */
export function updateDoctor(data) {
  return request.post('/doctor/updateDoctor', data)
}

/**
 * 修改密码（校验原密码）
 */
export function updatePassword(data) {
  return request.post('/doctor/updatePassword', data)
}

/**
 * 医生注册
 */
export function registerDoctor(data) {
  return request.post('/doctor/register', data)
}
