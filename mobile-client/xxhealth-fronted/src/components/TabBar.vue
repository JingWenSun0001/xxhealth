<template>
  <nav class="tabbar">
    <router-link
      v-for="item in items"
      :key="item.path"
      :to="item.path"
      class="tabbar-item"
      :class="{ active: $route.path === item.path }"
    >
      <i :class="['fa', item.icon]"></i>
      <span>{{ item.label }}</span>
    </router-link>
  </nav>
</template>

<script>
export default {
  name: 'TabBar',
  data() {
    return {
      items: [
        { path: '/index', label: '首页', icon: 'fa-home' },
        { path: '/appointment', label: '预约', icon: 'fa-calendar-plus-o' },
        { path: '/reportList', label: '报告', icon: 'fa-file-text-o' },
        { path: '/personal', label: '我的', icon: 'fa-user-o' }
      ]
    }
  }
}
</script>
