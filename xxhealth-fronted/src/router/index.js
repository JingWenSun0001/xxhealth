import { createRouter, createWebHashHistory } from "vue-router";
async function preferredLanding() {
  try {
    const { loadSettings } = await import("../doctor/api");
    const s = await loadSettings();
    return "/doctor/" + s.preferences.landingPage;
  } catch {
    return "/doctor/dashboard";
  }
}
const routes = [
  {
    path: "/",
    component: { render: () => null },
    beforeEnter: preferredLanding,
  },
  {
    path: "/doctor",
    component: { render: () => null },
    beforeEnter: preferredLanding,
  },
  {
    path: "/doctor/dashboard",
    component: () => import("../doctor/Dashboard.vue"),
    meta: { title: "工作台", section: "dashboard" },
  },
  {
    path: "/doctor/appointments",
    component: () => import("../doctor/Appointments.vue"),
    meta: { title: "我的预约", section: "appointments" },
  },
  {
    path: "/doctor/patients",
    component: () => import("../doctor/Patients.vue"),
    meta: { title: "患者管理", section: "patients" },
  },
  {
    path: "/doctor/reports",
    component: () => import("../doctor/Reports.vue"),
    meta: { title: "体检报告", section: "reports" },
  },
  {
    path: "/doctor/reports/new",
    component: () => import("../doctor/ReportCreate.vue"),
    meta: { title: "新建报告", section: "dashboard" },
  },
  {
    path: "/doctor/reports/:id/edit",
    component: () => import("../doctor/ReportEditor.vue"),
    meta: { title: "编辑报告", section: "dashboard" },
  },
  {
    path: "/doctor/reports/:id",
    component: () => import("../doctor/ReportDetail.vue"),
    meta: { title: "报告详情", section: "reports" },
  },
  {
    path: "/doctor/templates",
    component: () => import("../doctor/Templates.vue"),
    meta: { title: "报告模板", section: "dashboard" },
  },
  {
    path: "/doctor/history",
    component: () => import("../doctor/Reports.vue"),
    meta: { title: "历史报告", section: "dashboard" },
  },
  {
    path: "/doctor/alerts",
    component: () => import("../doctor/Alerts.vue"),
    meta: { title: "异常提醒", section: "dashboard" },
  },
  {
    path: "/doctor/packages",
    component: () => import("../doctor/Packages.vue"),
    meta: { title: "体检套餐", section: "packages" },
  },
  {
    path: "/doctor/schedule",
    component: () => import("../doctor/Schedule.vue"),
    meta: { title: "日程管理", section: "schedule" },
  },
  {
    path: "/doctor/notifications",
    component: () => import("../doctor/Notifications.vue"),
    meta: { title: "消息通知", section: "notifications" },
  },
  {
    path: "/doctor/settings",
    component: () => import("../doctor/Settings.vue"),
    meta: { title: "系统设置", section: "settings" },
  },
  { path: "/orders", redirect: "/doctor/reports" },
  { path: "/login", component: () => import("../views/Login.vue") },
  { path: "/register", component: () => import("../views/Register.vue") },
  { path: "/index", component: () => import("../views/Index.vue") },
  { path: "/appointment", component: () => import("../views/Appointment.vue") },
  { path: "/reportList", component: () => import("../views/ReportList.vue") },
  { path: "/personal", component: () => import("../views/Personal.vue") },
  { path: "/hospital", component: () => import("../views/Hospital.vue") },
  { path: "/setmeal", component: () => import("../views/Setmeal.vue") },
  { path: "/selectDate", component: () => import("../views/SelectDate.vue") },
  {
    path: "/confirmOrder",
    component: () => import("../views/ConfirmOrder.vue"),
  },
  {
    path: "/appointmentSuccess",
    component: () => import("../views/AppointmentSuccess.vue"),
  },
  {
    path: "/appointmentList",
    component: () => import("../views/AppointmentList.vue"),
  },
  { path: "/report", component: () => import("../views/Report.vue") },
  { path: "/:pathMatch(.*)*", redirect: "/doctor/dashboard" },
];
export default createRouter({ history: createWebHashHistory(), routes });
