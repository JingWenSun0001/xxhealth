<template>
  <div class="page-stack orders-page">
    <section class="stats-grid">
      <article class="stat-card stat-blue">
        <div class="stat-icon"><el-icon><Tickets /></el-icon></div>
        <div>
          <span>匹配预约</span>
          <strong>{{ pagination.total }}</strong>
          <small>当前查询结果</small>
        </div>
      </article>
      <article class="stat-card stat-green">
        <div class="stat-icon"><el-icon><EditPen /></el-icon></div>
        <div>
          <span>待编辑报告</span>
          <strong>{{ currentPending }}</strong>
          <small>当前页未归档</small>
        </div>
      </article>
      <article class="stat-card stat-purple">
        <div class="stat-icon"><el-icon><CircleCheckFilled /></el-icon></div>
        <div>
          <span>已归档报告</span>
          <strong>{{ currentArchived }}</strong>
          <small>当前页已完成</small>
        </div>
      </article>
    </section>

    <section class="content-card filter-card">
      <div class="card-title-row">
        <div>
          <h2>体检报告管理</h2>
          <p>查询预约订单并维护医生总检结论</p>
        </div>
        <span class="scope-badge">订单 / 检查项 / 总检结论</span>
      </div>

      <el-form :model="filters" @submit.prevent="search">
        <div class="filter-grid">
          <el-form-item label="预约编号">
            <el-input v-model="filters.orderid" clearable placeholder="请输入预约编号" />
          </el-form-item>
          <el-form-item label="患者姓名">
            <el-input v-model="filters.realname" clearable placeholder="请输入患者姓名" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="filters.userid" clearable placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="体检套餐">
            <el-select v-model="filters.smid" clearable placeholder="全部套餐">
              <el-option
                v-for="item in setmeals"
                :key="item.smid"
                :label="item.name"
                :value="item.smid"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="报告状态">
            <el-select v-model="filters.state" clearable placeholder="全部状态">
              <el-option label="未归档" :value="1" />
              <el-option label="已归档" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="体检日期">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              range-separator="~"
            />
          </el-form-item>
        </div>
        <div class="filter-actions">
          <el-button :icon="RefreshLeft" @click="reset">重置</el-button>
          <el-button type="primary" :icon="Search" :loading="loading" @click="search">查询</el-button>
        </div>
      </el-form>
    </section>

    <section class="content-card table-card">
      <div class="section-heading">
        <div>
          <h3>报告列表</h3>
          <span>共 {{ pagination.total }} 条预约记录</span>
        </div>
        <el-button :icon="Refresh" plain :loading="loading" @click="loadOrders">刷新数据</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="orders"
        row-key="orderid"
        empty-text="暂无预约记录"
        class="medical-table"
      >
        <el-table-column type="index" label="序号" width="72" align="center" />
        <el-table-column prop="orderid" label="报告编号" min-width="120">
          <template #default="{ row }">
            <span class="order-code">#{{ row.orderid }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者信息" min-width="190">
          <template #default="{ row }">
            <div class="patient-cell">
              <span class="patient-avatar">{{ patientInitial(row) }}</span>
              <div>
                <strong>{{ row.users?.realname || '-' }}</strong>
                <small>{{ maskPhone(row.userid) }}</small>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="86" align="center">
          <template #default="{ row }">
            <span :class="['gender-pill', row.users?.sex === 1 ? 'male' : 'female']">
              {{ row.users?.sex === 1 ? '男' : '女' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="体检套餐" min-width="190">
          <template #default="{ row }">{{ row.setmeal?.name || '-' }}</template>
        </el-table-column>
        <el-table-column label="体检医院" min-width="170">
          <template #default="{ row }">{{ row.hospital?.name || '-' }}</template>
        </el-table-column>
        <el-table-column prop="orderdate" label="体检日期" width="125" />
        <el-table-column label="报告状态" width="112" align="center">
          <template #default="{ row }">
            <span :class="['state-pill', row.state === 2 ? 'archived' : 'pending']">
              {{ row.state === 2 ? '已归档' : '待编辑' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row.orderid)">
              {{ row.state === 2 ? '查看报告' : '编辑报告' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <span>共 {{ pagination.total }} 条</span>
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next, jumper"
          @current-change="loadOrders"
          @size-change="handleSizeChange"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  CircleCheckFilled,
  EditPen,
  Refresh,
  RefreshLeft,
  Search,
  Tickets
} from '@element-plus/icons-vue'
import { listOrders } from '../api/orders'
import { listSetmeal } from '../api/setmeal'

const router = useRouter()
const loading = ref(false)
const orders = ref([])
const setmeals = ref([])
const dateRange = ref([])
const filters = reactive({ orderid: '', userid: '', realname: '', smid: null, state: null })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const currentPending = computed(() => orders.value.filter(item => item.state === 1).length)
const currentArchived = computed(() => orders.value.filter(item => item.state === 2).length)

async function loadOrders() {
  loading.value = true
  try {
    const data = await listOrders({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      orderid: filters.orderid ? Number(filters.orderid) : null,
      userid: filters.userid || null,
      realname: filters.realname || null,
      smid: filters.smid,
      state: filters.state,
      startDate: dateRange.value?.[0] || null,
      endDate: dateRange.value?.[1] || null
    })
    orders.value = data?.list || []
    pagination.total = data?.total || 0
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function search() {
  pagination.pageNum = 1
  loadOrders()
}

function reset() {
  Object.assign(filters, { orderid: '', userid: '', realname: '', smid: null, state: null })
  dateRange.value = []
  pagination.pageNum = 1
  loadOrders()
}

function handleSizeChange() {
  pagination.pageNum = 1
  loadOrders()
}

function openDetail(orderId) {
  router.push({ name: 'order-detail', params: { orderId } })
}

function patientInitial(row) {
  return row.users?.realname?.slice(0, 1) || '患'
}

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '-'
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

onMounted(async () => {
  try {
    setmeals.value = (await listSetmeal()) || []
  } catch (error) {
    ElMessage.error(error.message)
  }
  loadOrders()
})
</script>
