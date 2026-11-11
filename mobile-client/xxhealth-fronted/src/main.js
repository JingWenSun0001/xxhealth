import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

import 'font-awesome/css/font-awesome.min.css'
import './assets/app.css'

// 路由守卫：除登录、注册外，未登录一律跳登录页
router.beforeEach(function (to, from, next) {
  const users = sessionStorage.getItem('users')
  const whiteList = ['/', '/login', '/register']
  if (!whiteList.includes(to.path) && users == null) {
    next('/login')
  } else {
    next()
  }
})

createApp(App).use(router).mount('#app')
