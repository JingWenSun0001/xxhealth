import { createRouter, createWebHashHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import DoctorLayout from '@/layout/DoctorLayout.vue'
import ReportCreate from '@/views/ReportCreate.vue'
import ReportEdit from '@/views/ReportEdit.vue'
import ReportDetail from '@/views/ReportDetail.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { public: true, title: '医生登录' }
  },
  {
    path: '/',
    component: DoctorLayout,
    redirect: '/report/create',
    children: [
      {
        path: 'report',
        redirect: '/report/create'
      },
      {
        path: 'report/create',
        name: 'ReportCreate',
        component: ReportCreate,
        meta: { title: '新建报告' }
      },
      {
        path: 'report/edit/:orderid',
        name: 'ReportEdit',
        component: ReportEdit,
        meta: { title: '编辑报告' }
      },
      {
        path: 'report/detail/:orderid',
        name: 'ReportDetail',
        component: ReportDetail,
        meta: { title: '报告详情' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/report/create'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 登录权限校验
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} · 熙心健康体检管理系统`
    : '熙心健康 · 体检管理系统'

  const doctor = sessionStorage.getItem('doctor')
  if (to.meta.public) {
    if (doctor && (to.path === '/login' || to.path === '/')) {
      return next('/report/create')
    }
    return next()
  }
  if (!doctor) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

export default router
