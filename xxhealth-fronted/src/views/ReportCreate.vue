<template>
  <div class="create-page">
    <!-- 步骤条 -->
    <section class="card stepper-card">
      <div class="stepper">
        <div
          v-for="(label, index) in steps"
          :key="label"
          class="step"
          :class="{ active: step === index + 1, done: step > index + 1 }"
        >
          <span class="step-num">
            <el-icon v-if="step > index + 1"><Check /></el-icon>
            <template v-else>{{ index + 1 }}</template>
          </span>
          <span class="step-label">{{ label }}</span>
          <span v-if="index < steps.length - 1" class="step-line"></span>
        </div>
      </div>
    </section>

    <!-- 第一步：选择患者 -->
    <section v-if="step === 1" class="step1-grid">
      <div class="card panel">
        <div class="panel-head">
          <h3>选择患者</h3>
          <span class="panel-sub">按预约编号或患者信息选择本次体检对象</span>
        </div>

        <el-input v-model="searchText" placeholder="请输入姓名 / 身份证号 / 手机号搜索" clearable class="search-input">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>

        <div class="tabs">
          <span
            v-for="tab in tabs"
            :key="tab.key"
            class="tab-item"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >{{ tab.label }}</span>
        </div>

        <div class="patient-list">
          <div
            v-for="patient in filteredPatients"
            :key="patient.orderid"
            class="patient-row"
            :class="{ selected: isSelected(patient) }"
            @click="selectPatient(patient)"
          >
            <div class="patient-avatar" :class="'sex-' + patient.sex">{{ patient.name.slice(0, 1) }}</div>
            <div class="patient-main">
              <div class="patient-name">
                <strong>{{ patient.name }}</strong>
                <span>{{ patient.sex === 1 ? '男' : '女' }}</span>
                <span>{{ patient.age }} 岁</span>
                <span class="tag tag-muted">预约 #{{ patient.orderid }}</span>
              </div>
              <p class="patient-meta">身份证：{{ patient.idcard }} · 体检日期：{{ patient.examDate }}</p>
            </div>
            <el-icon v-if="isSelected(patient)" class="selected-icon"><CircleCheckFilled /></el-icon>
          </div>

          <el-empty v-if="!filteredPatients.length" description="没有匹配的患者" :image-size="80" />
        </div>

        <div class="manual-row">
          <span class="manual-label">或直接输入预约编号</span>
          <el-input
            v-model="manualOrderId"
            placeholder="如 1"
            class="manual-input"
            @input="onManualInput"
          />
        </div>

        <p class="mock-tip">
          <el-icon><InfoFilled /></el-icon>
          患者列表为示例数据（TODO: 待 orders/users 接口接入后替换），可直接输入真实预约编号。
        </p>
      </div>

      <div class="card panel">
        <div class="panel-head">
          <h3>报告信息预览</h3>
          <span class="panel-sub">创建模板前可先确认体检信息</span>
        </div>

        <div class="info-list">
          <div class="info-row"><span>预约编号</span><strong>{{ effectiveOrderId || '—' }}</strong></div>
          <div class="info-row"><span>体检套餐</span><strong>{{ reportInfo.setmeal || '—' }}</strong></div>
          <div class="info-row"><span>体检日期</span><strong>{{ reportInfo.examDate || '—' }}</strong></div>
          <div class="info-row"><span>既往病史</span><strong>{{ reportInfo.history || '无' }}</strong></div>
          <div class="info-row"><span>主诉</span><strong>{{ reportInfo.chief || '无' }}</strong></div>
        </div>

        <div class="conclusion-preview">
          <p>体检结论</p>
          <div class="conclusion-box">{{ reportInfo.conclusion || '待录入检查结果后生成' }}</div>
        </div>

        <div class="panel-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button type="primary" @click="nextToStep2">
            下一步：填写报告<el-icon class="el-icon--right"><ArrowRight /></el-icon>
          </el-button>
        </div>
      </div>
    </section>

    <!-- 第二步：填写报告 -->
    <section v-if="step === 2" class="card step-card">
      <div class="panel-head">
        <h3>填写报告信息</h3>
        <span class="panel-sub">预约 #{{ effectiveOrderId }} · {{ currentPatientText }}</span>
      </div>

      <el-form label-position="top" class="report-form">
        <div class="form-grid">
          <el-form-item label="体检套餐">
            <el-input v-model="reportInfo.setmeal" placeholder="体检套餐" />
          </el-form-item>
          <el-form-item label="体检日期">
            <el-input v-model="reportInfo.examDate" placeholder="体检日期" />
          </el-form-item>
          <el-form-item label="既往病史">
            <el-input v-model="reportInfo.history" placeholder="如：无 / 高血压" />
          </el-form-item>
          <el-form-item label="主诉">
            <el-input v-model="reportInfo.chief" placeholder="如：无 / 头晕" />
          </el-form-item>
        </div>
        <el-form-item label="体检结论">
          <el-input
            v-model="reportInfo.conclusion"
            type="textarea"
            :rows="3"
            placeholder="请输入本次体检结论（为空时由检查结果自动生成）"
          />
        </el-form-item>
      </el-form>

      <div class="template-preview">
        <div class="template-head">
          <span>检查项预览</span>
          <span class="template-count" v-if="reports.length">{{ reports.length }} 个检查项 / {{ stats.detailCount }} 个明细</span>
        </div>
        <div v-if="reports.length" class="template-items">
          <span v-for="item in reports" :key="item.cirid" class="template-item">
            {{ item.ciname }}（{{ item.cidetailedreports.length }} 项）
          </span>
        </div>
        <p v-else class="template-empty">该预约还没有报告模板，下一步将根据体检套餐自动生成。</p>
      </div>

      <p class="mock-tip">
        <el-icon><InfoFilled /></el-icon>
        既往病史 / 主诉 / 体检结论隶属总检结论模块（TODO: overallResult 接口），当前仅用于页面展示。
      </p>

      <div class="panel-actions">
        <el-button @click="step = 1"><el-icon><ArrowLeft /></el-icon>&nbsp;上一步</el-button>
        <el-button type="primary" @click="nextToStep3">
          下一步：预览提交<el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </section>

    <!-- 第三步：预览提交 -->
    <section v-if="step === 3" class="card step-card">
      <div class="panel-head">
        <h3>预览提交</h3>
        <span class="panel-sub">确认无误后创建报告模板并开始录入结果</span>
      </div>

      <div class="preview-grid">
        <div class="preview-block">
          <p class="preview-label">患者信息</p>
          <div class="preview-patient">
            <div class="patient-avatar" :class="'sex-' + (currentPatient.sex || 1)">
              {{ (currentPatient.name || '患').slice(0, 1) }}
            </div>
            <div>
              <strong>{{ currentPatientText || '未选择患者' }}</strong>
              <p class="patient-meta">预约编号 #{{ effectiveOrderId }}</p>
            </div>
          </div>
        </div>

        <div class="preview-block">
          <p class="preview-label">体检信息</p>
          <div class="info-list compact">
            <div class="info-row"><span>体检套餐</span><strong>{{ reportInfo.setmeal || '—' }}</strong></div>
            <div class="info-row"><span>体检日期</span><strong>{{ reportInfo.examDate || '—' }}</strong></div>
            <div class="info-row"><span>既往病史</span><strong>{{ reportInfo.history || '无' }}</strong></div>
            <div class="info-row"><span>主诉</span><strong>{{ reportInfo.chief || '无' }}</strong></div>
          </div>
        </div>

        <div class="preview-block wide">
          <p class="preview-label">检查项预览</p>
          <div v-if="reports.length" class="preview-checkitems">
            <div v-for="item in reports" :key="item.cirid" class="checkitem-chip">
              <span>{{ item.ciname }}</span>
              <small>{{ item.cidetailedreports.length }} 项</small>
            </div>
          </div>
          <p v-else class="template-empty">尚未创建模板，点击「创建报告并开始录入」后自动生成。</p>
        </div>
      </div>

      <div class="panel-actions">
        <el-button @click="step = 2"><el-icon><ArrowLeft /></el-icon>&nbsp;上一步</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreateAndEdit">
          <el-icon><DocumentAdd /></el-icon>&nbsp;创建报告并开始录入
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createReportTemplate, listCiReport } from '@/api/ciReport'
import { findMockPatient, mockPatients, patientText } from '@/mock/patients'
import { summarize } from '@/utils/report'

