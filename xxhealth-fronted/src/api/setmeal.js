import http from './http'
import request from './request'

/**
 * 查询全部套餐（订单模块使用，Result 已由 http 拆包）
 */
export function listSetmeal() {
  return http.post('/setmeal/listSetmeal')
}

/**
 * 按类型查询套餐（医生端体检套餐页面）
 */
export function listSetmealByType(type) {
  return request.post('/setmeal/listSetmealByType', { type: type === undefined ? null : type })
}

/**
 * 套餐详情（套餐 + 检查项）
 */
export function getSetmealById(smid) {
  return request.post('/setmeal/getSetmealById', { smid })
}
