<template>
  <div class="message-page">
    <!-- 统计 -->
    <section class="stat-grid">
      <div class="stat-card card">
        <div class="stat-icon blue"><el-icon><ChatDotRound /></el-icon></div>
        <div class="stat-body">
          <p>全部消息</p>
          <strong>{{ messages.length }}<small>条</small></strong>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon red"><el-icon><Warning /></el-icon></div>
        <div class="stat-body">
          <p>未读消息</p>
          <strong class="danger">{{ unreadCount }}<small>条</small></strong>
        </div>
      </div>
      <div class="stat-card card">
        <div class="stat-icon green"><el-icon><CircleCheckFilled /></el-icon></div>
        <div class="stat-body">
          <p>已读消息</p>
          <strong>{{ readCount }}<small>条</small></strong>
        </div>
      </div>
      <div class="read-all">
        <el-button type="primary" plain :disabled="!unreadCount" :loading="markingAll" @click="markAllRead">
          <el-icon><Select /></el-icon>&nbsp;全部已读
        </el-button>
      </div>
    </section>

    <section class="message-grid">
      <!-- 分类 -->
      <aside class="card category-card">
        <div class="category-head">消息分类</div>
        <ul class="category-list">
          <li
            v-for="type in messageTypes"
            :key="type.key"
            class="category-item"
            :class="{ active: activeType === type.key }"
            @click="selectType(type.key)"
          >
            <el-icon class="category-icon"><component :is="type.icon" /></el-icon>
            <span class="category-name">{{ type.label }}</span>
            <small>{{ typeCount(type.key) }}</small>
          </li>
        </ul>
      </aside>

      <!-- 列表 -->
      <div class="card list-card" v-loading="loading">
        <div class="list-toolbar">
          <el-input v-model="keyword" placeholder="请输入关键词搜索消息…" clearable class="search-input">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="statusFilter" class="toolbar-select">
            <el-option label="全部状态" value="all" />
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
          </el-select>
          <el-select v-model="timeFilter" class="toolbar-select">
            <el-option label="近三个月" value="3m" />
            <el-option label="近一周" value="1w" />
            <el-option label="全部时间" value="all" />
          </el-select>
        </div>

        <div class="message-list">
          <div
            v-for="item in pagedMessages"
            :key="item.msgid"
            class="message-item"
            :class="{ unread: item.isread === 0 }"
            @click="markRead(item)"
          >
            <div class="message-icon" :class="typeColor(item.type)">
              <el-icon><component :is="typeIcon(item.type)" /></el-icon>
            </div>

            <div class="message-main">
              <div class="message-title">
                <span>{{ item.title }}</span>
                <em v-if="item.isread === 0" class="unread-dot"></em>
              </div>
              <p class="message-desc">{{ item.content }}</p>
            </div>

            <div class="message-side">
              <span class="message-time">{{ formatTime(item.createtime) }}</span>
              <span class="message-state" :class="item.isread === 1 ? 'is-read' : 'is-unread'">
                {{ item.isread === 1 ? '已读' : '未读' }}
              </span>
            </div>

            <el-icon class="message-arrow"><ArrowRight /></el-icon>
          </div>

          <el-empty v-if="!loading && !filteredMessages.length" description="没有符合条件的消息" :image-size="110" />
        </div>

        <div v-if="filteredMessages.length > pageSize" class="pager">
          <span class="pager-total">共 {{ filteredMessages.length }} 条</span>
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="filteredMessages.length"
            layout="prev, pager, next"
            background
          />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMessages, markAllMessagesRead, markMessageRead } from '@/api/message'

const messages = ref([])
const loading = ref(false)
const markingAll = ref(false)

const activeType = ref('all')
const keyword = ref('')
const statusFilter = ref('all')
const timeFilter = ref('3m')
const page = ref(1)
const pageSize = 6

const messageTypes = [
  { key: 'all', label: '全部消息', icon: 'ChatDotRound' },
  { key: '预约提醒', label: '预约提醒', icon: 'Calendar' },
  { key: '报告通知', label: '报告通知', icon: 'Document' },
  { key: '异常提醒', label: '异常提醒', icon: 'WarningFilled' },
  { key: '系统通知', label: '系统通知', icon: 'Bell' },
  { key: '其他消息', label: '其他消息', icon: 'MoreFilled' }
]

const unreadCount = computed(() => messages.value.filter((m) => m.isread === 0).length)
const readCount = computed(() => messages.value.filter((m) => m.isread === 1).length)

function pad(n) {
  return n < 10 ? '0' + n : String(n)
}

