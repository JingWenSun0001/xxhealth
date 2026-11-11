<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="goBack"><i class="fa fa-angle-left"></i></span>
      <h1>选择体检日期</h1>
      <span class="placeholder"></span>
    </header>

    <div class="month-bar">
      <span class="arrow" @click="changeMonth(-1)"><i class="fa fa-angle-left"></i></span>
      <span class="month">{{ monthTitle }}</span>
      <span class="arrow" @click="changeMonth(1)"><i class="fa fa-angle-right"></i></span>
    </div>

    <div class="calendar">
      <div class="week-row">
        <span v-for="w in weeks" :key="w" class="week">{{ w }}</span>
      </div>

      <div v-if="loading" class="tip">加载中...</div>
      <div v-else class="grid">
        <span v-for="n in offset" :key="'b' + n" class="cell blank"></span>
        <div
          v-for="d in days"
          :key="d.date"
          class="cell"
          :class="{ disabled: isDisabled(d), selected: selected === d.date }"
          @click="select(d)"
        >
          <span class="dnum">{{ dayNum(d.date) }}</span>
          <span class="dinfo">{{ d.full ? '已满' : '余' + d.remaining }}</span>
        </div>
      </div>
    </div>

    <div class="footer">
      <div class="chosen" v-if="selected">已选：{{ selected }}</div>
      <button class="btn" @click="next">下一步</button>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'
import { getCurDate } from '@/common'

export default {
  name: 'SelectDate',
  data() {
    return {
      hpid: null,
      packageId: null,
      year: 0,
      month: 0,
      days: [],
      loading: true,
      selected: '',
      weeks: ['日', '一', '二', '三', '四', '五', '六']
    }
  },
  computed: {
    monthTitle() {
      return this.year + ' 年 ' + this.month + ' 月'
    },
    offset() {
      if (!this.year) return 0
      return new Date(this.year, this.month - 1, 1).getDay()
    }
  },
  created() {
    this.hpid = Number(this.$route.query.hpid) || 1
    this.packageId = Number(this.$route.query.packageId) || 0
    if (!this.packageId) {
      this.$router.replace('/appointment')
      return
    }
    const now = new Date()
    this.year = now.getFullYear()
    this.month = now.getMonth() + 1
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const res = await request.post('/calendar/listAppointmentCalendar', {
          hpid: this.hpid,
          year: this.year,
          month: this.month
        })
        this.days = res.data || []
      } catch (e) {
        this.days = []
      } finally {
        this.loading = false
      }
    },
    changeMonth(step) {
      let y = this.year
      let m = this.month + step
      if (m < 1) {
        m = 12
        y -= 1
      } else if (m > 12) {
        m = 1
        y += 1
      }
      this.year = y
      this.month = m
      this.selected = ''
      this.load()
    },
    isDisabled(d) {
      return d.full || d.date < getCurDate()
    },
    select(d) {
      if (this.isDisabled(d)) return
      this.selected = d.date
    },
    dayNum(date) {
      return Number(date.slice(8, 10))
    },
    next() {
      if (!this.selected) {
        window.alert('请选择体检日期')
        return
      }
      this.$router.push({
        path: '/confirmOrder',
        query: { hpid: this.hpid, packageId: this.packageId, date: this.selected }
      })
    },
    goBack() {
      this.$router.push({ path: '/hospital', query: { packageId: this.packageId } })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100%; background: #f5f6fa; padding-bottom: 80px; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 48px; padding: 0 12px; background: #2f7cf6; color: #fff; }
.nav h1 { font-size: 17px; font-weight: 500; }
.back, .placeholder { width: 24px; font-size: 20px; cursor: pointer; }
.month-bar { display: flex; align-items: center; justify-content: center; gap: 24px; padding: 16px 0; background: #fff; }
.month { font-size: 16px; font-weight: 600; color: #222; }
.arrow { font-size: 18px; color: #2f7cf6; cursor: pointer; padding: 4px 10px; }
.calendar { background: #fff; padding: 0 12px 16px; }
.week-row { display: flex; }
.week { flex: 1; text-align: center; font-size: 12px; color: #999; padding: 8px 0; }
.grid { display: flex; flex-wrap: wrap; }
.cell { width: 14.28%; height: 58px; display: flex; flex-direction: column; align-items: center; justify-content: center; box-sizing: border-box; border-radius: 8px; cursor: pointer; }
.cell.blank { cursor: default; }
.cell .dnum { font-size: 15px; color: #222; }
.cell .dinfo { font-size: 11px; color: #2fa86a; margin-top: 2px; }
.cell.disabled { background: #f7f7f7; cursor: not-allowed; }
.cell.disabled .dnum { color: #ccc; }
.cell.disabled .dinfo { color: #ccc; }
.cell.selected { background: #2f7cf6; }
.cell.selected .dnum, .cell.selected .dinfo { color: #fff; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; padding: 10px 16px; box-shadow: 0 -1px 6px rgba(0,0,0,.06); }
.chosen { font-size: 13px; color: #666; margin-bottom: 8px; }
.btn { width: 100%; height: 42px; border: none; border-radius: 21px; background: #2f7cf6; color: #fff; font-size: 16px; }
.tip { text-align: center; color: #999; padding: 30px 0; }
</style>
