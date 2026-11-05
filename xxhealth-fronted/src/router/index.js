import { createRouter, createWebHashHistory } from 'vue-router'
import OrdersView from '../views/OrdersView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: '/', redirect: '/orders' },
    {
      path: '/orders',
      name: 'orders',
      component: OrdersView,
      meta: { title: '体检报告管理' }
    },
    {
      path: '/orders/:orderId',
      name: 'order-detail',
      component: OrderDetailView,
      meta: { title: '体检报告详情' }
    }
  ]
})

export default router