function formatTime(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function typeCount(key) {
  if (key === 'all') return messages.value.length
  return messages.value.filter((m) => m.type === key).length
}

function typeIcon(type) {
  return {
    预约提醒: 'Calendar',
    报告通知: 'Document',
    异常提醒: 'WarningFilled',
    系统通知: 'Bell',
    其他消息: 'MoreFilled'
  }[type] || 'ChatDotRound'
}

function typeColor(type) {
  return {
    预约提醒: 'blue',
    报告通知: 'green',
    异常提醒: 'red',
    系统通知: 'purple',
    其他消息: 'gray'
  }[type] || 'blue'
}

const filteredMessages = computed(() => {
  const kw = keyword.value.trim()
  const now = new Date()
  const limitDays = timeFilter.value === '1w' ? 7 : timeFilter.value === '3m' ? 90 : 0

  return messages.value.filter((item) => {
    if (activeType.value !== 'all' && item.type !== activeType.value) return false
    if (statusFilter.value === 'unread' && item.isread !== 0) return false
    if (statusFilter.value === 'read' && item.isread !== 1) return false
    if (kw && (item.title || '').indexOf(kw) < 0 && (item.content || '').indexOf(kw) < 0) return false
    if (limitDays > 0) {
      const t = new Date(item.createtime).getTime()
      if (!Number.isNaN(t) && now.getTime() - t > limitDays * 24 * 3600 * 1000) return false
    }
    return true
  })
})

const pagedMessages = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredMessages.value.slice(start, start + pageSize)
})

function selectType(key) {
  activeType.value = key
  page.value = 1
}

async function loadMessages() {
  loading.value = true
  try {
    messages.value = (await listMessages({})) || []
  } catch (e) {
    messages.value = []
  } finally {
    loading.value = false
  }
}

async function markRead(item) {
  if (item.isread === 1) return
  try {
    await markMessageRead(item.msgid)
    item.isread = 1
  } catch (e) {
    // 拦截器已提示
  }
}

async function markAllRead() {
  if (!unreadCount.value) return
  markingAll.value = true
  try {
    await markAllMessagesRead()
    messages.value.forEach((m) => {
      m.isread = 1
    })
    ElMessage.success('已全部标记为已读')
  } catch (e) {
    // 拦截器已提示
  } finally {
    markingAll.value = false
  }
}

onMounted(loadMessages)
</script>

<style scoped>
.message-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr) 200px;
  gap: 16px;
  align-items: stretch;
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
.stat-icon.red { color: var(--red); background: var(--red-bg); }
.stat-icon.green { color: var(--green); background: var(--green-bg); }

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

.read-all {
  display: flex;
  align-items: center;
  justify-content: center;
}

.read-all .el-button {
  width: 100%;
  height: 46px;
}

.message-grid {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.category-card {
  padding: 16px 12px;
}

.category-head {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  padding: 0 10px 12px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
  border: 1px solid transparent;
}

.category-item:hover {
  background: #f7faff;
}

.category-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
  border-color: #cfe0ff;
}

.category-icon {
  font-size: 16px;
}

.category-name {
  flex: 1;
}

.category-item small {
  font-size: 11px;
  color: var(--text-3);
}

.list-card {
  padding: 18px 20px 22px;
  min-height: 520px;
}

.list-toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
}

.toolbar-select {
  width: 130px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: #fff;
  cursor: pointer;
  transition: all 0.18s ease;
}

.message-item:hover {
  background: #f8fbff;
  border-color: #dbe7ff;
  transform: translateX(2px);
}

.message-item.unread {
  background: #f7faff;
  border-color: #dbe7ff;
}

.message-icon {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 19px;
  flex-shrink: 0;
}

.message-icon.blue { color: var(--brand); background: var(--brand-light); }
.message-icon.green { color: var(--green); background: var(--green-bg); }
.message-icon.red { color: var(--red); background: var(--red-bg); }
.message-icon.purple { color: var(--purple); background: var(--purple-bg); }
.message-icon.gray { color: var(--text-2); background: #f1f4f9; }

.message-main {
  flex: 1;
  min-width: 0;
}

.message-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--text-1);
}

.unread-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--brand);
}

.message-desc {
  margin-top: 5px;
  font-size: 12px;
  color: var(--text-2);
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}

.message-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.message-time {
  font-size: 11.5px;
  color: var(--text-3);
}

.message-state {
  font-size: 11px;
  font-weight: 600;
}

.message-state.is-read {
  color: var(--text-3);
}

.message-state.is-unread {
  color: var(--brand);
}

.message-arrow {
  color: #c3cddd;
  font-size: 14px;
}

.pager {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 18px;
}

.pager-total {
  font-size: 12px;
  color: var(--text-3);
}
</style>
