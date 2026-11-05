/**
 * 患者 / 预约示例数据
 *
 * TODO: 这些数据属于「体检预约 orders」「用户 users」模块（由队友负责），
 *      对应接口就绪后请替换为真实接口返回值：
 *        - orders/listOrders 或 orders/getOrdersById
 *        - users/getUsersById
 *      当前仅用于按 front(1) 设计稿还原页面结构，方便联调与演示。
 */
export const mockPatients = [
  {
    orderid: 1,
    name: '张三',
    sex: 1,
    age: 32,
    idcard: '410********1234',
    phone: '138****5678',
    setmeal: '中青年体检套餐',
    examDate: '2025-06-18',
    status: '待录入'
  },
  {
    orderid: 2,
    name: '李四',
    sex: 1,
    age: 45,
    idcard: '410********5678',
    phone: '139****6789',
    setmeal: '高端体检套餐',
    examDate: '2025-06-18',
    status: '待录入'
  },
  {
    orderid: 3,
    name: '王五',
    sex: 0,
    age: 28,
    idcard: '410********9012',
    phone: '137****9012',
    setmeal: '基础体检套餐',
    examDate: '2025-06-16',
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
 * 患者展示文案：张三 · 男 · 32岁
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
          date: current.examDate || '2025-06-16',
          setmeal: current.setmeal || '中青年体检套餐',
          status: '已生成',
          current: true
        }
      ]
    : []
  return base.concat([
    { orderid: null, date: '2024-06-18', setmeal: '中青年体检套餐', status: '已归档', current: false },
    { orderid: null, date: '2023-06-20', setmeal: '中青年体检套餐', status: '已归档', current: false },
    { orderid: null, date: '2022-06-15', setmeal: '基础体检套餐', status: '已归档', current: false }
  ])
}
