<template>
  <div v-loading="loading" class="page-stack detail-page">
    <div class="detail-toolbar">
      <el-button :icon="ArrowLeft" plain @click="router.push('/orders')">返回报告列表</el-button>
      <el-button
        v-if="order"
        :type="order.state === 1 ? 'primary' : 'default'"
        :icon="order.state === 1 ? CircleCheck : RefreshLeft"
        :loading="stateUpdating"
        @click="toggleArchive"
      >
        {{ order.state === 1 ? '完成并归档' : '恢复编辑' }}
      </el-button>
    </div>

    <section v-if="order" class="report-summary-card">
      <div class="patient-summary">
        <div class="large-patient-avatar">{{ order.users?.realname?.slice(0, 1) || '患' }}</div>
        <div>
          <div class="patient-name-line">
            <h2>{{ order.users?.realname || '体检用户' }}</h2>
            <span>{{ order.users?.sex === 1 ? '男' : '女' }}</span>
          </div>
          <p>ID：{{ order.orderid }}</p>
          <p>手机号：{{ maskPhone(order.userid) }}</p>
        </div>
      </div>

      <div class="report-summary-item blue">
        <span class="summary-icon"><el-icon><Calendar /></el-icon></span>
        <div><small>体检日期</small><strong>{{ order.orderdate }}</strong></div>
      </div>
      <div class="report-summary-item cyan">
        <span class="summary-icon"><el-icon><Tickets /></el-icon></span>
        <div><small>体检套餐</small><strong>{{ order.setmeal?.name || '-' }}</strong></div>
      </div>
      <div class="report-summary-item green">
        <span class="summary-icon"><el-icon><CircleCheckFilled /></el-icon></span>
        <div>
          <small>报告状态</small>
          <strong :class="order.state === 2 ? 'is-finished' : 'is-editing'">
            {{ order.state === 2 ? '已归档' : '编辑中' }}
          </strong>
        </div>
      </div>
    </section>

    <section class="report-workspace">
      <aside class="report-aside">
        <div class="aside-heading">
          <h3>结论目录</h3>
          <span>{{ results.length }} 项</span>
        </div>
        <div v-if="results.length" class="aside-result-list">
          <button
            v-for="(item, index) in results"
            :key="item.orid"
            type="button"
            :class="{ active: selectedResultId === item.orid }"
            @click="selectedResultId = item.orid"
          >
            <span><el-icon><Document /></el-icon></span>
            <div>
              <strong>{{ item.title }}</strong>
              <small>总检结论 {{ index + 1 }}</small>
            </div>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <el-empty v-else :image-size="66" description="暂无结论" />
      </aside>

      <main class="content-card conclusion-card">
        <div class="section-heading">
          <div>
            <h3>总检结论</h3>
            <span>医生录入的总体检查结论与健康建议</span>
          </div>
          <el-button
            type="primary"
            :icon="Plus"
            :disabled="order?.state === 2"
            @click="openEditor()"
          >新增结论</el-button>
        </div>

        <div v-if="results.length" class="conclusion-list">
          <article
            v-for="(item, index) in results"
            :key="item.orid"
            :class="['conclusion-item', { selected: selectedResultId === item.orid }]"
            @click="selectedResultId = item.orid"
          >
            <span class="conclusion-index">{{ String(index + 1).padStart(2, '0') }}</span>
            <div class="conclusion-copy">
              <div class="conclusion-title-line">
                <h4>{{ item.title }}</h4>
                <span>总检结论</span>
              </div>
              <p>{{ item.content || '暂无详细说明' }}</p>
            </div>
            <div class="row-actions">
              <el-button link :icon="EditPen" :disabled="order?.state === 2" @click.stop="openEditor(item)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" :disabled="order?.state === 2" @click.stop="removeItem(item)">删除</el-button>
            </div>
          </article>
        </div>
        <div v-else class="empty-conclusion">
          <span class="empty-icon"><el-icon><DocumentAdd /></el-icon></span>
          <h4>尚未录入总检结论</h4>
          <p>点击“新增结论”填写检查结论与健康建议。</p>
        </div>
      </main>
    </section>

    <el-dialog
      v-model="dialog.visible"
      :title="dialog.form.orid ? '编辑总检结论' : '新增总检结论'"
      width="560px"
      class="medical-dialog"
      destroy-on-close
    >
      <el-form ref="formRef" :model="dialog.form" :rules="rules" label-position="top">
        <el-form-item label="结论标题" prop="title">
          <el-input v-model="dialog.form.title" maxlength="40" show-word-limit placeholder="例如：血压偏高" />
        </el-form-item>
        <el-form-item label="结论内容" prop="content">
          <el-input
            v-model="dialog.form.content"
            type="textarea"
            :rows="7"
            maxlength="400"
            show-word-limit
            placeholder="填写检查结论、风险提示和健康建议"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="submitResult">保存结论</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  ArrowRight,
  Calendar,
  CircleCheck,
  CircleCheckFilled,
  Delete,
  Document,
  DocumentAdd,
  EditPen,
  Plus,
  RefreshLeft,
  Tickets
} from '@element-plus/icons-vue'
import { getOrdersById, updateOrdersState } from '../api/orders'
import {
  listOverallResultByOrderId,
  removeOverallResult,
  saveOverallResult,
  updateOverallResult
} from '../api/overallResult'

