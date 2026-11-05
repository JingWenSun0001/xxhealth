import http from './http'

export function listOverallResultByOrderId(orderid) {
  return http.post('/overallResult/listOverallResultByOrderId', { orderid })
}

export function saveOverallResult(payload) {
  return http.post('/overallResult/saveOverallResult', payload)
}

export function updateOverallResult(payload) {
  return http.post('/overallResult/updateOverallResult', payload)
}

export function removeOverallResult(orid) {
  return http.post('/overallResult/removeOverallResult', { orid })
}
