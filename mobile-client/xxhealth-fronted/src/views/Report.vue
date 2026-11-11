<template>
  <div class="page">
    <header class="nav">
      <span class="back" @click="go('/reportList')"><i class="fa fa-angle-left"></i></span>
      <h1>体检报告详情</h1>
      <span class="placeholder"></span>
    </header>

    <div v-if="loading" class="tip">正在读取医生报告…</div>
    <div v-else-if="!report" class="tip">报告不存在或尚未发布</div>

    <template v-else>
      <section class="card head">
        <div class="score-ring"><strong>{{ report.score }}</strong><span>健康评分</span></div>
        <div class="head-main">
          <div class="name">{{ report.packageName }}</div>
          <div class="sub">{{ report.reportNo }}</div>
          <div class="sub">{{ report.examDate }} · {{ report.doctorName }}</div>
        </div>
      </section>

      <section v-if="report.alerts && report.alerts.length" class="card alerts">
        <h2><i class="fa fa-exclamation-triangle"></i> 健康提醒</h2>
        <div v-for="(alert, index) in report.alerts" :key="index" class="alert-item">
          <strong>{{ alert.type }}</strong><span>{{ alert.indicator }}</span>
        </div>
      </section>

      <section class="card">
        <h2>医生结论</h2>
        <p class="paragraph">{{ report.conclusion || '暂无结论' }}</p>
      </section>

      <section class="card">
        <h2>健康建议</h2>
        <p class="paragraph advice">{{ report.advice || '暂无健康建议' }}</p>
      </section>

      <section class="card">
        <h2>检查结果</h2>
        <div v-for="group in groupedItems" :key="group.name" class="group">
          <h3>{{ group.name }}</h3>
          <div v-for="(item, index) in group.items" :key="index" class="detail" :class="{ error: item.abnormal }">
            <div class="detail-main">
              <span class="d-name">{{ item.name }}</span>
              <span v-if="item.abnormal" class="err-tag">异常</span>
              <span class="d-value">{{ item.value }}<em v-if="item.unit"> {{ item.unit }}</em></span>
            </div>
            <div class="d-range">参考范围：{{ item.range || rangeText(item) || '—' }}</div>
          </div>
        </div>
        <div v-if="!report.items || report.items.length === 0" class="empty">暂无检查项</div>
      </section>
    </template>
  </div>
</template>

<script>
import { mobileApi } from '@/api/mobile'

export default {
  name: 'Report',
  data() {
    return { loading: true, report: null }
  },
  computed: {
    groupedItems() {
      const groups = {}
      ;(this.report && this.report.items || []).forEach(item => {
        const name = item.group || '其他检查'
        if (!groups[name]) groups[name] = []
        groups[name].push(item)
      })
      return Object.keys(groups).map(name => ({ name, items: groups[name] }))
    }
  },
  created() {
    this.load()
  },
  methods: {
    async load() {
      const reportid = Number(this.$route.query.reportid)
      if (!reportid) {
        this.loading = false
        return
      }
      try {
        this.report = await mobileApi.report(reportid)
      } catch (e) {
        this.report = null
      } finally {
        this.loading = false
      }
    },
    rangeText(item) {
      if (item.min != null && item.max != null) return item.min + ' - ' + item.max
      return ''
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
.back, .placeholder { width: 30px; font-size: 20px; }
.card { background: #fff; border-radius: 14px; margin: 12px; padding: 15px; box-shadow: 0 2px 10px rgba(31,45,61,.04); }
.head { display: flex; align-items: center; gap: 16px; }
.score-ring { width: 76px; height: 76px; flex: 0 0 76px; border: 6px solid #dff4ec; border-top-color: #10a06a; border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.score-ring strong { color: #10a06a; font-size: 24px; line-height: 1; }
.score-ring span { margin-top: 4px; color: #8a94a6; font-size: 10px; }
.head-main .name { font-size: 18px; font-weight: 600; color: #1f2d3d; }
.sub { color: #8a94a6; font-size: 12px; margin-top: 5px; }
.card h2 { font-size: 15px; color: #1f2d3d; margin-bottom: 10px; }
.alerts h2 { color: #d56a17; }
.alert-item { display: flex; flex-direction: column; gap: 3px; padding: 9px 10px; margin-top: 7px; background: #fff5ea; border-radius: 8px; font-size: 13px; }
.alert-item strong { color: #d56a17; }
.alert-item span { color: #7a6048; }
.paragraph { white-space: pre-line; font-size: 14px; color: #455365; line-height: 1.75; }
.advice { color: #45695d; }
.group { margin-top: 13px; }
.group h3 { margin-bottom: 7px; padding-left: 8px; border-left: 3px solid #10a06a; font-size: 14px; color: #405164; }
.detail { padding: 9px 0; border-bottom: 1px solid #f0f2f5; }
.detail:last-child { border-bottom: 0; }
.detail-main { display: flex; align-items: center; }
.d-name { flex: 1; color: #3d4b5b; font-size: 13px; }
.d-value { color: #1f2d3d; font-size: 14px; font-weight: 600; }
.d-value em { color: #8a94a6; font-size: 11px; font-style: normal; font-weight: 400; }
.d-range { margin-top: 3px; color: #a0a8b5; font-size: 11px; text-align: right; }
.detail.error .d-name, .detail.error .d-value { color: #e5484d; }
.err-tag { margin-right: 8px; padding: 1px 6px; border-radius: 8px; background: #fdecec; color: #e5484d; font-size: 10px; }
.tip, .empty { text-align: center; color: #8a94a6; font-size: 14px; padding: 60px 0; }
</style>
