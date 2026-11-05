import request from './request'

/**
 * 更新检查项信息及检查项明细信息
 * @param {Array} list 检查项明细数组（含 cidrid / value / iserror）
 * @returns {Promise<number>} 实际更新的记录数
 */
export function updateCiDetailedReport(list) {
  return request.post('/ciDetailedReport/updateCiDetailedReport', list)
}
