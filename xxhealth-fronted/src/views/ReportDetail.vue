<template>
  <div class="detail-page" v-loading="loading">
    <!-- 患者头部 -->
    <section class="card report-head">
      <div class="head-patient">
        <div class="patient-avatar" :class="'sex-' + (patient.sex || 1)">
          {{ (patient.name || '患').slice(0, 1) }}
        </div>
        <div>
          <div class="patient-name">
            <strong>{{ patient.name || '未知患者' }}</strong>
            <span>{{ patient.sex === 1 ? '男' : patient.sex === 0 ? '女' : '—' }}</span>
            <span v-if="patient.age">{{ patient.age }} 岁</span>
          </div>
          <p class="patient-line">
            <span>ID：H2025{{ orderid }}0001</span>
            <span v-if="patient.phone">手机号：{{ patient.phone }}</span>
          </p>
          <div class="patient-tags">
            <span class="tag tag-info">预约编号 #{{ orderid }}</span>
            <span class="tag" :class="reportTag.cls">{{ reportTag.text }}</span>
          </div>
        </div>
      </div>

      <div class="head-metrics">
        <div class="metric">
          <div class="metric-icon blue"><el-icon><Calendar /></el-icon></div>
          <div>
            <span>体检日期</span>
            <strong>{{ patient.examDate || '—' }}</strong>
          </div>
        </div>
        <div class="metric">
          <div class="metric-icon purple"><el-icon><Files /></el-icon></div>
          <div>
            <span>体检套餐</span>
            <strong>{{ patient.setmeal || '—' }}</strong>
          </div>
        </div>
        <div class="metric">
          <div class="metric-icon green"><el-icon><CircleCheckFilled /></el-icon></div>
          <div>
            <span>报告状态</span>
            <strong class="green">{{ reportTag.text }}</strong>
          </div>
        </div>
      </div>

      <div class="head-actions no-print">
        <el-button @click="handleIncomplete"><el-icon><Download /></el-icon>&nbsp;下载报告</el-button>
        <el-button @click="printReport"><el-icon><Printer /></el-icon>&nbsp;打印报告</el-button>
        <el-button @click="handleIncomplete"><el-icon><Share /></el-icon>&nbsp;分享报告</el-button>
        <el-button type="primary" plain @click="goEdit"><el-icon><EditPen /></el-icon>&nbsp;返回编辑</el-button>
      </div>
    </section>

    <section v-if="!reports.length" class="card empty-card" v-loading="loading">
      <el-empty v-if="!loading" description="该预约还没有体检报告" :image-size="120">
        <el-button type="primary" @click="goEdit">去创建 / 编辑报告</el-button>
      </el-empty>
    </section>

    <section v-else class="detail-grid">
      <!-- 左侧报告列表 -->
      <aside class="card history-card no-print">
        <div class="history-head">报告列表</div>
        <ul class="history-list">
          <li
            v-for="(item, index) in history"
            :key="index"
            class="history-item"
            :class="{ active: item.current }"
            @click="handleHistory(item)"
          >
            <div class="history-icon"><el-icon><Document /></el-icon></div>
            <div class="history-main">
              <p>{{ formatDate(item.date) }}</p>
              <div class="history-sub">
                <small>{{ item.setmeal }}</small>
                <span class="tag" :class="item.current ? 'tag-normal' : 'tag-muted'">{{ item.status }}</span>
              </div>
            </div>
          </li>
        </ul>
        <p class="history-tip">TODO：历史报告待 orders 接口接入</p>
      </aside>

      <!-- 右侧详情 -->
      <div class="card content-card">
        <div class="tabs">
          <span
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >{{ tab.label }}</span>
        </div>

        <!-- 报告总览 -->
        <div v-if="activeTab === 'overview'" class="tab-body">
          <div class="overview-top">
            <div class="summary-box">
              <div class="box-title"><el-icon><TrendCharts /></el-icon>健康总评</div>
              <div class="summary-content">
                <HealthRing :score="score" :size="150" :color="scoreInfo.color" label="健康评分" />
                <div class="summary-text">
                  <p class="summary-status" :style="{ color: scoreInfo.color }">
                    <el-icon><CircleCheckFilled /></el-icon>{{ scoreInfo.label }}
                  </p>
                  <p class="summary-desc">{{ summaryDesc }}</p>
                  <el-button text type="primary" @click="activeTab = 'assess'">查看评估详情 ›</el-button>
                </div>
              </div>
            </div>

            <div class="abnormal-box">
              <div class="box-title"><el-icon><WarningFilled /></el-icon>主要异常项</div>
              <ul v-if="abnormalDetails.length" class="abnormal-list">
                <li v-for="item in abnormalDetails" :key="item.cidrid">
                  <div class="abnormal-left">
                    <p><el-icon><WarningFilled /></el-icon>{{ item.name }}</p>
                    <span>{{ resultText(item) }}（参考：{{ formatRange(item) }}）</span>
                  </div>
                  <span class="tag tag-error">{{ abnormalDirection(item) }}</span>
                </li>
              </ul>
              <p v-else class="abnormal-empty">本次体检未发现异常指标</p>
            </div>
          </div>

          <div class="section-title"><el-icon><Files /></el-icon>体检结果概览</div>
          <div class="item-grid">
            <div v-for="item in reports" :key="item.cirid" class="item-card">
              <div class="item-card-head">
                <span class="item-card-name">{{ item.ciname }}</span>
                <span class="tag" :class="itemTag(item).cls">{{ itemTag(item).text }}</span>
              </div>
              <ul class="item-rows">
                <li v-for="d in item.cidetailedreports.slice(0, 5)" :key="d.cidrid">
                  <span class="row-name">{{ d.name }}</span>
                  <span class="row-value" :class="{ danger: d.iserror === 1 }">{{ resultText(d) }}</span>
                </li>
              </ul>
              <p v-if="!item.cidetailedreports.length" class="item-empty">暂无明细</p>
            </div>
          </div>

          <div class="advice-box">
            <div class="box-title"><el-icon><Opportunity /></el-icon>医生建议</div>
            <ol class="advice-list">
              <li v-for="(text, index) in adviceList" :key="index">
                <span class="advice-index">{{ index + 1 }}</span>{{ text }}
              </li>
            </ol>
            <div class="advice-sign">
              <span>报告医生：{{ doctor.realname || '医生' }}</span>
              <span>报告时间：{{ today }} 10:30</span>
            </div>
          </div>
        </div>

        <!-- 体检结果 -->
        <div v-else-if="activeTab === 'result'" class="tab-body">
          <div class="section-title"><el-icon><List /></el-icon>全部检查结果</div>
          <el-table :data="flatDetails" row-key="cidrid" class="result-table">
            <el-table-column label="检查项" width="140">
              <template #default="{ row }">{{ row.ciname }}</template>
            </el-table-column>
            <el-table-column label="项目名称" min-width="150">
              <template #default="{ row }">{{ row.name }}</template>
            </el-table-column>
            <el-table-column label="结果" min-width="130">
              <template #default="{ row }">{{ resultText(row) }}</template>
            </el-table-column>
            <el-table-column label="参考范围" min-width="150">
              <template #default="{ row }">{{ formatRange(row) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <span class="tag" :class="detailTag(row).cls">{{ detailTag(row).text }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 健康评估 -->
        <div v-else-if="activeTab === 'assess'" class="tab-body">
          <div class="section-title"><el-icon><DataAnalysis /></el-icon>健康评估</div>
          <div class="assess-grid">
            <div class="assess-ring">
              <HealthRing :score="score" :size="180" :color="scoreInfo.color" label="综合评分" />
            </div>
            <div class="assess-stats">
              <div class="assess-stat">
                <span>检查项</span><strong>{{ stats.itemCount }}</strong>
              </div>
              <div class="assess-stat">
                <span>明细项</span><strong>{{ stats.detailCount }}</strong>
              </div>
              <div class="assess-stat">
                <span>正常</span><strong class="green">{{ stats.normalCount }}</strong>
              </div>
              <div class="assess-stat">
                <span>异常</span><strong class="red">{{ stats.abnormalCount }}</strong>
              </div>
            </div>
          </div>

          <p class="assess-desc">{{ summaryDesc }}</p>

          <div v-if="abnormalDetails.length" class="assess-abnormal">
            <div class="section-title small">异常指标明细</div>
            <ul>
              <li v-for="item in abnormalDetails" :key="item.cidrid">
                <span>{{ item.name }}</span>
                <strong>{{ resultText(item) }}</strong>
                <em>参考 {{ formatRange(item) }}</em>
              </li>
            </ul>
          </div>
        </div>

        <!-- 建议与指导 -->
        <div v-else class="tab-body">
          <div class="section-title"><el-icon><Opportunity /></el-icon>建议与指导</div>
          <ol class="guide-list">
            <li v-for="(text, index) in adviceList" :key="index">
              <span class="guide-index">{{ index + 1 }}</span>
              <div>
                <p>{{ text }}</p>
              </div>
            </li>
          </ol>
          <div class="guide-sign">
            <span>报告医生：{{ doctor.realname || '医生' }}</span>
            <span>报告时间：{{ today }} 10:30</span>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import HealthRing from '@/components/HealthRing.vue'
