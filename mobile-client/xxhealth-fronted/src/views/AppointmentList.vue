<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="go('/index')"><i class="fa fa-angle-left"></i></span>
      <h1>我的预约</h1>
      <button class="refresh" @click="load"><i class="fa fa-refresh"></i></button>
    </header>

    <div class="tabs">
      <span v-for="tab in tabs" :key="tab" class="tab" :class="{ active: status === tab }" @click="status = tab">{{ tab }}</span>
    </div>

    <div v-if="loading" class="tip">正在同步医生端状态…</div>
    <div v-else-if="filteredList.length === 0" class="tip">暂无{{ status === '全部' ? '' : status }}预约</div>

    <ul v-else class="list">
      <li v-for="item in filteredList" :key="item.orderId" class="card">
        <div class="row">
          <span class="name">{{ item.setmealName }}</span>
          <span class="tag" :class="statusClass(item.doctorStatus)">{{ item.doctorStatus }}</span>
        </div>
        <div class="appointment-no">{{ item.appointmentNo || ('订单 #' + item.orderId) }}</div>
        <div class="sub"><i class="fa fa-hospital-o"></i>{{ item.hospitalName }}</div>
        <div class="sub"><i class="fa fa-calendar"></i>{{ item.orderDate }} {{ item.timeSlot || '时段待医生确认' }}</div>
        <div v-if="item.room" class="sub"><i class="fa fa-map-marker"></i>{{ item.room }}</div>
        <div v-if="item.notes" class="notice">医生备注：{{ item.notes }}</div>
        <div class="row bottom">
          <span class="price">¥{{ item.price }}</span>
          <span class="actions">
            <button v-if="canCancel(item)" class="btn ghost" @click="cancel(item)">取消预约</button>
            <button v-if="item.doctorStatus === '已完成'" class="btn primary" @click="go('/reportList')">查看报告</button>
          </span>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import request from '@/utils/request'
import { mobileApi } from '@/api/mobile'
import { getSessionStorage } from '@/common'

export default {
  name: 'AppointmentList',
  data() {
    return {
      loading: true,
      status: '全部',
      list: [],
      tabs: ['全部', '待确认', '已确认', '已完成', '已取消']
    }
  },
  computed: {
    filteredList() {
      return this.status === '全部' ? this.list : this.list.filter(item => item.doctorStatus === this.status)
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      if (!getSessionStorage('users')) {
        this.$router.push('/login')
        return
      }
      this.loading = true
      try {
        this.list = await mobileApi.appointments()
      } catch (e) {
        this.list = []
        window.alert(e.message || '同步医生端预约状态失败')
      } finally {
        this.loading = false
      }
    },
    statusClass(value) {
      return { '待确认': 'wait', '已确认': 'confirmed', '已完成': 'done', '已取消': 'cancelled' }[value] || 'wait'
    },
    canCancel(item) {
      return ['待确认', '已确认'].includes(item.doctorStatus)
    },
    async cancel(item) {
      if (!window.confirm('确定要取消这条预约吗？医生端将同步收到取消状态。')) return
      try {
        const res = await request.post('/orders/removeOrders', { orderid: item.orderId })
        if (res.data === 1) {
          window.alert('预约已取消，医生端已同步')
          this.load()
        } else if (res.data === -1) {
          window.alert('体检已完成，不能取消')
        } else {
          window.alert('取消失败，订单不存在')
        }
      } catch (e) {
        window.alert('取消失败，请稍后重试')
      }
    },
    go(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; max-width: 480px; margin: 0 auto; background: #f5f7fa; padding-bottom: 24px; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 52px; padding: 0 12px; background: #10a06a; color: #fff; }
.nav h1 { font-size: 17px; font-weight: 600; }
.back, .refresh { width: 30px; border: 0; background: none; color: #fff; font-size: 20px; cursor: pointer; }
.refresh { font-size: 15px; }
.tabs { display: flex; overflow-x: auto; background: #fff; border-bottom: 1px solid #e8edf3; }
.tab { flex: 0 0 20%; min-width: 68px; text-align: center; padding: 12px 4px; font-size: 13px; color: #7b8797; cursor: pointer; }
.tab.active { color: #10a06a; font-weight: 600; border-bottom: 2px solid #10a06a; }
.list { padding: 12px; }
.card { background: #fff; border-radius: 14px; padding: 15px; margin-bottom: 12px; box-shadow: 0 2px 10px rgba(31,45,61,.05); }
.row { display: flex; align-items: center; justify-content: space-between; }
.name { font-size: 16px; font-weight: 600; color: #1f2d3d; }
.tag { font-size: 12px; padding: 3px 9px; border-radius: 12px; }
.tag.wait { color: #f08a18; background: #fff4e5; }
.tag.confirmed { color: #0c8c60; background: #e5f7ef; }
.tag.done { color: #2f7cf6; background: #eaf2ff; }
.tag.cancelled { color: #8a94a6; background: #eef1f5; }
.appointment-no { color: #a0a8b5; font-size: 11px; margin: 6px 0 8px; }
.sub { font-size: 13px; color: #6f7b8c; margin-top: 7px; }
.sub i { width: 19px; color: #10a06a; }
.notice { margin-top: 9px; padding: 8px 10px; background: #f0f8f5; border-radius: 8px; color: #507064; font-size: 12px; }
.bottom { margin-top: 12px; border-top: 1px solid #f0f2f5; padding-top: 11px; }
.price { color: #f0672c; font-size: 17px; font-weight: 600; }
.actions { display: flex; gap: 8px; }
.btn { height: 32px; border-radius: 17px; padding: 0 14px; font-size: 13px; border: 0; }
.btn.primary { background: #10a06a; color: #fff; }
.btn.ghost { background: #fff; color: #e5484d; border: 1px solid #e5484d; }
.tip { text-align: center; color: #8a94a6; font-size: 14px; padding: 70px 0; }
</style>
