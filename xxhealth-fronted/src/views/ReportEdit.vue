<template>
  <div class="edit-page" v-loading="loading">
    <!-- 患者信息条 -->
    <section class="card patient-strip">
      <div class="strip-left">
        <div class="patient-avatar" :class="'sex-' + (patient.sex || 1)">
          {{ (patient.name || '患').slice(0, 1) }}
        </div>
        <div class="strip-info">
          <div class="strip-name">
            <strong>{{ patient.name || '未知患者' }}</strong>
            <span class="tag" :class="patient.sex === 1 ? 'tag-info' : 'tag-purple'">
              {{ patient.sex === 1 ? '男' : patient.sex === 0 ? '女' : '—' }}
            </span>
            <span class="strip-age" v-if="patient.age">{{ patient.age }} 岁</span>
            <span class="tag tag-muted">预约编号 #{{ orderid }}</span>
          </div>
          <p class="strip-meta">
            <span v-if="patient.idcard">身份证：{{ patient.idcard }}</span>
            <span v-if="patient.phone">手机号：{{ patient.phone }}</span>
            <span>体检日期：{{ patient.examDate || '—' }}</span>
          </p>
        </div>
      </div>
      <div class="strip-right">
        <el-button @click="goDetail"><el-icon><View /></el-icon>&nbsp;预览报告</el-button>
        <el-button type="primary" plain @click="switchPatient">
          <el-icon><Switch /></el-icon>&nbsp;切换患者
        </el-button>
      </div>
    </section>

    <div v-if="!reports.length" class="card empty-card" v-loading="loading">
      <el-empty v-if="!loading" description="该预约还没有体检报告模板" :image-size="120">
        <el-button type="primary" :loading="creating" @click="handleCreate">
          <el-icon><Plus /></el-icon>&nbsp;根据套餐创建报告模板
        </el-button>
      </el-empty>
    </div>

    <section v-else class="edit-grid">
      <!-- 左侧分区导航 -->
      <aside class="card side-nav">
        <div
          v-for="nav in navs"
          :key="nav.key"
          class="side-item"
          :class="{ active: activeView === nav.key }"
          @click="activeView = nav.key"
        >
          <el-icon><component :is="nav.icon" /></el-icon>
          <span>{{ nav.label }}</span>
        </div>

        <div class="side-summary">
          <div class="summary-row"><span>检查项</span><strong>{{ stats.itemCount }}</strong></div>
          <div class="summary-row"><span>明细项</span><strong>{{ stats.detailCount }}</strong></div>
          <div class="summary-row"><span>已录入</span><strong>{{ stats.enteredCount }}</strong></div>
          <div class="summary-row"><span>异常项</span><strong class="red">{{ stats.abnormalCount }}</strong></div>
          <el-progress :percentage="stats.progress" :stroke-width="7" :color="progressColor" class="side-progress" />
        </div>
      </aside>

      <!-- 主内容 -->
      <div class="card main-card">
        <!-- 基本信息 -->
        <template v-if="activeView === 'basic'">
          <div class="main-head">
            <h3>基本信息</h3>
            <span class="tag tag-muted">患者信息待 orders/users 接口接入</span>
          </div>
          <div class="basic-grid">
            <div class="basic-item"><span>预约编号</span><strong>#{{ orderid }}</strong></div>
            <div class="basic-item"><span>患者姓名</span><strong>{{ patient.name || '—' }}</strong></div>
            <div class="basic-item"><span>性别</span><strong>{{ patient.sex === 1 ? '男' : patient.sex === 0 ? '女' : '—' }}</strong></div>
            <div class="basic-item"><span>年龄</span><strong>{{ patient.age ? patient.age + ' 岁' : '—' }}</strong></div>
            <div class="basic-item"><span>身份证号</span><strong>{{ patient.idcard || '—' }}</strong></div>
            <div class="basic-item"><span>联系电话</span><strong>{{ patient.phone || '—' }}</strong></div>
            <div class="basic-item"><span>体检套餐</span><strong>{{ patient.setmeal || '—' }}</strong></div>
            <div class="basic-item"><span>体检日期</span><strong>{{ patient.examDate || '—' }}</strong></div>
            <div class="basic-item"><span>报告医生</span><strong>{{ doctor.realname || '医生' }}</strong></div>
            <div class="basic-item"><span>检查项 / 明细</span><strong>{{ stats.itemCount }} / {{ stats.detailCount }}</strong></div>
          </div>
        </template>

        <!-- 体检结果 -->
        <template v-else-if="activeView === 'result'">
          <div class="main-head">
            <h3>体检结果</h3>
            <div class="legend">
              <span class="tag tag-normal">正常</span>
              <span class="tag tag-error">异常</span>
              <span class="tag tag-muted">未录入</span>
            </div>
          </div>

          <div class="checkitem-tabs">
            <span
              v-for="(item, index) in reports"
              :key="item.cirid"
              class="checkitem-tab"
              :class="{ active: index === activeIndex }"
              @click="activeIndex = index"
            >
              {{ item.ciname }}
              <em v-if="abnormalOf(item)" class="tab-badge">{{ abnormalOf(item) }}</em>
            </span>
          </div>

          <div class="tab-caption">
            共 {{ activeDetails.length }} 个明细项 · 异常
            <span class="red">{{ abnormalOf(activeItem) }}</span> 项
          </div>

          <el-table :data="activeDetails" row-key="cidrid" class="result-table" empty-text="暂无检查项明细">
            <el-table-column label="项目名称" min-width="150">
              <template #default="{ row }">
                <span class="item-name">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="结果" min-width="180">
              <template #default="{ row }">
                <el-input
                  v-model="row.value"
                  size="default"
                  :placeholder="inputPlaceholder(row)"
                  clearable
                  @input="onValueChange(row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="单位" width="100">
              <template #default="{ row }">{{ row.unit || '—' }}</template>
            </el-table-column>
            <el-table-column label="参考范围" min-width="150">
              <template #default="{ row }">{{ formatRange(row) }}</template>
            </el-table-column>
            <el-table-column label="异常提示" width="120" align="center">
              <template #default="{ row }">
                <span class="tag" :class="abnormalHint(row).cls">{{ abnormalHint(row).text }}</span>
              </template>
            </el-table-column>
          </el-table>

          <p class="result-tip">
            <el-icon><InfoFilled /></el-icon>
            数值型项目按参考范围自动判定异常；描述型项目请填写文字结论。
          </p>
        </template>

        <!-- 医学建议 -->
        <template v-else-if="activeView === 'advice'">
          <div class="main-head">
            <h3>医学建议</h3>
            <span class="tag tag-muted">TODO：待 overallResult 接口接入后持久化</span>
          </div>

          <div class="advice-form">
            <p class="field-label">医生意见</p>
            <el-input
              v-model="advice.opinion"
              type="textarea"
              :rows="6"
              placeholder="请输入医生意见，例如：本次体检未见明显异常，建议保持健康生活方式。"
              @input="dirty = true"
            />

            <p class="field-label">建议项</p>
            <el-checkbox-group v-model="advice.suggestions" @change="dirty = true">
              <el-checkbox v-for="s in suggestionOptions" :key="s" :label="s" :value="s">{{ s }}</el-checkbox>
            </el-checkbox-group>

            <p class="mock-tip">
              <el-icon><InfoFilled /></el-icon>
              医学建议字段属于总检结论模块（队友负责的 overallResult 接口），当前先保存在浏览器本地，仅用于页面展示。
            </p>
          </div>
        </template>

        <!-- 其他 -->
        <template v-else>
          <div class="main-head">
            <h3>其他</h3>
          </div>
          <div class="other-box">
            <p class="field-label">报告备注</p>
            <el-input
              v-model="remark"
              type="textarea"
              :rows="4"
              placeholder="可填写本次报告的特殊说明（本模块接口暂不包含备注字段）"
              @input="dirty = true"
            />
            <div class="api-note">
              <p>当前页面使用的接口：</p>
              <ul>
                <li>ciReport/listCiReport —— 查询检查项及明细</li>
                <li>ciDetailedReport/updateCiDetailedReport —— 保存检查结果</li>
              </ul>
            </div>
          </div>
        </template>
      </div>

      <!-- 右侧医生意见 -->
      <aside class="card right-panel">
        <div class="right-title">
          <el-icon><ChatLineSquare /></el-icon>
          医生意见
        </div>
        <el-input
          v-model="advice.opinion"
          type="textarea"
          :rows="5"
          placeholder="请输入医生意见…"
          @input="dirty = true"
        />

        <div class="right-title mt">
          <el-icon><Opportunity /></el-icon>
          建议
        </div>
        <el-checkbox-group v-model="advice.suggestions" class="right-suggestions" @change="dirty = true">
          <el-checkbox v-for="s in suggestionOptions" :key="s" :label="s" :value="s">{{ s }}</el-checkbox>
        </el-checkbox-group>

        <div class="right-actions">
          <el-button :loading="saving" @click="handleSave(false)">保存草稿</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSave(true)">提交审核</el-button>
        </div>
        <p class="right-status">
          <span class="dot" :class="dirty ? 'dot-warn' : 'dot-ok'"></span>
          {{ dirty ? '有未保存的修改' : '所有修改已保存' }}
        </p>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave, useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createReportTemplate, listCiReport } from '@/api/ciReport'
