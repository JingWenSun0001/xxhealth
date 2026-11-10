import { createRouter, createWebHashHistory } from 'vue-router'

// ===== 用户端（队友负责）=====
import UserLogin from '../views/UserLogin.vue'
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
import DoctorLogin from '../views/Login.vue'
import DoctorLayout from '../layout/DoctorLayout.vue'
import ReportCreate from '../views/ReportCreate.vue'
import ReportEdit from '../views/ReportEdit.vue'
import ReportDetail from '../views/ReportDetail.vue'
import SetmealManage from '../views/SetmealManage.vue'
import ScheduleManage from '../views/ScheduleManage.vue'
import MessageCenter from '../views/MessageCenter.vue'
import SystemSetting from '../views/SystemSetting.vue'

const routes = [
  // 用户端
  { path: '/', name: 'home', component: UserLogin },
  { path: '/login', name: 'UserLogin', component: UserLogin, meta: { public: true, title: '用户登录' } },
  { path: '/register', name: 'Register', component: Register, meta: { public: true, title: '用户注册' } },
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

  // 医生端登录页（不带医生端布局）
  { path: '/doctor/login', name: 'DoctorLogin', component: DoctorLogin, meta: { public: true, title: '医生登录' } },

  // 医生端：统一挂在 /doctor 下（与用户端路由隔离）
  {
    path: '/doctor',
    component: DoctorLayout,
    redirect: '/doctor/report/create',
    children: [
      { path: 'report/create', name: 'ReportCreate', component: ReportCreate, meta: { needsDoctor: true, title: '新建报告' } },
      { path: 'report/edit/:orderid', name: 'ReportEdit', component: ReportEdit, meta: { needsDoctor: true, title: '编辑报告' } },
      { path: 'report/detail/:orderid', name: 'ReportDetail', component: ReportDetail, meta: { needsDoctor: true, title: '报告详情' } },
      { path: 'setmeal', name: 'SetmealManage', component: SetmealManage, meta: { needsDoctor: true, title: '体检套餐' } },
      { path: 'schedule', name: 'ScheduleManage', component: ScheduleManage, meta: { needsDoctor: true, title: '日程管理' } },
      { path: 'message', name: 'MessageCenter', component: MessageCenter, meta: { needsDoctor: true, title: '消息通知' } },
      { path: 'setting', name: 'SystemSetting', component: SystemSetting, meta: { needsDoctor: true, title: '系统设置' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 医生端页面校验医生登录态；用户端页面校验用户登录态
router.beforeEach((to, from, next) => {
  const needsDoctor = to.matched.some((record) => record.meta && record.meta.needsDoctor)
  if (needsDoctor && !sessionStorage.getItem('doctor')) {
    return next({ path: '/doctor/login', query: { redirect: to.fullPath } })
  }

  const publicPaths = ['/', '/login', '/register', '/doctor/login']
  if (!needsDoctor && !publicPaths.includes(to.path) && !sessionStorage.getItem('users')) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  next()
})

export default router
