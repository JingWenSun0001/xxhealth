<template>
  <div class="app">
    <div class="topbar">
      <button class="back" @click="$router.push('/index')"><i class="fa fa-angle-left"></i></button>
      <h1>选择体检套餐</h1>
      <span class="step">1 / 4</span>
    </div>

    <div class="intro">
      <strong>医生端在售套餐</strong>
      <span>套餐内容、适用人群和价格与医生工作台实时一致</span>
    </div>

    <div class="filters">
      <button v-for="item in categories" :key="item" :class="{ active: category === item }" @click="category = item">{{ item }}</button>
    </div>

    <div v-if="loading" class="empty">正在同步医生端套餐…</div>
    <div v-else-if="!filteredPackages.length" class="empty">暂无可预约套餐</div>

    <article v-for="item in filteredPackages" :key="item.packageId" class="card package-card" @click="openDetail(item)">
      <div class="package-top">
        <span class="picture" :class="item.picture"><i :class="icon(item)"></i></span>
        <div class="package-main">
          <div class="package-title"><h3>{{ item.name }}</h3><span class="gender">{{ item.gender }}</span></div>
          <p>{{ item.audience }}</p>
        </div>
      </div>
      <div class="features"><span v-for="feature in item.features" :key="feature">{{ feature }}</span></div>
      <ul class="preview-items"><li v-for="content in item.items.slice(0, 2)" :key="content"><i class="fa fa-check"></i>{{ content }}</li></ul>
      <div class="package-bottom">
        <div><strong>¥{{ item.price }}</strong><del v-if="item.originalPrice > item.price">¥{{ item.originalPrice }}</del></div>
        <button @click.stop="choose(item)">选择套餐</button>
      </div>
    </article>

    <div v-if="detail" class="modal-mask" @click.self="detail = null">
      <div class="modal">
        <div class="modal-head"><h3>{{ detail.name }}</h3><button @click="detail = null"><i class="fa fa-times"></i></button></div>
        <p class="audience">{{ detail.audience }} · {{ detail.gender === '全部' ? '男女均适用' : detail.gender + '性适用' }}</p>
        <div class="modal-price"><strong>¥{{ detail.price }}</strong><del v-if="detail.originalPrice > detail.price">¥{{ detail.originalPrice }}</del></div>
        <h4>套餐包含</h4>
        <ul class="all-items"><li v-for="content in detail.items" :key="content"><i class="fa fa-check-circle"></i>{{ content }}</li></ul>
        <button class="book" @click="choose(detail)">选择此套餐</button>
      </div>
    </div>
  </div>
</template>

<script>
import { mobileApi } from '@/api/mobile'

export default {
  name: 'Appointment',
  data() {
    return { packages: [], loading: true, category: '全部', detail: null }
  },
  computed: {
    categories() {
      return ['全部', ...new Set(this.packages.map(item => item.category))]
    },
    filteredPackages() {
      return this.category === '全部' ? this.packages : this.packages.filter(item => item.category === this.category)
    }
  },
  async created() {
    try {
      this.packages = await mobileApi.packages()
      const selected = Number(this.$route.query.packageId)
      if (selected) this.detail = this.packages.find(item => Number(item.packageId) === selected) || null
    } catch (e) {
      this.packages = []
    } finally {
      this.loading = false
    }
  },
  methods: {
    icon(item) {
      if (item.gender === '女') return 'fa fa-female'
      if (item.gender === '男') return 'fa fa-male'
      return 'fa fa-heartbeat'
    },
    openDetail(item) {
      this.detail = item
    },
    choose(item) {
      this.$router.push({ path: '/hospital', query: { packageId: item.packageId } })
    }
  }
}
</script>

<style scoped>
.intro { margin: 14px 16px 8px; padding: 14px; border-radius: 13px; background: linear-gradient(135deg,#e5f7ef,#f3fbf8); }
.intro strong, .intro span { display: block; }
.intro strong { color: #0b8557; font-size: 15px; }
.intro span { margin-top: 5px; color: #668076; font-size: 12px; line-height: 1.5; }
.filters { display: flex; gap: 8px; padding: 4px 16px 2px; overflow-x: auto; }
.filters button { flex: 0 0 auto; height: 32px; padding: 0 13px; border: 0; border-radius: 16px; background: #fff; color: #748092; font-size: 12px; }
.filters button.active { background: #10a06a; color: #fff; }
.package-card { cursor: pointer; }
.package-top { display: flex; gap: 12px; align-items: center; }
.picture { width: 48px; height: 48px; flex: 0 0 48px; display: flex; align-items: center; justify-content: center; border-radius: 13px; background: #e6f6ef; color: #10a06a; font-size: 21px; }
.picture.women { background: #fff0f5; color: #e95c94; }
.picture.men { background: #edf4ff; color: #397bea; }
.package-main { flex: 1; min-width: 0; }
.package-title { display: flex; align-items: center; gap: 7px; }
.package-title h3 { font-size: 16px; }
.gender { padding: 1px 7px; border-radius: 10px; background: #f0f3f6; color: #6f7b8c; font-size: 10px; }
.package-main p { margin-top: 5px; color: #8a94a6; font-size: 12px; }
.features { display: flex; gap: 6px; margin-top: 11px; }
.features span { padding: 2px 8px; border-radius: 10px; background: #fff4e8; color: #d97718; font-size: 10px; }
.preview-items { margin-top: 10px; }
.preview-items li { margin-top: 5px; color: #5e6b7b; font-size: 12px; }
.preview-items i { margin-right: 7px; color: #10a06a; }
.package-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 12px; padding-top: 11px; border-top: 1px solid #eef1f5; }
.package-bottom strong, .modal-price strong { color: #f0672c; font-size: 20px; }
del { margin-left: 6px; color: #aab1bb; font-size: 11px; }
.package-bottom button, .book { border: 0; border-radius: 18px; background: #10a06a; color: #fff; }
.package-bottom button { height: 34px; padding: 0 16px; }
.modal-mask { position: fixed; inset: 0; z-index: 40; display: flex; align-items: flex-end; justify-content: center; background: rgba(15,23,42,.45); }
.modal { width: 100%; max-width: 480px; max-height: 82vh; overflow-y: auto; padding: 18px; border-radius: 18px 18px 0 0; background: #fff; }
.modal-head { display: flex; justify-content: space-between; align-items: center; }
.modal-head h3 { font-size: 18px; }
.modal-head button { border: 0; background: none; color: #8a94a6; font-size: 18px; }
.audience { margin-top: 6px; color: #7a8595; font-size: 12px; }
.modal-price { margin: 12px 0 16px; }
.modal h4 { font-size: 14px; }
.all-items { margin: 8px 0 18px; }
.all-items li { padding: 10px 0; border-bottom: 1px solid #f0f2f5; color: #4f5d6d; font-size: 13px; }
.all-items i { margin-right: 8px; color: #10a06a; }
.book { width: 100%; height: 44px; font-size: 15px; }
</style>