import { updateCiDetailedReport } from '@/api/ciDetailedReport'
import { detectError, formatRange, hasValue } from '@/utils/report'
import { getSessionStorage } from '@/common'
import { findMockPatient } from '@/mock/patients'

const route = useRoute()
const router = useRouter()
const doctor = getSessionStorage('doctor') || {}

const orderid = route.params.orderid

const reports = ref([])
const activeIndex = ref(0)
const activeView = ref('result')
const loading = ref(false)
const creating = ref(false)
const saving = ref(false)
const submitting = ref(false)
const dirty = ref(false)
const remark = ref('')

const navs = [
  { key: 'basic', label: '基本信息', icon: 'Document' },
  { key: 'result', label: '体检结果', icon: 'List' },
  { key: 'advice', label: '医学建议', icon: 'ChatLineSquare' },
  { key: 'other', label: '其他', icon: 'MoreFilled' }
]

const suggestionOptions = ['定期复查', '进一步检查', '健康指导', '其他']

const advice = reactive({ opinion: '', suggestions: [] })

// TODO: 患者信息来自 orders/users 接口（队友负责），当前用示例数据 + 路由参数兜底
const patient = computed(() => {
  const mock = findMockPatient(orderid)
  if (mock) return mock
  return {
    name: route.query.name ? String(route.query.name) : '',
    sex: route.query.sex !== undefined ? Number(route.query.sex) : 1,
    age: route.query.age ? Number(route.query.age) : '',
    idcard: route.query.idcard ? String(route.query.idcard) : '',
    phone: '',
    setmeal: '',
    examDate: ''
  }
})

