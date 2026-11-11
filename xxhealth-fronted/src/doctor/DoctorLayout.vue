<template>
  <div class="doctor-shell">
    <aside class="doctor-sidebar">
      <RouterLink to="/doctor/dashboard" class="doctor-brand"
        ><img class="brand-logo" src="/doctor-assets/logo.png" alt="" />
        <div>
          <strong>熙心健康</strong><span>体检管理系统</span>
        </div></RouterLink
      >
      <nav>
        <RouterLink
          v-for="item in menu"
          :key="item.path"
          :to="'/doctor/' + item.path"
          class="doctor-nav"
          :class="{ selected: route.meta.section === item.path }"
          ><img
            class="nav-reference-icon"
            :src="'/doctor-assets/nav-' + item.path + '.png'"
            alt=""
          /><span>{{ item.label }}</span
          ><b v-if="item.path === 'notifications' && unread" class="badge">{{
            unread
          }}</b></RouterLink
        >
      </nav>
      <div class="doctor-profile">
        <img :src="avatar('doctor')" alt="医生头像" />
        <div>
          <strong>{{ workspaceSettings.profile.doctorName }}</strong
          ><span>{{ workspaceSettings.profile.title }}</span>
        </div>
      </div>
    </aside>
    <main class="doctor-main">
      <header class="doctor-topbar">
        <div class="doctor-breadcrumb">
          <RouterLink to="/doctor/dashboard"
            ><UiIcon name="HomeFilled" /> 工作台</RouterLink
          ><UiIcon name="ArrowRight" /><RouterLink
            v-if="route.meta.title === '报告详情'"
            to="/doctor/reports"
            >体检报告</RouterLink
          ><UiIcon
            v-if="route.meta.title === '报告详情'"
            name="ArrowRight"
          /><strong>{{ route.meta.title }}</strong>
        </div>
        <div class="top-actions">
          <button
            class="bell"
            aria-label="消息通知"
            @click="router.push('/doctor/notifications')"
          >
            <UiIcon name="BellFilled" /><b v-if="unread" class="badge">{{
              unread
            }}</b></button
          ><img :src="avatar('doctor')" alt="医生头像" />
        </div>
      </header>
      <section class="doctor-content"><slot /></section>
    </main>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import UiIcon from "./UiIcon.vue";
import {
  api,
  avatar,
  workspaceSettings,
  loadSettings,
  loadCatalog,
} from "./api";
const route = useRoute(),
  router = useRouter(),
  unread = ref(0);
const menu = [
  { path: "dashboard", label: "工作台", icon: "Present" },
  { path: "appointments", label: "我的预约", icon: "Calendar" },
  { path: "reports", label: "体检报告", icon: "Document" },
  { path: "patients", label: "患者管理", icon: "UserFilled" },
  { path: "packages", label: "体检套餐", icon: "FirstAidKit" },
  { path: "schedule", label: "日程管理", icon: "Memo" },
  { path: "notifications", label: "消息通知", icon: "BellFilled" },
  { path: "settings", label: "系统设置", icon: "Tools" },
];
async function refreshNotifications() {
  try {
    unread.value = (await api.get("/notifications/summary")).unread || 0;
  } catch {}
}
watch(() => route.fullPath, refreshNotifications);
onMounted(() => {
  refreshNotifications();
  loadSettings().catch(() => {});
  loadCatalog().catch(() => {});
  window.addEventListener("doctor:refresh", refreshNotifications);
});
onUnmounted(() =>
  window.removeEventListener("doctor:refresh", refreshNotifications)
);
</script>
