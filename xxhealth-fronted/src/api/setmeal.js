import http from './http'

export function listSetmeal() {
  return http.post('/setmeal/listSetmeal')
}
