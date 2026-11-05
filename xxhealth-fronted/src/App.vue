<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">
          <span class="brand-heart">♥</span>
          <span class="brand-cross">+</span>
        </div>
        <div class="brand-copy">
          <strong>熙心健康</strong>
          <span>体检管理系统</span>
        </div>
      </div>

      <nav>
        <RouterLink to="/orders" class="nav-item">
          <el-icon><Document /></el-icon>
          <span>体检报告</span>
        </RouterLink>
      </nav>

      <div class="doctor-profile">
        <div class="doctor-avatar">医</div>
        <div>
          <strong>张医生</strong>
          <span>医生</span>
        </div>
      </div>
    </aside>

    <main class="main-area">
      <header class="topbar">
        <div class="breadcrumb">
          <span class="home-chip"><el-icon><HomeFilled /></el-icon></span>
          <span class="breadcrumb-home">工作台</span>
          <el-icon class="breadcrumb-arrow"><ArrowRight /></el-icon>
          <strong>{{ route.meta.title || '体检报告' }}</strong>
        </div>
        <div class="topbar-actions">
          <button class="notification-button" type="button" aria-label="通知">
            <el-icon><BellFilled /></el-icon>
            <span>3</span>
          </button>
          <div class="topbar-avatar">医</div>
        </div>
      </header>
      <section class="page-content">
        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup>
import { ArrowRight, BellFilled, Document, HomeFilled } from '@element-plus/icons-vue'
import { useRoute } from 'vue-router'

const route = useRoute()
</script>
