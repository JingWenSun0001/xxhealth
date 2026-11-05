import { formatRange, detectError } from '@/common'

export { formatRange, detectError }

// 是否有录入结果
export function hasValue(detail) {
  if (!detail) return false
  const v = detail.value
  return v !== null && v !== undefined && String(v).trim() !== ''
}

// 汇总整份报告
export function summarize(reports = []) {
  let detailCount = 0
  let abnormalCount = 0
  let enteredCount = 0
  reports.forEach((item) => {
    ;(item.cidetailedreports || []).forEach((d) => {
      detailCount++
      if (d.iserror === 1) abnormalCount++
      if (hasValue(d)) enteredCount++
    })
  })
  return {
    itemCount: reports.length,
    detailCount,
    abnormalCount,
    enteredCount,
    normalCount: detailCount - abnormalCount,
    progress: detailCount ? Math.round((enteredCount / detailCount) * 100) : 0
  }
}

// 健康评分：正常明细占比
export function scoreOf(stats) {
  if (!stats.detailCount) return 0
  return Math.round((stats.normalCount / stats.detailCount) * 100)
}

export function scoreMeta(score, hasData = true) {
  if (!hasData) return { text: '未录入', cls: 'tag-muted', color: '#93a0b5', label: '待录入' }
  if (score >= 90) return { text: '健康优秀', cls: 'tag-normal', color: '#12b76a', label: '健康优秀' }
  if (score >= 70) return { text: '需要关注', cls: 'tag-warn', color: '#f79009', label: '需要关注' }
  return { text: '异常较多', cls: 'tag-error', color: '#f04438', label: '异常较多' }
}

export function enteredOf(item) {
  return ((item && item.cidetailedreports) || []).filter(hasValue).length
}

export function abnormalOf(item) {
  return ((item && item.cidetailedreports) || []).filter((d) => d.iserror === 1).length
}

export function resultText(detail, withUnit = true) {
  if (!detail) return '—'
  const v = detail.value
  if (v === null || v === undefined || String(v).trim() === '') return '—'
  return withUnit && detail.unit ? `${v} ${detail.unit}` : String(v)
}

export function typeText(type) {
  return { 1: '数值范围', 2: '数值相等', 3: '无需验证', 4: '文字描述', 5: '其它' }[type] || '其它'
}

// 单个检查项状态
export function statusOf(item) {
  const details = (item && item.cidetailedreports) || []
  if (abnormalOf(item) > 0) return { text: '存在异常', cls: 'tag-error' }
  if (details.length > 0 && enteredOf(item) === details.length) return { text: '已录入', cls: 'tag-normal' }
  if (enteredOf(item) > 0) return { text: '录入中', cls: 'tag-warn' }
  return { text: '待录入', cls: 'tag-muted' }
}

// 根据数据生成医生建议（无独立接口，先由异常项推导）
export function buildAdvice(reports = []) {
  const abnormal = []
  reports.forEach((item) => {
    ;(item.cidetailedreports || []).forEach((d) => {
      if (d.iserror === 1) abnormal.push(d)
    })
  })
  if (!abnormal.length) {
    return [
      '各项检查指标基本正常，建议保持健康的生活方式。',
      '每周进行 150 分钟以上的中等强度运动，规律作息。',
      '建议每年进行一次常规体检，持续关注身体变化。'
    ]
  }
  const names = abnormal.map((d) => d.name).slice(0, 3).join('、')
  return [
    `本次体检发现 ${abnormal.length} 项异常（${names}），建议结合临床进一步检查。`,
    '控制饮食，减少高脂、高盐、高糖食物摄入，戒烟限酒。',
    '建议 3~6 个月后复查相关指标，如有不适及时就诊。'
  ]
}
