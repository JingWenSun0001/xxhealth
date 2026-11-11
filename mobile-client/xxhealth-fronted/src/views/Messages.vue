<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="$router.back()"><i class="fa fa-angle-left"></i></span>
      <h1>健康消息</h1>
      <button class="refresh" @click="load"><i class="fa fa-refresh"></i></button>
    </header>
    <div v-if="loading" class="tip">正在同步医生端消息…</div>
    <div v-else-if="list.length === 0" class="tip">暂无消息</div>
    <div v-else class="list">
      <button v-for="(item, index) in list" :key="index" class="message" @click="open(item)">
        <span class="icon" :class="item.type"><i :class="item.type === 'report' ? 'fa fa-file-text-o' : 'fa fa-calendar-check-o'"></i></span>
        <span class="content">
          <strong>{{ item.title }}</strong>
          <span>{{ item.content }}</span>
          <small>{{ formatTime(item.time) }}</small>
        </span>
        <i class="fa fa-angle-right arrow"></i>
      </button>
    </div>
  </div>
</template>

<script>
import { mobileApi } from '@/api/mobile'

export default {
  name: 'Messages',
  data() {
    return { loading: true, list: [] }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      this.loading = true
      try {
        this.list = await mobileApi.messages()
      } catch (e) {
        this.list = []
      } finally {
        this.loading = false
      }
    },
    open(item) {
      if (item.type === 'report') this.$router.push({ path: '/report', query: { reportid: item.reportId } })
      else this.$router.push('/appointmentList')
    },
    formatTime(value) {
      return value ? String(value).replace('.0', '') : ''
    }
  }
}
</script>

<style scoped>
.page { min-height: 100vh; max-width: 480px; margin: 0 auto; background: #f5f7fa; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 52px; padding: 0 12px; background: #10a06a; color: #fff; }
.nav h1 { font-size: 17px; font-weight: 600; }
.back, .refresh { width: 30px; border: 0; background: none; color: #fff; font-size: 20px; }
.refresh { font-size: 15px; }
.list { padding: 12px; }
.message { width: 100%; display: flex; align-items: center; gap: 12px; margin-bottom: 10px; padding: 14px; border: 0; border-radius: 13px; background: #fff; text-align: left; box-shadow: 0 2px 8px rgba(31,45,61,.04); }
.icon { width: 42px; height: 42px; flex: 0 0 42px; display: flex; align-items: center; justify-content: center; border-radius: 50%; color: #fff; font-size: 17px; }
.icon.appointment { background: #2f7cf6; }
.icon.report { background: #10a06a; }
.content { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.content strong { color: #263445; font-size: 14px; }
.content > span { overflow: hidden; color: #687587; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; }
.content small { color: #a0a8b5; font-size: 10px; }
.arrow { color: #c3cad4; }
.tip { text-align: center; color: #8a94a6; font-size: 14px; padding: 70px 0; }
</style>
