<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="go('/index')"><i class="fa fa-angle-left"></i></span>
      <h1>预约成功</h1>
      <span class="placeholder"></span>
    </header>

    <div class="result">
      <i class="fa fa-check-circle"></i>
      <h2>预约成功</h2>
      <p class="sub">请按预约时间到院体检</p>
    </div>

    <section class="card" v-if="hospital || setmeal || date">
      <div class="row" v-if="hospital"><span class="label">体检机构</span><span class="value">{{ hospital }}</span></div>
      <div class="row" v-if="setmeal"><span class="label">体检套餐</span><span class="value">{{ setmeal }}</span></div>
      <div class="row" v-if="date"><span class="label">体检日期</span><span class="value">{{ date }}</span></div>
    </section>

    <div class="actions">
      <button class="btn primary" @click="go('/appointmentList')">查看我的预约</button>
      <button class="btn ghost" @click="go('/index')">返回首页</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AppointmentSuccess',
  computed: {
    hospital() {
      return this.$route.query.hospital || ''
    },
    setmeal() {
      return this.$route.query.setmeal || ''
    },
    date() {
      return this.$route.query.date || ''
    }
  },
  methods: {
    go(path) {
      this.$router.push(path)
    }
  }
}
</script>

<style scoped>
.page { min-height: 100%; background: #f5f6fa; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 48px; padding: 0 12px; background: #2f7cf6; color: #fff; }
.nav h1 { font-size: 17px; font-weight: 500; }
.back, .placeholder { width: 24px; font-size: 20px; cursor: pointer; }
.result { text-align: center; padding: 40px 0 20px; }
.result i { font-size: 64px; color: #2fa86a; }
.result h2 { font-size: 20px; color: #222; margin-top: 14px; }
.sub { font-size: 13px; color: #999; margin-top: 8px; }
.card { background: #fff; border-radius: 10px; margin: 12px; padding: 14px; }
.row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.label { color: #888; }
.value { color: #222; max-width: 65%; text-align: right; }
.actions { padding: 20px 16px; display: flex; flex-direction: column; gap: 12px; }
.btn { height: 42px; border-radius: 21px; font-size: 16px; border: none; }
.btn.primary { background: #2f7cf6; color: #fff; }
.btn.ghost { background: #fff; color: #2f7cf6; border: 1px solid #2f7cf6; }
</style>