const route = useRoute()
const router = useRouter()

const steps = ['选择患者', '填写报告', '预览提交']
const step = ref(1)

const tabs = [
  { key: 'recent', label: '最近就诊' },
  { key: 'hospital', label: '本院患者' },
  { key: 'all', label: '全部患者' }
]
const activeTab = ref('recent')
const searchText = ref('')
const selectedOrderId = ref(null)
const manualOrderId = ref('')

const reportInfo = reactive({
  setmeal: '',
  examDate: '',
  history: '无',
  chief: '无',
  conclusion: ''
})

const reports = ref([])
const loading = ref(false)
const creating = ref(false)

const filteredPatients = computed(() => {
  const keyword = searchText.value.trim()
  if (!keyword) return mockPatients
  return mockPatients.filter((p) =>
    [p.name, p.idcard, p.phone].some((v) => v && String(v).includes(keyword))
  )
})

const effectiveOrderId = computed(() => {
  const manual = String(manualOrderId.value).trim()
  if (/^\d+$/.test(manual)) return Number(manual)
  return selectedOrderId.value
})

const currentPatient = computed(() => {
  const found = findMockPatient(effectiveOrderId.value)
  if (found) return found
  return {
    name: route.query.name ? String(route.query.name) : '',
    sex: route.query.sex !== undefined ? Number(route.query.sex) : 1,
    age: route.query.age ? Number(route.query.age) : ''
  }
})