import { listCiReport } from '@/api/ciReport'
import {
  abnormalOf,
  buildAdvice,
  formatRange,
  hasValue,
  resultText,
  scoreMeta,
  scoreOf,
  statusOf,
  summarize
} from '@/utils/report'
import { findMockPatient, mockReportHistory } from '@/mock/patients'
import { getSessionStorage } from '@/common'

const route = useRoute()
const router = useRouter()
const doctor = getSessionStorage('doctor') || {}

const orderid = route.params.orderid
const reports = ref([])
const loading = ref(false)
const activeTab = ref('overview')

const tabs = [
  { key: 'overview', label: '报告总览' },
  { key: 'result', label: '体检结果' },
  { key: 'assess', label: '健康评估' },
  { key: 'guide', label: '建议与指导' }
]

// TODO: 患者信息来自 orders/users 接口（队友负责），当前用示例数据 + 路由参数兜底
const patient = computed(() => {
  const mock = findMockPatient(orderid)
  if (mock) return mock
  return {
    name: route.query.name ? String(route.query.name) : '',
    sex: route.query.sex !== undefined ? Number(route.query.sex) : 1,
    age: route.query.age ? Number(route.query.age) : '',
    phone: '',
    setmeal: '',
    examDate: ''
  }
})

const stats = computed(() => summarize(reports.value))
const score = computed(() => scoreOf(stats.value))
const scoreInfo = computed(() => scoreMeta(score.value, stats.value.detailCount > 0))

