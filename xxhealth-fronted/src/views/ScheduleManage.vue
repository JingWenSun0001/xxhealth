<template>
  <div class="schedule-page">
    <!-- 统计 -->
    <section class="stat-grid">
      <div class="stat-card card">
        <div class="stat-icon blue"><el-icon><Calendar /></el-icon></div>
        <div class="stat-body">
          <p>今日预约</p>
          <strong>{{ stats.today }}<small>人</small></strong>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon orange"><el-icon><User /></el-icon></div>
        <div class="stat-body">
          <p>待检人数</p>
          <strong>{{ stats.waiting }}<small>人</small></strong>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon green"><el-icon><CircleCheckFilled /></el-icon></div>
        <div class="stat-body">
          <p>已检人数</p>
          <strong>{{ stats.done }}<small>人</small></strong>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon red"><el-icon><WarningFilled /></el-icon></div>
        <div class="stat-body">
          <p>异常提醒</p>
          <strong class="danger">{{ stats.abnormal }}<small>条</small></strong>
        </div>
      </div>
    </section>

    <section class="schedule-grid">
      <!-- 日历 -->
      <aside class="card calendar-card">
        <div class="cal-head">
          <span class="cal-title">{{ viewYear }}年{{ viewMonth + 1 }}月</span>
          <div class="cal-nav">
            <button class="nav-btn" @click="changeMonth(-1)"><el-icon><ArrowLeft /></el-icon></button>
            <button class="nav-btn" @click="changeMonth(1)"><el-icon><ArrowRight /></el-icon></button>
          </div>
        </div>

        <div class="cal-week">
          <span v-for="w in weekNames" :key="w">{{ w }}</span>
        </div>

        <div class="cal-days">
          <div
            v-for="(cell, index) in calendarCells"
            :key="index"
            class="cal-day"
            :class="{
              muted: !cell.current,
              today: cell.date === today,
              selected: cell.date === selectedDate,
              has: cell.count > 0
            }"
            @click="selectDate(cell)"
          >
            <span>{{ cell.day }}</span>
            <em v-if="cell.count" class="dot"></em>
          </div>
        </div>

        <div class="cal-legend">
          <span><i class="dot-blue"></i>已预约</span>
          <span><i class="dot-green"></i>已完成</span>
        </div>

        <div class="cal-actions">
          <el-button size="small" @click="goToday">今天</el-button>
          <el-button size="small" type="primary" plain @click="handleMock('新增预约')">新增预约</el-button>
        </div>
      </aside>

      <!-- 当日时间轴 -->
      <div class="card timeline-card" v-loading="loading">
        <div class="timeline-head">
          <div>
            <h3>{{ selectedDateText }}</h3>
            <p>共 {{ appointments.length }} 位受检者</p>
          </div>
          <span class="tag tag-info">{{ appointments.length }} 人</span>
        </div>

        <div v-if="appointments.length" class="timeline">
          <div
            v-for="item in appointments"
            :key="item.orderid"
            class="timeline-item"
            :class="statusClass(item.stateText)"
          >
            <div class="timeline-time">{{ (item.timeSlot || '08:00').split('-')[0] }}</div>
            <div class="timeline-body">
              <div class="appointment-head">
                <div class="appointment-name">
                  <span class="avatar" :class="item.sex === 0 ? 'female' : 'male'">{{ maskName(item.patientName).slice(0, 1) }}</span>
                  <div>
                    <p>{{ maskName(item.patientName) }}
                      <small>{{ item.sex === 0 ? '女' : '男' }}<template v-if="item.age"> · {{ item.age }}岁</template></small>
                    </p>
                    <span class="appointment-setmeal">{{ item.timeSlot }} · {{ item.setmealName }}</span>
                  </div>
                </div>
                <div class="appointment-right">
                  <span v-if="item.abnormalCount" class="tag tag-error">异常 {{ item.abnormalCount }} 项</span>
                  <span class="tag" :class="statusTag(item.stateText)">{{ item.stateText }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else-if="!loading" description="当天暂无预约安排" :image-size="110" />
      </div>

      <!-- 右侧列表 -->
      <aside class="card side-card">
        <div class="side-tabs">
          <span
            v-for="tab in sideTabs"
            :key="tab.key"
            class="side-tab"
            :class="{ active: sideTab === tab.key }"
            @click="sideTab = tab.key"
          >{{ tab.label }}</span>
        </div>

        <div v-if="sideTab === 'list'" class="side-list">
          <div v-for="item in appointments" :key="item.orderid" class="side-item">
            <span class="avatar small" :class="item.sex === 0 ? 'female' : 'male'">{{ maskName(item.patientName).slice(0, 1) }}</span>
            <div class="side-main">
              <p>{{ maskName(item.patientName) }} <small>{{ item.sex === 0 ? '女' : '男' }}<template v-if="item.age"> · {{ item.age }}岁</template></small></p>
              <span>{{ item.timeSlot }} · {{ item.setmealName }}</span>
            </div>
            <span class="tag" :class="statusTag(item.stateText)">{{ item.stateText }}</span>
          </div>
          <el-empty v-if="!appointments.length" description="暂无预约" :image-size="80" />
        </div>

        <div v-else-if="sideTab === 'check'" class="side-list">
          <div v-for="(item, index) in checkReminders" :key="index" class="side-item check">
            <div class="reminder-time">{{ (item.timeSlot || '08:00').split('-')[0] }}</div>
            <div class="side-main">
              <p>{{ maskName(item.patientName) }}</p>
              <span>{{ item.itemName }}：{{ item.content }}</span>
            </div>
          </div>
          <el-empty v-if="!checkReminders.length" description="暂无检查提醒" :image-size="80" />
        </div>

        <div v-else class="side-list">
          <div v-for="(item, index) in abnormalReminders" :key="index" class="side-item check">
            <div class="reminder-time danger">{{ (item.timeSlot || '08:00').split('-')[0] }}</div>
            <div class="side-main">
              <p>{{ maskName(item.patientName) }} <span class="tag tag-error">{{ item.level }}</span></p>
              <span>{{ item.item }}：{{ item.value }}{{ item.unit || '' }}（参考 {{ item.rangeText }}）</span>
            </div>
          </div>
          <el-empty v-if="!abnormalReminders.length" description="当天暂无异常提醒" :image-size="80" />
        </div>

        <p class="mock-tip">
          <el-icon><InfoFilled /></el-icon>
          数据来自 /schedule 接口（orders + users + setmeal 联查）
        </p>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getScheduleDay, listScheduleByMonth } from '@/api/schedule'