const activeItem = computed(() => reports.value[activeIndex.value] || null)
const activeDetails = computed(() => (activeItem.value ? activeItem.value.cidetailedreports : []))

const stats = computed(() => {
  let detailCount = 0
  let abnormalCount = 0
  let enteredCount = 0
  reports.value.forEach((item) => {
    ;(item.cidetailedreports || []).forEach((d) => {
      detailCount++
      if (d.iserror === 1) abnormalCount++
      if (hasValue(d)) enteredCount++
    })
  })
  return {
    itemCount: reports.value.length,
    detailCount,
    abnormalCount,
    enteredCount,
    normalCount: detailCount - abnormalCount,
    progress: detailCount ? Math.round((enteredCount / detailCount) * 100) : 0
  }
})

const progressColor = computed(() => (stats.value.progress === 100 ? '#12b76a' : '#2f6bff'))

const adviceKey = computed(() => `xxhealth:advice:${orderid}`)

function abnormalOf(item) {
  return ((item && item.cidetailedreports) || []).filter((d) => d.iserror === 1).length
}

function abnormalHint(row) {
  if (!hasValue(row)) return { text: '未录入', cls: 'tag-muted' }
  if (row.iserror !== 1) return { text: '正常', cls: 'tag-normal' }
  const v = Number(row.value)
  if (!Number.isNaN(v) && row.maxrange !== null && row.maxrange !== undefined && v > Number(row.maxrange)) {
    return { text: '↑ 偏高', cls: 'tag-error' }
  }
  if (!Number.isNaN(v) && row.minrange !== null && row.minrange !== undefined && v < Number(row.minrange)) {
    return { text: '↓ 偏低', cls: 'tag-error' }
  }
  return { text: '异常', cls: 'tag-error' }
}

