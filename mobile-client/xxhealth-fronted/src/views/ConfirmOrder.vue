<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="goBack"><i class="fa fa-angle-left"></i></span>
      <h1>确认预约</h1>
      <span class="placeholder"></span>
    </header>

    <div v-if="loading" class="tip">加载中...</div>

    <template v-else>
      <section class="card">
        <h2>体检信息</h2>
        <div class="row"><span class="label">体检机构</span><span class="value">{{ hospital.name || '-' }}</span></div>
        <div class="row"><span class="label">机构地址</span><span class="value">{{ hospital.address || '-' }}</span></div>
        <div class="row"><span class="label">体检套餐</span><span class="value">{{ packageInfo.name || '-' }}</span></div>
        <div class="row"><span class="label">适用人群</span><span class="value">{{ packageInfo.audience || '-' }} · {{ packageInfo.gender || '全部' }}</span></div>
        <div class="row"><span class="label">体检日期</span><span class="value">{{ date }}</span></div>
      </section>

      <section class="card">
        <div class="row total"><span class="label">应付金额</span><span class="price">¥{{ packageInfo.price || 0 }}</span></div>
      </section>

      <div class="footer">
        <button class="btn" :disabled="submitting" @click="submit">{{ submitting ? '提交中...' : '提交预约' }}</button>
      </div>
    </template>
  </div>
</template>

<script>
import request from '@/utils/request'
import { mobileApi } from '@/api/mobile'
import { getSessionStorage } from '@/common'

export default {
  name: 'ConfirmOrder',
  data() {
    return {
      hpid: null,
      packageId: null,
      date: '',
      hospital: {},
      packageInfo: {},
      loading: true,
      submitting: false
    }
  },
  created() {
    this.hpid = Number(this.$route.query.hpid) || 0
    this.packageId = Number(this.$route.query.packageId) || 0
    this.date = this.$route.query.date || ''
    this.load()
  },
  methods: {
    async load() {
      try {
        const [h, s] = await Promise.all([
          request.post('/hospital/getHospitalById', { hpid: this.hpid }),
          mobileApi.package(this.packageId)
        ])
        this.hospital = h.data || {}
        this.packageInfo = s || {}
      } catch (e) {
        this.hospital = {}
        this.packageInfo = {}
      } finally {
        this.loading = false
      }
    },
    async submit() {
      const user = getSessionStorage('users')
      if (!user) {
        this.$router.push('/login')
        return
      }
      if (this.submitting) return
      this.submitting = true
      try {
        const res = await mobileApi.book({
          hpid: this.hpid,
          packageid: this.packageId,
          orderdate: this.date
        })
        const rows = res.result
        if (rows === 1) {
          this.$router.push({
            path: '/appointmentSuccess',
            query: {
              hospital: this.hospital.name || '',
              setmeal: this.packageInfo.name || '',
              date: this.date
            }
          })
        } else if (rows === 0) {
          window.alert('您有未完成的预约，不能重复预约')
        } else if (rows === -2) {
          window.alert('预约日期不能早于今天')
        } else {
          window.alert('预约失败，请稍后重试')
        }
      } catch (e) {
        window.alert('预约失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    },
    goBack() {
      this.$router.push({
        path: '/selectDate',
        query: { hpid: this.hpid, packageId: this.packageId }
      })
    }
  }
}
</script>

<style scoped>
.page { min-height: 100%; background: #f5f6fa; padding-bottom: 80px; }
.nav { display: flex; align-items: center; justify-content: space-between; height: 48px; padding: 0 12px; background: #2f7cf6; color: #fff; }
.nav h1 { font-size: 17px; font-weight: 500; }
.back, .placeholder { width: 24px; font-size: 20px; cursor: pointer; }
.card { background: #fff; border-radius: 10px; margin: 12px; padding: 16px; }
.card h2 { font-size: 15px; color: #222; margin-bottom: 10px; }
.row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }
.label { color: #888; }
.value { color: #222; max-width: 65%; text-align: right; }
.total { align-items: center; }
.price { color: #ff6b35; font-size: 20px; font-weight: 600; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; background: #fff; padding: 10px 16px; box-shadow: 0 -1px 6px rgba(0,0,0,.06); }
.btn { width: 100%; height: 42px; border: none; border-radius: 21px; background: #2f7cf6; color: #fff; font-size: 16px; }
.btn:disabled { background: #9bb7e6; }
.tip { text-align: center; color: #999; padding: 60px 0; }
</style>
