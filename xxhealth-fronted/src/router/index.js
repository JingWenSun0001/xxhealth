import { createRouter, createWebHashHistory } from 'vue-router'

// ===== 用户端（队友负责）=====
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Index from '../views/Index.vue'
import Appointment from '../views/Appointment.vue'
import ReportList from '../views/ReportList.vue'
import Personal from '../views/Personal.vue'
import Hospital from '../views/Hospital.vue'
import Setmeal from '../views/Setmeal.vue'
import SelectDate from '../views/SelectDate.vue'
import ConfirmOrder from '../views/ConfirmOrder.vue'
import AppointmentSuccess from '../views/AppointmentSuccess.vue'
import AppointmentList from '../views/AppointmentList.vue'
import Report from '../views/Report.vue'

// ===== 医生端（我负责：体检报告模块）=====
import DoctorLayout from '../layout/DoctorLayout.vue'
import ReportCreate from '../views/ReportCreate.vue'
import ReportEdit from '../views/ReportEdit.vue'
import ReportDetail from '../views/ReportDetail.vue'

const routes = [
  // 用户端
  { path: '/', name: 'home', component: Login },
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/index', name: 'Index', component: Index },
  { path: '/appointment', name: 'Appointment', component: Appointment },
  { path: '/reportList', name: 'ReportList', component: ReportList },
  { path: '/personal', name: 'Personal', component: Personal },
  { path: '/hospital', name: 'Hospital', component: Hospital },
  { path: '/setmeal', name: 'Setmeal', component: Setmeal },
  { path: '/selectDate', name: 'SelectDate', component: SelectDate },
  { path: '/confirmOrder', name: 'ConfirmOrder', component: ConfirmOrder },
  { path: '/appointmentSuccess', name: 'AppointmentSuccess', component: AppointmentSuccess },
  { path: '/appointmentList', name: 'AppointmentList', component: AppointmentList },
  { path: '/report', name: 'Report', component: Report },

  // 医生端：体检报告三页（带医生端布局与登录校验）
  {
    path: '/report/create',
    component: DoctorLayout,
    children: [
      { path: '', name: 'ReportCreate', component: ReportCreate, meta: { needsDoctor: true, title: '新建报告' } }
    ]
  },
  {
    path: '/report/edit/:orderid',
    component: DoctorLayout,
    children: [
      { path: '', name: 'ReportEdit', component: ReportEdit, meta: { needsDoctor: true, title: '编辑报告' } }
    ]
  },
  {
    path: '/report/detail/:orderid',
    component: DoctorLayout,
    children: [
      { path: '', name: 'ReportDetail', component: ReportDetail, meta: { needsDoctor: true, title: '报告详情' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 仅医生端页面校验登录态，避免影响用户端路由
router.beforeEach((to, from, next) => {
  const needsDoctor = to.matched.some((record) => record.meta && record.meta.needsDoctor)
  if (needsDoctor && !sessionStorage.getItem('doctor')) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  next()
})

export default router