const weekNames = ['一', '二', '三', '四', '五', '六', '日']

const today = formatDate(new Date())
const selectedDate = ref(today)
const now = new Date()
const viewYear = ref(now.getFullYear())
const viewMonth = ref(now.getMonth())

const monthAppointments = ref([])
const appointments = ref([])
const checkReminders = ref([])
const abnormalReminders = ref([])
const todayAppointments = ref([])
const todayAbnormal = ref([])
const loading = ref(false)

const sideTab = ref('list')
const sideTabs = [
  { key: 'list', label: '预约列表' },
  { key: 'check', label: '检查提醒' },
  { key: 'abnormal', label: '异常提醒' }
]

function pad(n) {
  return n < 10 ? '0' + n : String(n)
}

function formatDate(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

function maskName(name) {
  if (!name) return '—'
  if (name.length <= 1) return name
  if (name.length === 2) return name[0] + '*'
  return name[0] + '*' + name[name.length - 1]
}

const countByDate = computed(() => {
  const map = {}
  monthAppointments.value.forEach((item) => {
    map[item.date] = (map[item.date] || 0) + 1
  })
  return map
})

const calendarCells = computed(() => {
  const year = viewYear.value
  const month = viewMonth.value
  const first = new Date(year, month, 1)
  const offset = (first.getDay() + 6) % 7
  const daysInMonth = new Date(year, month + 1, 0).getDate()
  const cells = []

  const prevMonthDays = new Date(year, month, 0).getDate()
  for (let i = offset - 1; i >= 0; i--) {
    const d = prevMonthDays - i
    const date = formatDate(new Date(year, month - 1, d))
    cells.push({ day: d, date, current: false, count: countByDate.value[date] || 0 })
  }
  for (let d = 1; d <= daysInMonth; d++) {
    const date = formatDate(new Date(year, month, d))
    cells.push({ day: d, date, current: true, count: countByDate.value[date] || 0 })
  }
  let next = 1
  while (cells.length < 42) {
    const date = formatDate(new Date(year, month + 1, next))
    cells.push({ day: next, date, current: false, count: countByDate.value[date] || 0 })
    next++
  }
  return cells
})

const stats = computed(() => ({
  today: todayAppointments.value.length,
  waiting: todayAppointments.value.filter((a) => a.state === 1).length,
  done: todayAppointments.value.filter((a) => a.state === 2).length,
  abnormal: todayAbnormal.value.length
}))

const selectedDateText = computed(() => {
  const parts = selectedDate.value.split('-')
  const d = new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]))
  const week = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${parts[0]}年${parts[1]}月${parts[2]}日（周${week}）`
})

function statusTag(stateText) {
  return stateText === '已完成' ? 'tag-normal' : 'tag-info'
}

function statusClass(stateText) {
  return stateText === '已完成' ? 'is-done' : 'is-booked'
}

function changeMonth(step) {
  const d = new Date(viewYear.value, viewMonth.value + step, 1)
  viewYear.value = d.getFullYear()
  viewMonth.value = d.getMonth()
}

async function loadMonth() {
  try {
    monthAppointments.value = (await listScheduleByMonth(viewYear.value, viewMonth.value + 1)) || []
  } catch (e) {
    monthAppointments.value = []
  }
}

async function loadDay(date) {
  loading.value = true
  try {
    const data = await getScheduleDay(date)
    appointments.value = (data && data.appointments) || []
    checkReminders.value = (data && data.checkReminders) || []
    abnormalReminders.value = (data && data.abnormalReminders) || []
  } catch (e) {
    appointments.value = []
    checkReminders.value = []
    abnormalReminders.value = []
  } finally {
    loading.value = false
  }
}

async function loadToday() {
  try {
    const data = await getScheduleDay(today)
    todayAppointments.value = (data && data.appointments) || []
    todayAbnormal.value = (data && data.abnormalReminders) || []
  } catch (e) {
    todayAppointments.value = []
    todayAbnormal.value = []
  }
}

function selectDate(cell) {
  selectedDate.value = cell.date
  loadDay(cell.date)
}

function goToday() {
  const d = new Date()
  viewYear.value = d.getFullYear()
  viewMonth.value = d.getMonth()
  selectedDate.value = today
  loadDay(today)
}

function handleMock(label) {
  ElMessage.info(`「${label}」功能属于预约模块，待 orders 写接口接入`)
}

watch([viewYear, viewMonth], () => {
  loadMonth()
})

onMounted(() => {
  loadToday()
  loadMonth()
  loadDay(today)
})
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-icon.blue { color: var(--brand); background: var(--brand-light); }
.stat-icon.orange { color: var(--orange); background: var(--orange-bg); }
.stat-icon.green { color: var(--green); background: var(--green-bg); }
.stat-icon.red { color: var(--red); background: var(--red-bg); }

.stat-body p {
  font-size: 12px;
  color: var(--text-3);
}

.stat-body strong {
  display: block;
  margin-top: 4px;
  font-size: 24px;
  font-weight: 800;
  color: var(--text-1);
}

.stat-body strong small {
  margin-left: 3px;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-3);
}

.danger {
  color: var(--red) !important;
}

.schedule-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr) 330px;
  gap: 16px;
  align-items: start;
}

.calendar-card {
  padding: 18px;
}

.cal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.cal-title {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-1);
}

.cal-nav {
  display: flex;
  gap: 6px;
}

.nav-btn {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  border: 1px solid var(--line);
  background: #fff;
  color: var(--text-2);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn:hover {
  color: var(--brand);
  border-color: #cfe0ff;
}

.cal-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  font-size: 11.5px;
  color: var(--text-3);
  text-align: center;
  margin-bottom: 6px;
}

.cal-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.cal-day {
  position: relative;
  height: 38px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 12.5px;
  color: var(--text-1);
  cursor: pointer;
  transition: all 0.15s ease;
}

.cal-day:hover {
  background: #f4f8ff;
}

.cal-day.muted {
  color: #c3cddd;
}

.cal-day.today {
  color: var(--brand);
  font-weight: 700;
  background: #f0f5ff;
}

.cal-day.selected {
  background: var(--brand);
  color: #fff;
  font-weight: 700;
  box-shadow: 0 8px 16px rgba(47, 107, 255, 0.28);
}

.cal-day .dot {
  position: absolute;
  bottom: 4px;
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--brand);
}

.cal-day.selected .dot {
  background: #fff;
}

.cal-legend {
  display: flex;
  gap: 14px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  font-size: 11px;
  color: var(--text-3);
}

.cal-legend span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.cal-legend i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.dot-blue { background: var(--brand); }
.dot-green { background: var(--green); }

.cal-actions {
  display: flex;
  gap: 8px;
  margin-top: 14px;
}

.cal-actions .el-button {
  flex: 1;
}

.timeline-card {
  padding: 18px 20px 22px;
  min-height: 520px;
}

.timeline-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.timeline-head h3 {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-1);
}

.timeline-head p {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-3);
}

.timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.timeline-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.timeline-time {
  width: 48px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-2);
  padding-top: 14px;
}

.timeline-body {
  flex: 1;
  border-radius: 14px;
  border: 1px solid var(--line);
  border-left: 3px solid var(--brand);
  padding: 12px 14px;
  background: #fbfcff;
}

.timeline-item.is-done .timeline-body {
  border-left-color: var(--green);
}

.timeline-item.is-booked .timeline-body {
  border-left-color: var(--brand);
}

.appointment-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.appointment-name {
  display: flex;
  align-items: center;
  gap: 10px;
}

.appointment-name p {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--text-1);
}

.appointment-name small {
  margin-left: 6px;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--text-3);
}

.appointment-setmeal {
  display: block;
  margin-top: 3px;
  font-size: 11.5px;
  color: var(--text-3);
}

.appointment-right {
  display: flex;
 align-items: center;
  gap: 8px;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  background: linear-gradient(135deg, #5aa0ff, #2f6bff);
}

.avatar.female {
  background: linear-gradient(135deg, #ff9dbb, #f4628f);
}

.avatar.small {
  width: 32px;
  height: 32px;
  font-size: 12px;
}

.side-card {
  padding: 16px;
  min-height: 520px;
}

.side-tabs {
  display: flex;
  gap: 18px;
  border-bottom: 1px solid var(--line);
  padding-bottom: 10px;
}

.side-tab {
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
  position: relative;
  padding-bottom: 8px;
}

.side-tab.active {
  color: var(--brand);
  font-weight: 700;
}

.side-tab.active::after {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  bottom: -10px;
  height: 2px;
  background: var(--brand);
  border-radius: 2px;
}

.side-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
  max-height: 420px;
  overflow-y: auto;
}

.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 12px;
  background: #f8fafe;
  border: 1px solid var(--line);
}

.side-main {
  flex: 1;
  min-width: 0;
}

.side-main p {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text-1);
}

.side-main small {
  color: var(--text-3);
  font-weight: 500;
}

.side-main span {
  display: block;
  margin-top: 3px;
  font-size: 11px;
  color: var(--text-3);
  line-height: 1.5;
}

.side-item.check {
  align-items: flex-start;
}

.reminder-time {
  width: 44px;
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 700;
  color: var(--brand);
  padding-top: 2px;
}

.reminder-time.danger {
  color: var(--red);
}

.mock-tip {
  margin-top: 14px;
  font-size: 11px;
  color: var(--text-3);
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
