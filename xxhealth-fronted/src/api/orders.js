import http from './http'

export function listOrders(params) {
  return http.post('/orders/listOrders', params)
}

export function getOrdersById(orderid) {
  return http.post('/orders/getOrdersById', { orderid })
}

export function updateOrdersState(orderid, state) {
  return http.post('/orders/updateOrdersState', { orderid, state })
}
