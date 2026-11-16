// 参考范围展示，如 90-140/60-90 或 4.0 ~ 10.0
export function formatRange(detail) {
  if (!detail) return '—'
  if (detail.normalvaluestring) return detail.normalvaluestring
  if (detail.minrange !== null && detail.minrange !== undefined &&
      detail.maxrange !== null && detail.maxrange !== undefined) {
    return `${detail.minrange} ~ ${detail.maxrange}`
  }
  if (detail.normalvalue) return detail.normalvalue
  return '—'
}

/**
 * 根据检查项明细的类型与参考范围判断结果是否异常
 * type: 1 数值范围型 2 数值相等型 3 无需验证型 4 描述型 5 其它
 * @returns {number} 1 异常 / 0 正常
 */
export function detectError(detail, value) {
  if (!detail) return 0
  const type = detail.type
  const text = value === null || value === undefined ? '' : String(value).trim()

  if (type === 1) {
    if (text === '') return 0
    const num = Number(text)
    if (Number.isNaN(num)) return 0
    if (detail.minrange !== null && detail.minrange !== undefined && num < Number(detail.minrange)) {
      return 1
    }
    if (detail.maxrange !== null && detail.maxrange !== undefined && num > Number(detail.maxrange)) {
      return 1
    }
    return 0
  }

  if (type === 2) {
    if (text === '') return 0
    return detail.normalvalue !== null && detail.normalvalue !== undefined &&
      text !== String(detail.normalvalue).trim() ? 1 : 0
  }

  // 无需验证 / 描述型 / 其它
  return 0
}

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