const currentPatientText = computed(() => patientText(currentPatient.value))

const stats = computed(() => summarize(reports.value))

function isSelected(patient) {
  return effectiveOrderId.value === patient.orderid
}

function selectPatient(patient) {
  selectedOrderId.value = patient.orderid
  manualOrderId.value = ''
  reportInfo.setmeal = patient.setmeal
  reportInfo.examDate = patient.examDate
}

function onManualInput(val) {
  if (String(val).trim()) selectedOrderId.value = null
}

async function loadReport() {
  if (!effectiveOrderId.value) return
  loading.value = true
  try {
    reports.value = (await listCiReport(effectiveOrderId.value)) || []
  } catch (e) {
    reports.value = []
  } finally {
    loading.value = false
  }
}

function validateOrder() {
  if (!/^\d+$/.test(String(effectiveOrderId.value || ''))) {
    ElMessage.warning('请选择患者或输入正确的预约编号')
    return false
  }
  return true
}

async function nextToStep2() {
  if (!validateOrder()) return
  const mock = findMockPatient(effectiveOrderId.value)
  if (mock) selectPatient(mock)
  await loadReport()
  step.value = 2
}

async function nextToStep3() {
  if (!validateOrder()) return
  await loadReport()
  step.value = 3
}

async function handleCreateAndEdit() {
  if (!validateOrder()) return
  creating.value = true
  try {
    const count = await createReportTemplate(effectiveOrderId.value)
    await loadReport()
    if (count > 0) {
      ElMessage.success(`已创建 ${count} 个检查项模板`)
    } else if (reports.value.length) {
      ElMessage.info('报告模板已存在，直接进入录入')
    } else {
      ElMessage.warning('未找到该预约或该套餐暂无检查项（示例患者需真实预约数据）')
      creating.value = false
      return
    }
    router.push(`/doctor/report/edit/${effectiveOrderId.value}`)
  } catch (e) {
    // 拦截器已提示
  } finally {
    creating.value = false
  }
}

function goBack() {
  router.push('/doctor/report/create')
}

onMounted(() => {
  const qid = route.query.orderid
  if (qid && /^\d+$/.test(String(qid))) {
    manualOrderId.value = String(qid)
    const mock = findMockPatient(qid)
    if (mock) selectPatient(mock)
  }
})
</script>

<style scoped>
.create-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 步骤条 */
.stepper-card {
  padding: 18px 32px;
}

.stepper {
  display: flex;
  align-items: center;
  gap: 0;
}

.step {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  flex: 1;
}

.step:last-child {
  flex: 0 0 auto;
}

