/**
 * 患者 / 预约示例数据
 *
 * TODO: 这些数据属于「体检预约 orders」「用户 users」模块（由队友负责），
 *      对应接口就绪后请替换为真实接口返回值：
 *        - orders/listOrders 或 orders/getOrdersById
 *        - users/getUsersById
 *      当前数据与 sql/seed.sql 的演示数据保持一致，方便联调与演示。
 */
export const mockPatients = [
  {
    orderid: 1,
    name: '张伟',
    sex: 1,
    age: 36,
    idcard: '210********1234',
    phone: '138****0001',
    setmeal: '中青年体检套餐',
    examDate: '2026-11-06',
    status: '待录入'
  },
  {
    orderid: 2,
    name: '张伟',
    sex: 1,
    age: 36,
    idcard: '210********1234',
    phone: '138****0001',
    setmeal: '中青年体检套餐',
    examDate: '2026-10-20',
    status: '已归档'
  },
  {
    orderid: 3,
    name: '李娜',
    sex: 0,
    age: 31,
    idcard: '210********2345',
    phone: '138****0002',
    setmeal: '女性关爱套餐',
    examDate: '2026-10-15',
    status: '已归档'
  }
]

/**
 * 根据预约编号查找示例患者信息
 */
export function findMockPatient(orderid) {
  return mockPatients.find((p) => Number(p.orderid) === Number(orderid)) || null
}

/**
 * 患者展示文案：张伟 · 男 · 36岁
 */
export function patientText(patient) {
  if (!patient) return ''
  const sex = Number(patient.sex) === 1 ? '男' : Number(patient.sex) === 0 ? '女' : ''
  return [patient.name, sex, patient.age ? `${patient.age}岁` : ''].filter(Boolean).join(' · ')
}

/**
 * 历史报告示例数据（报告详情页左侧列表用）
 * TODO: 待 orders 接口接入后替换
 */
export function mockReportHistory(current) {
  const base = current
    ? [
        {
          orderid: current.orderid,
          date: current.examDate || '2026-11-06',
          setmeal: current.setmeal || '中青年体检套餐',
          status: '已生成',
          current: true
        }
      ]
    : []
  return base.concat([
    { orderid: null, date: '2025-06-18', setmeal: '中青年体检套餐', status: '已归档', current: false },
    { orderid: null, date: '2024-06-20', setmeal: '中青年体检套餐', status: '已归档', current: false },
    { orderid: null, date: '2023-06-15', setmeal: '基础体检套餐', status: '已归档', current: false }
  ])
}
