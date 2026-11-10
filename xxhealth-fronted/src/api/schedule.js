import request from './request'

/**
 * 按月查询医生端预约（日历打点 + 当月列表）
 */
export function listScheduleByMonth(year, month) {
  return request.post('/schedule/listByMonth', { year, month })
}

/**
 * 某一天的日程详情：预约列表 + 检查提醒 + 异常提醒
 */
export function getScheduleDay(date) {
  return request.post('/schedule/dayDetail', { date })
}