const reportTag = computed(() => {
  if (!reports.value.length) return { text: '未生成', cls: 'tag-muted' }
  if (stats.value.enteredCount === stats.value.detailCount) return { text: '已生成', cls: 'tag-normal' }
  return { text: '录入中', cls: 'tag-warn' }
})

const summaryDesc = computed(() => {
  if (!stats.value.detailCount) return '暂无检查结果，请先录入体检数据。'
  if (score.value >= 90) {
    return '各项指标基本正常，建议保持健康的生活方式、合理饮食并定期体检。'
  }
  if (score.value >= 70) {
    return `本次体检发现 ${stats.value.abnormalCount} 项异常指标，建议关注并遵医嘱复查。`
  }
  return `本次体检异常指标较多（${stats.value.abnormalCount} 项），建议尽快到相关科室进一步检查。`
})

const abnormalDetails = computed(() => {
  const list = []
  reports.value.forEach((item) => {
    ;(item.cidetailedreports || []).forEach((d) => {
      if (d.iserror === 1) list.push(d)
    })
  })
  return list
})

const flatDetails = computed(() => {
  const list = []
  reports.value.forEach((item) => {
    ;(item.cidetailedreports || []).forEach((d) => {
      list.push({ ...d, ciname: item.ciname })
    })
  })
  return list
})

// 优先使用编辑页保存的本地建议（TODO: overallResult 接口接入后改为服务端）
const adviceList = computed(() => {
  try {
    const raw = localStorage.getItem(`xxhealth:advice:${orderid}`)
    if (raw) {
      const data = JSON.parse(raw)
      const list = []
      if (data.opinion) list.push(data.opinion)
      if (Array.isArray(data.suggestions)) data.suggestions.forEach((s) => list.push(`建议：${s}`))
      if (list.length) return list
    }
  } catch (e) {
    // ignore
  }
  return buildAdvice(reports.value)
})

