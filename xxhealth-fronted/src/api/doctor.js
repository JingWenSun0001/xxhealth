import request from './request'

/**
 * 医生登录：根据医生编码和密码进行登录
 * @param {{doccode: string, password: string}} data
 */
export function getDoctorByCodeByPass(data) {
  return request.post('/doctor/getDoctorByCodeByPass', data)
}
