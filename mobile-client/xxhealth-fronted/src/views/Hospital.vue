<template>
  <div class="app">
    <div class="topbar">
      <button class="back" @click="goBack">
        <i class="fa fa-angle-left"></i>
      </button>
      <h1>选择体检机构</h1>
      <span class="step">2 / 4</span>
    </div>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!hospitals.length" class="empty">暂无可用医院</div>

    <div v-for="h in hospitals" :key="h.hpid" class="card hospital">
      <div class="h-head">
        <h3>{{ h.name }}</h3>
        <span class="tag">正常营业</span>
      </div>
      <p class="line"><i class="fa fa-map-marker"></i>{{ h.address }}</p>
      <p class="line"><i class="fa fa-phone"></i>{{ h.telephone }}</p>
      <p class="line"><i class="fa fa-clock-o"></i>营业时间 {{ h.businesshours }} · 采血截止 {{ h.deadline }}</p>
      <p class="line"><i class="fa fa-users"></i>{{ h.rule }}</p>
      <button class="btn btn-primary btn-block" @click="select(h)">选择该医院</button>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Hospital',
  data() {
    return {
      hospitals: [],
      loading: true,
      packageId: null
    }
  },
  created() {
    const q = this.$route.query
    this.packageId = q.packageId ? Number(q.packageId) : null
    if (!this.packageId) {
      this.$router.replace('/appointment')
      return
    }
    this.loadHospitals()
  },
  methods: {
    async loadHospitals() {
      this.loading = true
      try {
        const res = await request.post('/hospital/listHospital', { state: 1 })
        this.hospitals = Array.isArray(res.data) ? res.data : []
      } catch (e) {
        this.hospitals = []
      } finally {
        this.loading = false
      }
    },
    select(h) {
      this.$router.push({ path: '/selectDate', query: { hpid: h.hpid, packageId: this.packageId } })
    },
    goBack() {
      this.$router.push('/appointment')
    }
  }
}
</script>

<style scoped>
.hospital {
  padding-bottom: 18px;
}
.h-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}
.h-head h3 {
  font-size: 16px;
  line-height: 1.4;
}
.line {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
  color: #6b7787;
  font-size: 13px;
  line-height: 1.5;
}
.line i {
  width: 14px;
  margin-top: 2px;
  color: #10a06a;
  text-align: center;
}
.hospital .btn {
  margin-top: 12px;
}
</style>