const history = computed(() => mockReportHistory(patient.value))

const today = new Date().toLocaleDateString('zh-CN')

function itemTag(item) {
  const s = statusOf(item)
  if (item.cidetailedreports.some((d) => d.iserror === 1)) return { text: `异常 ${abnormalOf(item)} 项`, cls: 'tag-error' }
  return { text: s.text === '已录入' ? '正常' : s.text, cls: s.cls }
}

function detailTag(d) {
  if (!hasValue(d)) return { text: '未录入', cls: 'tag-muted' }
  return d.iserror === 1 ? { text: '异常', cls: 'tag-error' } : { text: '正常', cls: 'tag-normal' }
}

function abnormalDirection(d) {
  const v = Number(d.value)
  if (!Number.isNaN(v) && d.maxrange !== null && d.maxrange !== undefined && v > Number(d.maxrange)) return '偏高'
  if (!Number.isNaN(v) && d.minrange !== null && d.minrange !== undefined && v < Number(d.minrange)) return '偏低'
  return '异常'
}

function formatDate(date) {
  if (!date) return '—'
  const parts = String(date).split('-')
  if (parts.length !== 3) return String(date)
  return `${parts[0]}年${parts[1]}月${parts[2]}日`
}

async function loadReport() {
  loading.value = true
  try {
    reports.value = (await listCiReport(Number(orderid))) || []
  } catch (e) {
    reports.value = []
  } finally {
    loading.value = false
  }
}

function printReport() {
  window.print()
}

function handleIncomplete() {
  ElMessage.info('该功能待后端报告文件/分享接口接入')
}

function handleHistory(item) {
  if (item.current) return
  ElMessage.info('历史报告数据待 orders 接口接入后开放')
}

function goEdit() {
  router.push(`/report/edit/${orderid}`)
}

onMounted(loadReport)
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 头部 */
.report-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px;
}

.head-patient {
  display: flex;
  align-items: center;
  gap: 16px;
}

.patient-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #5aa0ff, #2f6bff);
  flex-shrink: 0;
}