.step-num {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f1f5fc;
  color: var(--text-3);
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.step.active .step-num {
  background: var(--brand);
  color: #fff;
  box-shadow: 0 8px 18px rgba(47, 107, 255, 0.3);
}

.step.done .step-num {
  background: var(--green-bg);
  color: var(--green);
}

.step-label {
  font-size: 13px;
  color: var(--text-3);
  font-weight: 600;
  white-space: nowrap;
}

.step.active .step-label {
  color: var(--text-1);
}

.step.done .step-label {
  color: var(--green);
}

.step-line {
  flex: 1;
  height: 2px;
  background: #eef2f9;
  margin: 0 18px;
}

/* 第一步 */
.step1-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
  align-items: start;
}

.panel {
  padding: 22px 24px 20px;
  min-height: 560px;
  display: flex;
  flex-direction: column;
}

.panel-head {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 16px;
}

.panel-head h3 {
  font-size: 17px;
  font-weight: 800;
  color: var(--text-1);
}

.panel-sub {
  font-size: 12px;
  color: var(--text-3);
}

.search-input {
  margin-bottom: 14px;
}

.tabs {
  display: flex;
  gap: 8px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--line);
}

.tab-item {
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 12.5px;
  color: var(--text-2);
  cursor: pointer;
  background: #f5f8fe;
  transition: all 0.18s ease;
}

.tab-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
}

.patient-list {
  flex: 1;
  margin-top: 8px;
  overflow-y: auto;
  max-height: 320px;
}

.patient-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.18s ease;
}

.patient-row:hover {
  background: #f7faff;
}

.patient-row.selected {
  background: var(--brand-light);
  border-color: #cfe0ff;
}

.patient-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
  background: linear-gradient(135deg, #5aa0ff, #2f6bff);
}

.patient-avatar.sex-0 {
  background: linear-gradient(135deg, #ff9dbb, #f4628f);
}

.patient-main {
  flex: 1;
  min-width: 0;
}

.patient-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-2);
}

.patient-name strong {
  font-size: 14px;
  color: var(--text-1);
}

.patient-meta {
  margin-top: 5px;
  font-size: 11.5px;
  color: var(--text-3);
}

.selected-icon {
  color: var(--brand);
  font-size: 20px;
}

.manual-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed var(--line);
}

.manual-label {
  font-size: 12.5px;
  color: var(--text-2);
  white-space: nowrap;
}

.manual-input {
  max-width: 200px;
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

/* 右侧信息 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 6px 0 4px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--text-2);
}

.info-row strong {
  color: var(--text-1);
  font-weight: 600;
}

.conclusion-preview {
  margin-top: 8px;
}

.conclusion-preview > p {
  font-size: 12.5px;
  color: var(--text-2);
  margin-bottom: 8px;
}

.conclusion-box {
  min-height: 86px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f8fafe;
  border: 1px dashed var(--line);
  font-size: 13px;
  color: var(--text-3);
  line-height: 1.7;
}

.panel-actions {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 第二/三步 */
.step-card {
  padding: 24px 28px;
}

.report-form {
  margin-top: 4px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 24px;
}

.template-preview {
  margin-top: 6px;
  padding: 16px;
  border-radius: 14px;
  background: #f8fafe;
  border: 1px solid var(--line);
}

.template-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-1);
}

.template-count {
  font-size: 12px;
  color: var(--brand);
  font-weight: 600;
}

.template-items {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.template-item {
  padding: 6px 12px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--line);
  font-size: 12px;
  color: var(--text-2);
}

.template-empty {
  margin-top: 10px;
  font-size: 12.5px;
  color: var(--text-3);
}

/* 预览 */
.preview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.preview-block {
  padding: 18px;
  border-radius: 14px;
  background: #f8fafe;
  border: 1px solid var(--line);
}

.preview-block.wide {
  grid-column: span 2;
}

.preview-label {
  font-size: 12px;
  color: var(--text-3);
  margin-bottom: 12px;
}

.preview-patient {
  display: flex;
  align-items: center;
  gap: 12px;
}

.preview-patient strong {
  font-size: 14px;
  color: var(--text-1);
}

.info-list.compact {
  gap: 10px;
}

.preview-checkitems {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.checkitem-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid var(--line);
  font-size: 12.5px;
  color: var(--text-1);
}

.checkitem-chip small {
  font-size: 11px;
  color: var(--text-3);
}
</style>
