import http from './http'

export function createReportTemplate(orderid) {
  return http.post('/ciReport/createReportTemplate', { orderid })
}

export function listCiReport(orderid) {
  return http.post('/ciReport/listCiReport', { orderid })
}

export function updateCiDetailedReport(details) {
  return http.post('/ciDetailedReport/updateCiDetailedReport', details)
}