.patient-avatar.sex-0 {
  background: linear-gradient(135deg, #ff9dbb, #f4628f);
}

.patient-name {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.patient-name strong {
  font-size: 19px;
  font-weight: 800;
  color: var(--text-1);
}

.patient-name span {
  font-size: 13px;
  color: var(--text-2);
}

.patient-line {
  margin-top: 6px;
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: var(--text-3);
}

.patient-tags {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.head-metrics {
  display: flex;
  gap: 22px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 14px;
  background: #f8fafe;
  border: 1px solid var(--line);
}

.metric-icon {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.metric-icon.blue { color: var(--brand); background: var(--brand-light); }
.metric-icon.purple { color: var(--purple); background: var(--purple-bg); }
.metric-icon.green { color: var(--green); background: var(--green-bg); }

.metric span {
  display: block;
  font-size: 11px;
  color: var(--text-3);
}

.metric strong {
  display: block;
  margin-top: 3px;
  font-size: 13px;
  color: var(--text-1);
}

.metric strong.green {
  color: var(--green);
}

.head-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.head-actions .el-button {
  margin-left: 0;
}

.empty-card {
  padding: 40px 0 50px;
  min-height: 320px;
}

/* 主体 */
.detail-grid {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.history-card {
  padding: 16px 12px;
  position: sticky;
  top: 0;
}

.history-head {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  padding: 0 8px 12px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  border: 1px solid transparent;
}

.history-item:hover {
  background: #f7faff;
}

.history-item.active {
  background: var(--brand-light);
  border-color: #cfe0ff;
}

.history-icon {
  color: var(--brand);
  font-size: 18px;
}

.history-main {
  flex: 1;
  min-width: 0;
}

.history-main p {
  font-size: 12.5px;
  color: var(--text-1);
  font-weight: 600;
  white-space: nowrap;
}

.history-sub {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  margin-top: 5px;
}

.history-sub small {
  font-size: 11px;
  color: var(--text-3);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-sub .tag {
  height: 20px;
  padding: 0 8px;
  font-size: 11px;
}

.history-tip {
  margin-top: 12px;
  padding: 0 8px;
  font-size: 11px;
  color: var(--text-3);
}

.content-card {
  padding: 0 0 24px;
  overflow: hidden;
}

.tabs {
  display: flex;
  gap: 28px;
  padding: 0 24px;
  border-bottom: 1px solid var(--line);
}

.tab-item {
  position: relative;
  padding: 16px 2px 14px;
  font-size: 14px;
  color: var(--text-2);
  cursor: pointer;
}

.tab-item.active {
  color: var(--brand);
  font-weight: 700;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -1px;
  height: 2.5px;
  border-radius: 2px;
  background: var(--brand);
}

.tab-body {
  padding: 20px 24px 0;
}

/* 总览 */
.overview-top {
  display: grid;
  grid-template-columns: 1.25fr 0.75fr;
  gap: 16px;
}

.summary-box,
.abnormal-box {
  border-radius: 16px;
  border: 1px solid var(--line);
  background: #fbfcff;
  padding: 16px 18px;
}

.abnormal-box {
  background: #fffafa;
  border-color: #ffe1dd;
}

.box-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 12px;
}

.summary-content {
  display: flex;
  align-items: center;
  gap: 22px;
}

.summary-text {
  flex: 1;
}

.summary-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
}

.summary-desc {
  margin-top: 8px;
  font-size: 12.5px;
  color: var(--text-2);
  line-height: 1.8;
}

.abnormal-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.abnormal-list li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ffe1dd;
}

.abnormal-list li:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.abnormal-left p {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}

.abnormal-left p .el-icon {
  color: var(--red);
}

.abnormal-left span {
  font-size: 11.5px;
  color: var(--text-3);
}

.abnormal-empty {
  font-size: 12.5px;
  color: var(--green);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 22px 0 12px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
}

.section-title.small {
  margin-top: 18px;
  font-size: 13px;
}

.item-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.item-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px 16px;
  background: #fff;
}

.item-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.item-card-name {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--text-1);
}

.item-rows li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 0;
  font-size: 12.5px;
}

.row-name {
  color: var(--text-2);
}

.row-value {
  color: var(--text-1);
  font-weight: 600;
}

.row-value.danger {
  color: var(--red);
}

.item-empty {
  font-size: 12px;
  color: var(--text-3);
}

.advice-box {
  margin-top: 20px;
  padding: 16px 18px;
  border-radius: 16px;
  background: #f5f9ff;
  border: 1px solid #e3edff;
}

.advice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.advice-list li {
  display: flex;
  gap: 10px;
  font-size: 12.5px;
  color: var(--text-2);
  line-height: 1.7;
}

.advice-index,
.guide-index {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #fff;
  color: var(--brand);
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 1px solid #d6e4ff;
}

.advice-sign,
.guide-sign {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  font-size: 12px;
  color: var(--text-3);
}

/* 体检结果 */
.result-table {
  width: 100%;
}

/* 健康评估 */
.assess-grid {
  display: flex;
  align-items: center;
  gap: 40px;
}

.assess-ring {
  padding: 10px 20px;
}

.assess-stats {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.assess-stat {
  padding: 14px 18px;
  border-radius: 14px;
  background: #f8fafe;
  border: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.assess-stat span {
  font-size: 12px;
  color: var(--text-3);
}

.assess-stat strong {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-1);
}

.assess-desc {
  margin-top: 20px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafe;
  font-size: 12.5px;
  color: var(--text-2);
  line-height: 1.8;
}

.assess-abnormal ul {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.assess-abnormal li {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #fff8f7;
  border: 1px solid #ffe1dd;
  font-size: 12.5px;
}

.assess-abnormal li span {
  flex: 1;
  color: var(--text-1);
  font-weight: 600;
}

.assess-abnormal li strong {
  color: var(--red);
}

.assess-abnormal li em {
  font-style: normal;
  color: var(--text-3);
}

/* 建议 */
.guide-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guide-list li {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafe;
  border: 1px solid var(--line);
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.7;
}

.green {
  color: var(--green);
}

.red {
  color: var(--red);
}
</style>
