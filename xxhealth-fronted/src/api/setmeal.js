import request from './request'

/**
 * 按类型查询套餐（type: 1 男士 / 0 女士 / null 全部）
 * 后端返回裸数组 List<Setmeal>
 */
export function listSetmealByType(type) {
  return request.post('/setmeal/listSetmealByType', { type: type === undefined ? null : type })
}

/**
 * 套餐详情（套餐 + 检查项 items）
 * 后端返回裸对象 SetmealDto
 */
export function getSetmealById(smid) {
  return request.post('/setmeal/getSetmealById', { smid })
}