function inputPlaceholder(row) {
  if (row.type === 4) return '请输入检查结论'
  if (row.type === 1) return '请输入数值'
  return '请输入检查结果'
}

function onValueChange(row) {
  row.iserror = detectError(row, row.value)
  dirty.value = true
}

function loadLocalAdvice() {
  try {
    const raw = localStorage.getItem(adviceKey.value)
    if (!raw) return
    const data = JSON.parse(raw)
    advice.opinion = data.opinion || ''
    advice.suggestions = Array.isArray(data.suggestions) ? data.suggestions : []
    remark.value = data.remark || ''
  } catch (e) {
    // ignore
  }
}

function saveLocalAdvice() {
  // TODO: 医学建议/备注目前无对应接口，先本地暂存；overallResult 接口就绪后改为服务端保存
  try {
    localStorage.setItem(
      adviceKey.value,
      JSON.stringify({ opinion: advice.opinion, suggestions: advice.suggestions, remark: remark.value })
    )
  } catch (e) {
    // ignore
  }
}

async function loadReport() {
  loading.value = true
  try {
    reports.value = (await listCiReport(Number(orderid))) || []
    activeIndex.value = 0
    dirty.value = false
  } catch (e) {
    reports.value = []
  } finally {
    loading.value = false
  }
}

async function handleCreate() {
  creating.value = true
  try {
    const count = await createReportTemplate(Number(orderid))
    if (count > 0) ElMessage.success(`已生成 ${count} 个检查项模板`)
    else ElMessage.info('模板已存在，或该套餐暂无检查项')
    await loadReport()
  } catch (e) {
    // 拦截器已提示
  } finally {
    creating.value = false
  }
}

function buildPayload() {
  const payload = []
  reports.value.forEach((item) => {
    ;(item.cidetailedreports || []).forEach((d) => {
      payload.push({
        cidrid: d.cidrid,
        value: d.value === null || d.value === undefined ? '' : String(d.value),
        iserror: d.iserror
      })
    })
  })
  return payload
}

async function handleSave(isSubmit) {
  if (!reports.value.length) return
  if (isSubmit) {
    try {
      await ElMessageBox.confirm(
        `本次共录入 ${stats.value.enteredCount} 项，异常 ${stats.value.abnormalCount} 项，确认提交审核？`,
        '提交审核',
        { confirmButtonText: '确认提交', cancelButtonText: '再检查一下', type: 'warning' }
      )
    } catch (e) {
      return
    }
  }

  if (isSubmit) submitting.value = true
  else saving.value = true

  try {
    const rows = await updateCiDetailedReport(buildPayload())
    saveLocalAdvice()
    dirty.value = false
    ElMessage.success(
      isSubmit ? `报告已提交审核，共保存 ${rows} 条检查项明细` : `保存成功，共更新 ${rows} 条检查项明细`
    )
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
    submitting.value = false
  }
}

function goDetail() {
  router.push(`/report/detail/${orderid}`)
}

function switchPatient() {
  router.push('/report/create')
}

