<template>
  <div class="app">
    <div class="quick">
      <router-link v-for="item in entries" :key="item.path" :to="item.path" class="quick-item">
        <i :class="['fa', item.icon]"></i>
        <span>{{ item.label }}</span>
      </router-link>
    </div>

    <router-link v-if="summary && summary.nextAppointment" to="/appointmentList" class="sync-card">
      <span class="sync-icon"><i class="fa fa-calendar-check-o"></i></span>
      <span class="sync-main">
        <strong>下一次体检 · {{ summary.nextAppointment.doctorStatus }}</strong>
        <small>{{ summary.nextAppointment.orderDate }} {{ summary.nextAppointment.timeSlot || '等待医生安排时段' }}</small>
      </span>
      <i class="fa fa-angle-right"></i>
    </router-link>

    <div class="section-title">
      <h3>热门套餐</h3>
      <router-link to="/appointment" class="more">去预约 <i class="fa fa-angle-right"></i></router-link>
    </div>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!hotPackages.length" class="empty">暂无套餐</div>
    <div
      v-for="item in hotPackages"
      :key="item.packageId"
      class="card setmeal"
      @click="goPackage(item)"
    >
      <div class="setmeal-main">
        <h4>{{ item.name }}</h4>
        <p>{{ item.audience }} · {{ item.gender }}</p>
      </div>
      <div class="setmeal-side">
        <span class="price">¥{{ item.price }}</span>
        <i class="fa fa-angle-right"></i>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<script>
import { mobileApi } from '@/api/mobile'
import { getSessionStorage } from '@/common'
import TabBar from '@/components/TabBar.vue'

export default {
  name: 'Index',
  components: { TabBar },
  data() {
    return {
      user: null,
      summary: null,
      packages: [],
      loading: true,
      entries: [
        { path: '/appointment', label: '预约体检', icon: 'fa-calendar-plus-o' },
        { path: '/reportList', label: '报告查询', icon: 'fa-file-text-o' },
        { path: '/appointmentList', label: '我的预约', icon: 'fa-list-alt' },
        { path: '/messages', label: '健康消息', icon: 'fa-bell-o' }
      ]
    }
  },
  computed: {
    hotPackages() {
      return this.packages.slice(0, 3)
    }
  },
  created() {
    this.user = getSessionStorage('users')
    if (!this.user) {
      this.$router.replace('/login')
      return
    }
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      const [packages, summary] = await Promise.all([
        mobileApi.packages().catch(() => []),
        mobileApi.summary().catch(() => null)
      ])
      this.packages = packages
      this.summary = summary
      this.loading = false
    },
    goPackage(item) {
      this.$router.push({ path: '/appointment', query: { packageId: item.packageId } })
    }
  }
}
</script>

<style scoped>
.quick {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  margin: 16px 16px 0;
  padding: 16px 6px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 14px rgba(31, 45, 61, 0.06);
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 7px;
  color: #1f2d3d;
  font-size: 12px;
}
.quick-item i {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #e6f6ef;
  color: #10a06a;
  font-size: 18px;
}
.sync-card { display: flex; align-items: center; gap: 11px; margin: 12px 16px 0; padding: 13px 14px; border-radius: 13px; background: #fff; color: #1f2d3d; box-shadow: 0 2px 10px rgba(31,45,61,.04); }
.sync-icon { width: 38px; height: 38px; display: flex; align-items: center; justify-content: center; border-radius: 12px; background: #e6f6ef; color: #10a06a; }
.sync-main { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.sync-main strong { font-size: 13px; }
.sync-main small { color: #8a94a6; font-size: 11px; }
.sync-card > i { color: #c3cad4; }
.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 20px 16px 6px;
}
.section-title h3 {
  font-size: 16px;
}
.more {
  color: #8a94a6;
  font-size: 12px;
}
.setmeal {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.setmeal-main h4 {
  font-size: 15px;
}
.setmeal-main p {
  margin-top: 6px;
  color: #8a94a6;
  font-size: 12px;
}
.setmeal-side {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #c3cad4;
}
</style>
