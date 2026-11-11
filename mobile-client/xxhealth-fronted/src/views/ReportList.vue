<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="go('/index')"><i class="fa fa-angle-left"></i></span>
      <h1>体检报告</h1>
      <button class="refresh" @click="load"><i class="fa fa-refresh"></i></button>
    </header>

    <div class="sync-tip"><i class="fa fa-shield"></i> 仅展示医生审核并正式发布的报告</div>
    <div v-if="loading" class="tip">正在同步医生端报告…</div>
    <div v-else-if="list.length === 0" class="tip">暂无已发布的体检报告</div>

    <ul v-else class="list">
      <li v-for="item in list" :key="item.reportId" class="card" @click="openReport(item)">
        <div class="row">
          <span class="name">{{ item.packageName }}</span>
          <span class="score">{{ item.score }}<small>分</small></span>
        </div>
        <div class="report-no">{{ item.reportNo }}</div>
        <div class="sub"><i class="fa fa-calendar"></i>体检日期：{{ item.examDate }}</div>
        <div class="sub"><i class="fa fa-user-md"></i>报告医生：{{ item.doctorName }}</div>
        <div class="conclusion">{{ item.conclusion }}</div>
        <div class="row bottom">
          <span v-if="Number(item.unresolvedAlerts)" class="risk"><i class="fa fa-exclamation-circle"></i> {{ item.unresolvedAlerts }} 项健康提醒</span>
          <span v-else class="normal"><i class="fa fa-check-circle"></i> 暂无未处理提醒</span>
          <span class="link">查看详情 <i class="fa fa-angle-right"></i></span>
        </div>
      </li>
    </ul>
  </div>
</template>

<script>
import { mobileApi } from '@/api/mobile'
import { getSessionStorage } from '@/common'

export default {
  name: 'ReportList',
  data() {
    return { list: [], loading: true }
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
        this.list = await mobileApi.reports()
      } catch (e) {
        this.list = []
        window.alert(e.message || '同步医生端报告失败')
      } finally {
        this.loading = false
      }
    },
    openReport(item) {
      this.$router.push({ path: '/report', query: { reportid: item.reportId } })
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
.back, .refresh { width: 30px; border: 0; background: none; color: #fff; font-size: 20px; }
.refresh { font-size: 15px; }
.sync-tip { margin: 12px 12px 0; padding: 9px 12px; background: #e7f7f0; border-radius: 10px; color: #14825c; font-size: 12px; }
.list { padding: 12px; }
.card { background: #fff; border-radius: 14px; padding: 15px; margin-bottom: 12px; box-shadow: 0 2px 10px rgba(31,45,61,.05); cursor: pointer; }
.row { display: flex; align-items: center; justify-content: space-between; }
.name { font-size: 16px; font-weight: 600; color: #1f2d3d; }
.score { color: #10a06a; font-size: 24px; font-weight: 700; }
.score small { font-size: 12px; font-weight: 400; }
.report-no { color: #a0a8b5; font-size: 11px; margin: 4px 0 8px; }
.sub { font-size: 13px; color: #6f7b8c; margin-top: 6px; }
.sub i { width: 18px; color: #10a06a; }
.conclusion { margin-top: 10px; padding: 10px; border-radius: 8px; background: #f8fafb; color: #4f5d6d; font-size: 13px; line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.bottom { margin-top: 11px; padding-top: 10px; border-top: 1px solid #eef1f5; font-size: 12px; }
.risk { color: #e5484d; }
.normal { color: #10a06a; }
.link { color: #10a06a; font-size: 13px; }
.tip { text-align: center; color: #8a94a6; font-size: 14px; padding: 70px 0; }
</style>
