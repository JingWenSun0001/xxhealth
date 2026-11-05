<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-logo">
          <svg viewBox="0 0 32 32" width="26" height="26">
            <path d="M16 26s-9-5.2-9-11.5A5.4 5.4 0 0 1 16 10a5.4 5.4 0 0 1 9 4.5C25 20.8 16 26 16 26z" fill="#fff" />
            <path d="M7 16h5l2.2-3.6 2.6 6.4 2-3.2H25" fill="none" stroke="#2f6bff" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </div>
        <div class="brand-text">
          <h1>熙心健康</h1>
          <p>体检管理系统</p>
        </div>
      </div>

      <nav class="menu">
        <div
          v-for="item in menuItems"
          :key="item.key"
          class="menu-item"
          :class="{ active: isActive(item) }"
          @click="handleMenu(item)"
        >
          <el-icon class="menu-icon"><component :is="item.icon" /></el-icon>
          <span class="menu-label">{{ item.label }}</span>
          <span v-if="item.badge" class="menu-badge">{{ item.badge }}</span>
        </div>
      </nav>

      <div class="sidebar-footer">
        <div class="doctor-avatar">{{ avatarText }}</div>
        <div class="doctor-meta">
          <p class="doctor-name">{{ doctor.realname || '医生' }}</p>
          <p class="doctor-role">{{ deptText }}</p>
        </div>
      </div>
    </aside>

    <!-- 主区域 -->
    <div class="main">
      <header class="topbar">
        <div class="breadcrumb">
          <el-icon class="home-icon"><HomeFilled /></el-icon>
          <span class="crumb">工作台</span>
          <template v-for="(crumb, index) in breadcrumbs" :key="index">
            <el-icon class="separator"><ArrowRight /></el-icon>
            <span class="crumb" :class="{ current: index === breadcrumbs.length - 1 }">{{ crumb }}</span>
          </template>
        </div>

        <div class="topbar-right">
          <el-badge :value="3" :max="99" class="bell-badge">
            <button class="icon-btn" type="button" @click="handleUnimplemented('消息通知')">
              <el-icon><Bell /></el-icon>
            </button>
          </el-badge>
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user">
              <div class="user-avatar">{{ avatarText }}</div>
              <div class="user-meta">
                <span>{{ doctor.realname || '医生' }}</span>
                <small>{{ deptText }}</small>
              </div>
              <el-icon class="caret"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" disabled>个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { removeSessionStorage, getSessionStorage } from '@/common'

const route = useRoute()
const router = useRouter()

const doctor = getSessionStorage('doctor') || {}

const deptMap = { 1: '检验科', 2: '内科', 3: '外科' }
const deptText = computed(() => deptMap[doctor.deptno] || '医生')

const avatarText = computed(() => {
  const name = doctor.realname || '医'
  // 中文姓名取姓氏（首字）
  return name.slice(0, 1)
})

const menuItems = [
  { key: 'workbench', label: '工作台', icon: 'Grid', path: '/workbench' },
  { key: 'appointment', label: '我的预约', icon: 'Calendar', path: '/appointment' },
  { key: 'report', label: '体检报告', icon: 'Document', path: '/report/create' },
  { key: 'patient', label: '患者管理', icon: 'User', path: '/patient' },
  { key: 'setmeal', label: '体检套餐', icon: 'FirstAidKit', path: '/setmeal' },
  { key: 'schedule', label: '日程管理', icon: 'Calendar', path: '/schedule' },
  { key: 'message', label: '消息通知', icon: 'Bell', path: '/message', badge: 3 },
  { key: 'setting', label: '系统设置', icon: 'Setting', path: '/setting' }
]

const isActive = (item) => {
  if (item.key === 'report') return route.path.startsWith('/report')
  return route.path === item.path
}

const breadcrumbs = computed(() => {
  if (route.path.startsWith('/report/create')) return ['体检报告', '新建报告']
  if (route.path.startsWith('/report/edit')) return ['体检报告', '编辑报告']
  if (route.path.startsWith('/report/detail')) return ['体检报告', '报告详情']
  return ['体检报告']
})

function handleMenu(item) {
  if (item.key === 'report') {
    router.push('/report/create')
    return
  }
  handleUnimplemented(item.label)
}

function handleUnimplemented(label) {
  ElMessage.info(`「${label}」模块由其他同学负责，当前分支暂未接入`)
}

function handleCommand(command) {
  if (command === 'logout') {
    removeSessionStorage('doctor')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  width: 100%;
  height: 100%;
  min-width: 1180px;
  background: radial-gradient(1200px 420px at 55% -140px, #e6efff 0%, var(--page-bg) 62%);
}

/* ---------- 侧边栏 ---------- */
.sidebar {
  width: 224px;
  flex-shrink: 0;
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  padding: 22px 14px 18px;
  color: var(--sidebar-text);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 8px 22px;
}

.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  background: linear-gradient(135deg, #2f6bff, #4f8bff);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 18px rgba(47, 107, 255, 0.45);
  flex-shrink: 0;
}

.brand-text h1 {
  font-size: 17px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.6px;
}

.brand-text p {
  margin-top: 3px;
  font-size: 11px;
  color: #8fa3c0;
  letter-spacing: 0.5px;
}

.menu {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  overflow-y: auto;
}

.menu-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
  height: 46px;
  padding: 0 14px;
  border-radius: 12px;
  cursor: pointer;
  font-size: 14px;
  color: var(--sidebar-text);
  transition: background 0.18s ease, color 0.18s ease, transform 0.18s ease;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.07);
  color: #fff;
}

.menu-item.active {
  background: linear-gradient(120deg, #2f6bff, #4f8bff);
  color: #fff;
  box-shadow: 0 10px 22px rgba(47, 107, 255, 0.35);
}

.menu-icon {
  font-size: 17px;
}

.menu-label {
  flex: 1;
  letter-spacing: 0.4px;
}

.menu-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #f04438;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.sidebar-footer {
  margin-top: 16px;
  padding: 14px 10px 4px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  gap: 10px;
}

.doctor-avatar,
.user-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #5aa0ff, #2f6bff);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.doctor-name {
  font-size: 13px;
  color: #fff;
  font-weight: 600;
}

.doctor-role {
  margin-top: 2px;
  font-size: 11px;
  color: #8fa3c0;
}

/* ---------- 主区域 ---------- */
.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.topbar {
  height: 64px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-2);
}

.home-icon {
  font-size: 16px;
  color: var(--brand);
}

.separator {
  font-size: 12px;
  color: #c3cddd;
}

.crumb.current {
  color: var(--text-1);
  font-weight: 600;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.bell-badge :deep(.el-badge__content) {
  border: 2px solid #fff;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--text-2);
  font-size: 17px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.18s ease;
}

.icon-btn:hover {
  color: var(--brand);
  border-color: #c9d9ff;
  background: #f5f8ff;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 12px;
  transition: background 0.18s ease;
}

.user:hover {
  background: #f3f7ff;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}

.user-meta span {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}

.user-meta small {
  font-size: 11px;
  color: var(--text-3);
}

.caret {
  font-size: 12px;
  color: var(--text-3);
}

.content {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px 32px;
}
</style>
