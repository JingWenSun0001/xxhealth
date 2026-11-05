import request from './request'

/**
 * 根据预约订单信息创建体检报告模板
 * @param {number} orderid 预约编号
 * @returns {Promise<number>} 创建的检查项数量（0 表示模板已存在或套餐无检查项）
 */
export function createReportTemplate(orderid) {
  return request.post('/ciReport/createReportTemplate', { orderid })
}

/**
 * 根据体检预约编号查询检查项列表及明细信息
 * @param {number} orderid 预约编号
 * @returns {Promise<Array>} 检查项数组，每项包含 cidetailedreports
 */
export function listCiReport(orderid) {
  return request.post('/ciReport/listCiReport', { orderid })
}
