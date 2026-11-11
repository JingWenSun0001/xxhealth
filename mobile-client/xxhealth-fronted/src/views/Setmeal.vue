<template>
  <div class="app">
    <div class="topbar">
      <button class="back" @click="goBack">
        <i class="fa fa-angle-left"></i>
      </button>
      <h1>选择体检套餐</h1>
      <span class="step">2 / 3</span>
    </div>

    <div class="tabs">
      <div class="tab" :class="{ active: type === 1 }" @click="switchType(1)">男士套餐</div>
      <div class="tab" :class="{ active: type === 0 }" @click="switchType(0)">女士套餐</div>
    </div>

    <div v-if="hpid" class="selected">
      <i class="fa fa-hospital-o"></i> 已选医院：{{ hospitalName || '编号 ' + hpid }}
    </div>
    <div v-else class="selected warn">
      <i class="fa fa-info-circle"></i> 还未选择医院，预约前需要先选择医院
    </div>

    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!setmeals.length" class="empty">该类型暂无套餐</div>

    <div v-for="item in setmeals" :key="item.smid" class="card setmeal" @click="openDetail(item)">
      <div class="setmeal-main">
        <h3>{{ item.name }}</h3>
        <p>{{ item.type === 1 ? '男士套餐' : '女士套餐' }}</p>
      </div>
      <div class="setmeal-side">
        <span class="price">¥{{ item.price }}</span>
        <span class="detail-link">查看详情 <i class="fa fa-angle-right"></i></span>
      </div>
    </div>

    <div v-if="detail" class="modal-mask" @click.self="closeDetail">
      <div class="modal">
        <div class="modal-head">
          <h3>{{ detail.name }}</h3>
          <button class="close" @click="closeDetail"><i class="fa fa-times"></i></button>
        </div>
        <p class="modal-sub">
          <span class="price">¥{{ detail.price }}</span>
          <span class="tag">{{ detail.type === 1 ? '男士套餐' : '女士套餐' }}</span>
        </p>

        <h4 class="items-title">包含 {{ (detail.items || []).length }} 个检查项</h4>
        <ul class="items">
          <li v-for="it in detail.items" :key="it.ciid">
            <b>{{ it.ciname }}</b>
            <p>{{ it.cicontent }}</p>
            <span v-if="it.meaning">{{ it.meaning }}</span>
          </li>
        </ul>

        <button class="btn btn-primary btn-block" @click="book">立即预约</button>
      </div>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'Setmeal',
  data() {
    return {
      setmeals: [],
      loading: true,
      type: 1,
      hpid: null,
      hospitalName: '',
      detail: null
    }
  },
  created() {
    const q = this.$route.query
    this.type = q.type !== undefined ? Number(q.type) : 1
    this.hpid = q.hpid ? Number(q.hpid) : null
    this.loadSetmeals()
    if (this.hpid) this.loadHospital()
    if (q.smid) this.openDetail({ smid: Number(q.smid) })
  },
  methods: {
    async loadSetmeals() {
      this.loading = true
      try {
        const res = await request.post('/setmeal/listSetmealByType', { type: this.type })
        this.setmeals = Array.isArray(res.data) ? res.data : []
      } catch (e) {
        this.setmeals = []
      } finally {
        this.loading = false
      }
    },
    async loadHospital() {
      try {
        const res = await request.post('/hospital/getHospitalById', { hpid: this.hpid })
        this.hospitalName = res.data && res.data.name ? res.data.name : ''
      } catch (e) {
        this.hospitalName = ''
      }
    },
    switchType(t) {
      if (t === this.type) return
      this.type = t
      this.detail = null
      const query = { type: t }
      if (this.hpid) query.hpid = this.hpid
      this.$router.replace({ path: '/setmeal', query })
      this.loadSetmeals()
    },
    goBack() {
      const query = { type: this.type }
      if (this.hpid) query.hpid = this.hpid
      this.$router.push({ path: '/hospital', query })
    },
    async openDetail(item) {
      try {
        const res = await request.post('/setmeal/getSetmealById', { smid: item.smid })
        this.detail = res.data || null
      } catch (e) {
        this.detail = null
      }
    },
    closeDetail() {
      this.detail = null
    },
    book() {
      if (!this.detail) return
      if (!this.hpid) {
        this.$router.push({ path: '/hospital', query: { type: this.type, smid: this.detail.smid } })
        return
      }
      this.$router.push({
        path: '/selectDate',
        query: { hpid: this.hpid, smid: this.detail.smid, type: this.type }
      })
    }
  }
}
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 8px;
  margin: 14px 16px 4px;
}
.tab {
  flex: 1;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #fff;
  color: #8a94a6;
  font-size: 14px;
}
.tab.active {
  background: #10a06a;
  color: #fff;
  font-weight: 600;
}
.selected {
  margin: 10px 16px 0;
  padding: 9px 12px;
  border-radius: 10px;
  background: #e6f6ef;
  color: #0b8557;
  font-size: 12px;
}
.selected.warn {
  background: #fff7e8;
  color: #b7791f;
}
.setmeal {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.setmeal-main h3 {
  font-size: 15px;
}
.setmeal-main p {
  margin-top: 6px;
  color: #8a94a6;
  font-size: 12px;
}
.setmeal-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}
.detail-link {
  color: #10a06a;
  font-size: 12px;
}
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 40;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
}
.modal {
  width: 100%;
  max-width: 480px;
  max-height: 82vh;
  overflow-y: auto;
  padding: 18px 18px 24px;
  background: #fff;
  border-radius: 18px 18px 0 0;
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.modal-head h3 {
  font-size: 17px;
}
.close {
  border: none;
  background: none;
  color: #8a94a6;
  font-size: 18px;
}
.modal-sub {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 8px 0 16px;
}
.items-title {
  margin-bottom: 8px;
  font-size: 14px;
  color: #1f2d3d;
}
.items li {
  padding: 12px 0;
  border-bottom: 1px solid #f2f5f8;
}
.items li b {
  font-size: 14px;
}
.items li p {
  margin-top: 4px;
  color: #6b7787;
  font-size: 13px;
  line-height: 1.5;
}
.items li span {
  display: block;
  margin-top: 4px;
  color: #8a94a6;
  font-size: 12px;
}
.modal .btn {
  margin-top: 18px;
}
</style>