const route = useRoute()
const router = useRouter()
const orderId = Number(route.params.orderId)
const loading = ref(false)
const stateUpdating = ref(false)
const order = ref(null)
const results = ref([])
const selectedResultId = ref(null)
const formRef = ref()
const dialog = reactive({
  visible: false,
  saving: false,
  form: { orid: null, orderid: orderId, title: '', content: '' }
})
const rules = { title: [{ required: true, message: '请输入结论标题', trigger: 'blur' }] }

async function loadDetail() {
  loading.value = true
  try {
    const [orderData, resultData] = await Promise.all([
      getOrdersById(orderId),
      listOverallResultByOrderId(orderId)
    ])
    order.value = orderData
    results.value = resultData || []
    selectedResultId.value = results.value[0]?.orid || null
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    loading.value = false
  }
}

function openEditor(item) {
  Object.assign(dialog.form, item
    ? { orid: item.orid, orderid: item.orderid, title: item.title, content: item.content || '' }
    : { orid: null, orderid: orderId, title: '', content: '' })
  dialog.visible = true
}

async function submitResult() {
  await formRef.value.validate()
  dialog.saving = true
  try {
    const action = dialog.form.orid ? updateOverallResult : saveOverallResult
    await action({ ...dialog.form })
    ElMessage.success('总检结论已保存')
    dialog.visible = false
    results.value = (await listOverallResultByOrderId(orderId)) || []
    selectedResultId.value = results.value[0]?.orid || null
  } catch (error) {
    ElMessage.error(error.message)
  } finally {
    dialog.saving = false
  }
}

async function removeItem(item) {
  try {
    await ElMessageBox.confirm(`确定删除“${item.title}”吗？`, '删除总检结论', { type: 'warning' })
    await removeOverallResult(item.orid)
    results.value = (await listOverallResultByOrderId(orderId)) || []
    selectedResultId.value = results.value[0]?.orid || null
    ElMessage.success('已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message)
  }
}

async function toggleArchive() {
  const nextState = order.value.state === 1 ? 2 : 1
  const actionText = nextState === 2 ? '归档后页面将进入只读状态' : '恢复后可继续编辑报告'
  try {
    await ElMessageBox.confirm(actionText, nextState === 2 ? '确认归档' : '确认恢复', { type: 'warning' })
    stateUpdating.value = true
    await updateOrdersState(orderId, nextState)
    order.value.state = nextState
    ElMessage.success(nextState === 2 ? '报告已归档' : '报告已恢复为未归档')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error.message)
  } finally {
    stateUpdating.value = false
  }
}

function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone || '-'
  return `${phone.slice(0, 3)}****${phone.slice(-4)}`
}

onMounted(loadDetail)
</script>
