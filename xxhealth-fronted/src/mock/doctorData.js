/**
 * 医生端示例数据
 *
 * TODO:
 *  - 套餐的「适用人群 / 标签 / 特色」在 setmeal 表里没有对应字段，先按名称匹配展示文案，
 *    等套餐表扩展字段后改为接口返回。
 *  - 日程（预约）、消息通知属于 orders / 消息模块，接口就绪后替换为真实数据。
 */

function pad(n) {
  return n < 10 ? '0' + n : String(n)
}

export function dateStr(offset = 0) {
  const d = new Date()
  d.setDate(d.getDate() + offset)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const packageMetaRules = [
  {
    keyword: '基础',
    crowd: '适合 18-40 岁人群',
    tags: ['热门', '推荐'],
    features: ['常规检查 12 项', '血常规、尿常规', '肝功能、心电图']
  },
  {
    keyword: '中青年',
    crowd: '适合 40-60 岁人群',
    tags: ['畅销', '全面'],
    features: ['常规检查 18 项', '肿瘤筛查、心脑血管', '彩超、骨密度']
  },
  {
    keyword: '女性',
    crowd: '适合女性全生命周期',
    tags: ['呵护', '全面'],
    features: ['妇科检查、乳腺彩超', 'HPV 筛查、激素检测', '甲状腺功能']
  },
  {
    keyword: '男性',
    crowd: '适合男性各年龄段',
    tags: ['精准', '高效'],
    features: ['前列腺检查、肝功能', '心脑血管、肿瘤筛查', '代谢评估']
  },
  {
    keyword: '高端',
    crowd: '高端人群专属定制',
    tags: ['尊享', '全面'],
    features: ['全面深度检查 40 项', '高端影像、基因检测', '专家解读报告']
  },
  {
    keyword: '老年',
    crowd: '适合 60 岁以上人群',
    tags: ['专业', '深度'],
    features: ['全面检查 24 项', '心脑血管专项', '肿瘤标志物筛查']
  }
]

export function packageMeta(pkg) {
  const name = (pkg && pkg.name) || ''
  const hit = packageMetaRules.find((rule) => name.indexOf(rule.keyword) >= 0)
  if (hit) return hit
  return {
    crowd: '适合全年龄段人群',
    tags: ['常规'],
    features: ['常规检查项目', '血常规、尿常规', '心电图']
  }
}

// ---------- 日程（预约）示例 ----------
export const mockAppointments = [
  { id: 1, date: dateStr(0), time: '08:00-08:30', name: '李*萱', sex: 0, age: 32, setmeal: '中青年体检套餐', status: '已完成' },
  { id: 2, date: dateStr(0), time: '08:30-09:00', name: '王*强', sex: 1, age: 45, setmeal: '高端体检套餐', status: '进行中' },
  { id: 3, date: dateStr(0), time: '09:30-10:00', name: '张*敏', sex: 0, age: 28, setmeal: '女性关爱套餐', status: '待检' },
  { id: 4, date: dateStr(0), time: '10:30-11:00', name: '赵*伟', sex: 1, age: 62, setmeal: '高端体检套餐', status: '已预约' },
  { id: 5, date: dateStr(0), time: '14:00-14:30', name: '刘*婷', sex: 0, age: 37, setmeal: '中青年体检套餐', status: '已预约' },
  { id: 6, date: dateStr(-1), time: '08:00-08:30', name: '陈*宇', sex: 1, age: 50, setmeal: '基础体检套餐', status: '已完成' },
  { id: 7, date: dateStr(-1), time: '09:00-09:30', name: '孙*杰', sex: 1, age: 29, setmeal: '基础体检套餐', status: '已完成' },
  { id: 8, date: dateStr(1), time: '08:30-09:00', name: '周*涵', sex: 0, age: 41, setmeal: '女性关爱套餐', status: '已预约' },
  { id: 9, date: dateStr(1), time: '10:00-10:30', name: '吴*磊', sex: 1, age: 35, setmeal: '中青年体检套餐', status: '已预约' },
  { id: 10, date: dateStr(1), time: '13:30-14:00', name: '郑*爽', sex: 0, age: 26, setmeal: '基础体检套餐', status: '已预约' },
  { id: 11, date: dateStr(3), time: '09:00-09:30', name: '冯*博', sex: 1, age: 58, setmeal: '高端体检套餐', status: '已预约' }
]

// ---------- 检查提醒 / 异常提醒 示例 ----------
export const mockCheckReminders = [
  { id: 1, time: '09:00', patient: '王*强', content: '空腹项目：血常规、肝功能，请确认已空腹 8 小时' },
  { id: 2, time: '10:30', patient: '赵*伟', content: '影像检查：胸部 X 光，请提醒去除金属物品' },
  { id: 3, time: '14:00', patient: '刘*婷', content: '女性专项：妇科彩超，请提醒提前憋尿' }
]

export const mockAbnormalReminders = [
  { id: 1, patient: '李*萱', item: '总胆固醇', value: '5.8 mmol/L', level: '偏高', time: '10:05' },
  { id: 2, patient: '张*敏', item: '血红蛋白', value: '105 g/L', level: '偏低', time: '11:12' },
  { id: 3, patient: '赵*伟', item: '空腹血糖', value: '7.2 mmol/L', level: '偏高', time: '11:40' }
]

// ---------- 消息通知示例 ----------
export const mockMessages = [
  {
    id: 1,
    type: '预约提醒',
    title: '预约提醒',
    desc: '您有一位新预约：李*萱，体检时间为 2026-11-06 09:00，请提前做好准备。',
    time: '2026-11-06 14:23',
    read: false
  },
  {
    id: 2,
    type: '报告通知',
    title: '报告通知',
    desc: '王*强的体检报告已生成，请登录系统查看。',
    time: '2026-11-06 13:50',
    read: false
  },
  {
    id: 3,
    type: '异常提醒',
    title: '异常提醒',
    desc: '张*敏的体检结果中发现血压偏高，请关注并进一步检查。',
    time: '2026-11-06 11:20',
    read: false
  },
  {
    id: 4,
    type: '系统通知',
    title: '系统通知',
    desc: '系统将于 11 月 20 日凌晨 2:00-4:00 进行例行维护，期间部分功能可能受影响。',
    time: '2026-11-05 18:30',
    read: true
  },
  {
    id: 5,
    type: '预约提醒',
    title: '预约提醒',
    desc: '您有一位新预约：王*珊，体检时间为 2026-11-07 08:30，请提前做好准备。',
    time: '2026-11-05 16:45',
    read: true
  },
  {
    id: 6,
    type: '报告通知',
    title: '报告通知',
    desc: '赵*伟的体检报告已生成，请登录系统查看。',
    time: '2026-11-05 15:20',
    read: true
  },
  {
    id: 7,
    type: '异常提醒',
    title: '异常提醒',
    desc: '李*萱的血脂检查结果偏高，建议复查。',
    time: '2026-11-04 17:10',
    read: true
  },
  {
    id: 8,
    type: '系统通知',
    title: '系统通知',
    desc: '您的账号已在新设备登录，如非本人操作请及时修改密码。',
    time: '2026-11-03 09:05',
    read: true
  },
  {
    id: 9,
    type: '其他消息',
    title: '排班调整',
    desc: '下周一门诊排班有调整，请查看最新排班表。',
    time: '2026-11-02 10:00',
    read: true
  },
  {
    id: 10,
    type: '其他消息',
    title: '培训通知',
    desc: '本周五下午三点在会议室开展体检报告规范填写培训。',
    time: '2026-10-30 14:00',
    read: true
  }
]

export const messageTypes = [
  { key: 'all', label: '全部消息', icon: 'ChatDotRound' },
  { key: '预约提醒', label: '预约提醒', icon: 'Calendar' },
  { key: '报告通知', label: '报告通知', icon: 'Document' },
  { key: '异常提醒', label: '异常提醒', icon: 'WarningFilled' },
  { key: '系统通知', label: '系统通知', icon: 'Bell' },
  { key: '其他消息', label: '其他消息', icon: 'MoreFilled' }
]
