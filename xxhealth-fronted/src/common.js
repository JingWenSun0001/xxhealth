/**
 * 通用工具方法（与团队约定保持一致）
 */

// 获取当前时间（YYYY-MM-DD）
export function getCurDate() {
  const now = new Date()
  const year = now.getFullYear()
  let month = now.getMonth() + 1
  let day = now.getDate()
  month = month < 10 ? '0' + month : month
  day = day < 10 ? '0' + day : day
  return year + '-' + month + '-' + day
}

// 向 sessionStorage 中存储一个 JSON 对象
export function setSessionStorage(keyStr, value) {
  sessionStorage.setItem(keyStr, JSON.stringify(value))
}

// 从 sessionStorage 中获取一个 JSON 对象（取不到时返回 null）
export function getSessionStorage(keyStr) {
  const str = sessionStorage.getItem(keyStr)
  if (str === '' || str === null || str === 'null' || str === undefined) {
    return null
  }
  try {
    return JSON.parse(str)
  } catch (e) {
    return null
  }
}

// 从 sessionStorage 中移除一个 JSON 对象
export function removeSessionStorage(keyStr) {
  sessionStorage.removeItem(keyStr)
}

// 数值型参考范围展示，如 4.0 ~ 10.0
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
 * 根据检查项明细的类型与参考范围，判断结果是否异常
 * type: 1 数值范围验证型 2 数值相等验证型 3 无需验证型 4 描述型 5 其它
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
