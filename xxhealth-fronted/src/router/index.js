import { createRouter, createWebHashHistory } from 'vue-router'

// ===== 用户端 =====
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

// ===== 医生端（体检报告 / 套餐 / 日程 / 消息 / 设置）=====
import DoctorLogin from '../views/Login.vue'
import DoctorLayout from '../layout/DoctorLayout.vue'
import ReportCreate from '../views/ReportCreate.vue'
import ReportEdit from '../views/ReportEdit.vue'
import ReportDetail from '../views/ReportDetail.vue'
import SetmealManage from '../views/SetmealManage.vue'
import ScheduleManage from '../views/ScheduleManage.vue'
import MessageCenter from '../views/MessageCenter.vue'
import SystemSetting from '../views/SystemSetting.vue'

// ===== 医生端（订单 / 总检，队友模块）=====
import OrdersLayout from '../layout/OrdersLayout.vue'
import OrdersView from '../views/OrdersView.vue'
import OrderDetailView from '../views/OrderDetailView.vue'

const routes = [
  { path: '/', redirect: '/doctor/login' },

  // 登录 / 注册（公开）
  { path: '/login', name: 'UserLogin', component: UserLogin, meta: { public: true, title: '用户登录' } },
  { path: '/register', name: 'Register', component: Register, meta: { public: true, title: '医生注册' } },
  { path: '/doctor/login', name: 'DoctorLogin', component: DoctorLogin, meta: { public: true, title: '医生登录' } },

  // 用户端页面（需要用户登录）
  { path: '/index', name: 'Index', component: Index, meta: { title: '首页' } },
  { path: '/appointment', name: 'Appointment', component: Appointment, meta: { title: '体检预约' } },
  { path: '/reportList', name: 'ReportList', component: ReportList, meta: { title: '体检报告' } },
  { path: '/personal', name: 'Personal', component: Personal, meta: { title: '个人中心' } },
  { path: '/hospital', name: 'Hospital', component: Hospital, meta: { title: '选择医院' } },
  { path: '/setmeal', name: 'Setmeal', component: Setmeal, meta: { title: '体检套餐' } },
  { path: '/selectDate', name: 'SelectDate', component: SelectDate, meta: { title: '选择日期' } },
  { path: '/confirmOrder', name: 'ConfirmOrder', component: ConfirmOrder, meta: { title: '确认订单' } },
  { path: '/appointmentSuccess', name: 'AppointmentSuccess', component: AppointmentSuccess, meta: { title: '预约成功' } },
  { path: '/appointmentList', name: 'AppointmentList', component: AppointmentList, meta: { title: '预约列表' } },
  { path: '/report', name: 'Report', component: Report, meta: { title: '体检报告' } },

  // 医生端（统一 /doctor 前缀，需要医生登录）
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
  },

  // 医生端订单 / 总检（队友模块，使用其独立布局）
  {
    path: '/orders',
    component: OrdersLayout,
    meta: { needsDoctor: true },
    children: [
      { path: '', name: 'OrdersView', component: OrdersView, meta: { needsDoctor: true, title: '体检报告管理' } },
      { path: ':orderId', name: 'OrderDetailView', component: OrderDetailView, meta: { needsDoctor: true, title: '体检报告详情' } }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 医生端页面校验医生登录态；用户端页面校验用户登录态
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} · 熙心健康体检管理系统`
    : '熙心健康 · 体检管理系统'

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