onBeforeRouteLeave((to, from, next) => {
  if (!dirty.value) return next()
  ElMessageBox.confirm('当前有未保存的修改，确定离开吗？', '提示', {
    confirmButtonText: '离开',
    cancelButtonText: '继续编辑',
    type: 'warning'
  })
    .then(() => next())
    .catch(() => next(false))
})

onMounted(() => {
  loadLocalAdvice()
  loadReport()
})
</script>

<style scoped>
.edit-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 患者条 */
.patient-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 24px;
}

.strip-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.patient-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #5aa0ff, #2f6bff);
  flex-shrink: 0;
}

.patient-avatar.sex-0 {
  background: linear-gradient(135deg, #ff9dbb, #f4628f);
}

.strip-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.strip-name strong {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-1);
}

.strip-age {
  font-size: 13px;
  color: var(--text-2);
}

.strip-meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 18px;
  font-size: 12px;
  color: var(--text-3);
}

.strip-right {
  display: flex;
  gap: 10px;
}

.empty-card {
  padding: 40px 0 50px;
  min-height: 320px;
}

/* 三栏 */
.edit-grid {
  display: grid;
  grid-template-columns: 170px minmax(0, 1fr) 270px;
  gap: 16px;
  align-items: start;
}

.side-nav {
  padding: 12px;
  position: sticky;
  top: 0;
}

.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 11px;
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
  transition: all 0.18s ease;
}

.side-item:hover {
  background: #f6f9ff;
}

.side-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
}

.side-summary {
  margin-top: 12px;
  padding: 12px 6px 4px;
  border-top: 1px solid var(--line);
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--text-3);
  padding: 5px 8px;
}

.summary-row strong {
  color: var(--text-1);
  font-size: 13px;
}

.side-progress {
  margin: 8px 8px 0;
}

/* 主卡片 */
.main-card {
  padding: 20px 22px 22px;
  min-height: 560px;
}

.main-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.main-head h3 {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-1);
}

.legend {
  display: flex;
  gap: 8px;
}

/* 基本信息 */
.basic-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px 28px;
}

.basic-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafe;
  font-size: 13px;
  color: var(--text-2);
}

.basic-item strong {
  color: var(--text-1);
  font-weight: 600;
}

/* 结果表 */
.checkitem-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.checkitem-tab {
  position: relative;
  padding: 8px 16px;
  border-radius: 10px 10px 0 0;
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
}

.checkitem-tab:hover {
  color: var(--brand);
}

.checkitem-tab.active {
  color: var(--brand);
  font-weight: 700;
  background: var(--brand-light);
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  margin-left: 6px;
  border-radius: 9px;
  background: var(--red-bg);
  color: var(--red);
  font-size: 11px;
  font-style: normal;
  font-weight: 700;
}

.tab-caption {
  margin: 12px 2px 8px;
  font-size: 12px;
  color: var(--text-3);
}

.result-table {
  width: 100%;
}

.item-name {
  font-size: 13.5px;
  font-weight: 600;
  color: var(--text-1);
}

.result-tip {
  margin-top: 14px;
  font-size: 12px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 医学建议 / 其他 */
.advice-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-2);
  margin-top: 6px;
}

.mock-tip {
  margin-top: 12px;
  font-size: 11.5px;
  color: var(--text-3);
  display: flex;
  align-items: flex-start;
  gap: 6px;
  line-height: 1.6;
}

.other-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.api-note {
  margin-top: 18px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #f8fafe;
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.api-note p {
  font-weight: 600;
  margin-bottom: 6px;
}

.api-note li {
  line-height: 1.9;
  color: var(--text-3);
}

/* 右栏 */
.right-panel {
  padding: 18px;
  position: sticky;
  top: 0;
}

.right-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
  margin-bottom: 10px;
}

.right-title.mt {
  margin-top: 16px;
}

.right-suggestions {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.right-actions {
  margin-top: 20px;
  display: flex;
  gap: 8px;
}

.right-actions .el-button {
  flex: 1;
}

.right-status {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-ok {
  background: var(--green);
}

.dot-warn {
  background: var(--orange);
}

.red {
  color: var(--red);
}
</style>
