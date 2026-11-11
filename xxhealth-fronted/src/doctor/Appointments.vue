<template>
  <div class="appointments-page" v-loading="loading">
    <div class="appointment-hero">
      <img
        class="calendar-art"
        src="/doctor-assets/calendar.png"
        alt="日历和时钟"
      />
      <div class="appointment-title">
        <h1>我的预约</h1>
        <p>管理您的体检预约，轻松安排，<br />健康无忧</p>
      </div>
      <div v-for="s in cards" :key="s.label" class="appointment-stat">
        <UiIcon :name="s.icon" :class="s.color" />
        <div>
          <span>{{ s.label }}</span
          ><strong>{{ s.value || 0 }} <small>人</small></strong>
        </div>
      </div>
    </div>
    <div class="appointment-tabs">
      <button
        v-for="t in tabs"
        :key="t.status"
        :class="{ active: status === t.status }"
        @click="
          status = t.status;
          search();
        "
      >
        {{ t.label }} <span>{{ stats[t.count] || 0 }}</span>
      </button>
    </div>
    <div class="appointment-columns">
      <aside class="calendar-column">
        <section class="panel calendar-panel">
          <div class="panel-heading">
            <h3>预约日期</h3>
            <button class="icon-btn" aria-label="返回本月" @click="goToday">
              <UiIcon name="DArrowRight" />
            </button>
          </div>
          <div class="calendar-title">
            <button @click="shiftMonth(-1)" aria-label="上个月">
              <UiIcon name="ArrowLeft" /></button
            ><strong>{{ year }}年{{ month + 1 }}月</strong
            ><button @click="shiftMonth(1)" aria-label="下个月">
              <UiIcon name="ArrowRight" />
            </button>
          </div>
          <div class="calendar-grid">
            <span
              v-for="d in ['一', '二', '三', '四', '五', '六', '日']"
              :key="d"
              class="weekday"
              >{{ d }}</span
            ><button
              v-for="day in calendarDays"
              :key="day.date"
              :class="{
                outside: !day.inMonth,
                chosen: selectedDate === day.date,
                today: day.date === today(),
              }"
              @click="selectDate(day.date)"
            >
              <span>{{ day.number }}</span
              ><i v-if="day.count"></i>
            </button>
          </div>
          <button
            v-if="selectedDate"
            class="text-btn calendar-clear"
            @click="
              selectedDate = '';
              search();
            "
          >
            清除日期筛选 · {{ selectedDate }}
          </button>
        </section>
        <section class="panel quick-appointment">
          <div class="panel-heading"><h3>快捷操作</h3></div>
          <div class="shortcut-grid">
            <button @click="openNew"><UiIcon name="Calendar" />新增预约</button
            ><button @click="nameInput.focus()">
              <UiIcon name="Search" />预约查询</button
            ><button
              @click="
                status = '';
                selectedDate = '';
                search();
              "
            >
              <UiIcon name="Document" />预约管理</button
            ><button @click="supportOpen = true">
              <UiIcon name="Service" />联系客服
            </button>
          </div>
        </section>
      </aside>
      <section class="panel appointment-list-panel">
        <form class="appointment-search" @submit.prevent="search()">
          <label
            >患者姓名<input
              ref="nameInput"
              v-model="filters.name"
              placeholder="请输入患者姓名" /></label
          ><label
            >手机号<input
              v-model="filters.phone"
              placeholder="请输入手机号" /></label
          ><label
            >预约类型<select v-model="filters.packageName">
              <option value="">全部</option>
              <option v-for="p in packages" :key="p">{{ p }}</option>
            </select></label
          ><label
            >状态<select v-model="status">
              <option value="">全部</option>
              <option
                v-for="s in ['待确认', '已确认', '已完成', '已取消']"
                :key="s"
              >
                {{ s }}
              </option>
            </select></label
          ><button class="btn primary"><UiIcon name="Search" />查询</button
          ><button class="btn" type="button" @click="reset">
            <UiIcon name="Refresh" />重置
          </button>
        </form>
        <div v-if="error" class="empty-state">
          {{ error }}<button class="btn" @click="load">重试</button>
        </div>
        <div v-else-if="!records.length" class="empty-state">
          <UiIcon name="Calendar" />暂无符合条件的预约
        </div>
        <article v-for="a in records" :key="a.id" class="appointment-card">
          <img
            class="appointment-avatar"
            :src="avatar(a.avatar)"
            alt="患者头像"
          />
          <div class="appointment-copy">
            <div class="appointment-person">
              <strong>{{ maskName(a.name) }}</strong
              ><span>{{ a.sex }}</span
              ><span>{{ a.age }}岁</span><span>ID：{{ a.appointmentNo }}</span
              ><span class="pill" :class="statusColor(a.status)">{{
                a.status
              }}</span>
            </div>
            <p><UiIcon name="PhoneFilled" />{{ maskPhone(a.phone) }}</p>
            <p>
              <UiIcon name="Calendar" />预约时间：{{ a.examDate }}（{{
                weekDay(a.examDate)
              }}） {{ a.timeSlot }}
            </p>
            <p><UiIcon name="Memo" />套餐：{{ a.packageName }}</p>
          </div>
          <div class="appointment-source">
            <template v-if="a.status === '已完成'"
              ><span>体检报告</span><strong>查看体检结果</strong
              ><UiIcon name="Document" /></template
            ><template v-else
              ><span>预约来源</span><strong>{{ a.source }}</strong></template
            >
          </div>
          <div class="appointment-actions">
            <button
              v-if="a.status === '待确认'"
              class="btn primary"
              @click="changeStatus(a, '已确认')"
            >
              确认预约</button
            ><button
              v-if="a.status === '已确认'"
              class="btn"
              @click="
                view = a;
                detailOpen = true;
              "
            >
              查看详情</button
            ><button
              v-if="['待确认', '已确认'].includes(a.status)"
              class="btn"
              @click="changeStatus(a, '已取消')"
            >
              取消预约</button
            ><button
              v-if="a.status === '已完成'"
              class="btn"
              @click="viewReport(a)"
            >
              查看报告</button
            ><button
              v-if="a.status === '已取消'"
              class="btn"
              @click="
                view = a;
                detailOpen = true;
              "
            >
              查看详情
            </button>
          </div>
        </article>
        <Pagination
          :total="total"
          :page="page"
          :page-size="3"
          @change="
            page = $event;
            load();
          "
        />
      </section>
    </div>
    <el-dialog v-model="newOpen" title="新增预约" width="560px"
      ><form class="dialog-form" @submit.prevent="save">
        <label class="full"
          >选择患者<select v-model="form.patientId" required>
            <option value="">请选择患者</option>
            <option v-for="p in patients" :value="p.id" :key="p.id">
              {{ p.name }} · {{ p.sex }} · {{ maskPhone(p.phone) }}
            </option>
          </select></label
        ><label
          >体检日期<input
            v-model="form.examDate"
            type="date"
            required
            :min="today()" /></label
        ><label
          >预约时段<select v-model="form.timeSlot">
            <option v-for="slot in bookingSlots" :key="slot">{{ slot }}</option>
          </select></label
        ><label class="full"
          >体检套餐<select v-model="form.packageName">
            <option v-for="p in packages" :key="p">{{ p }}</option>
          </select></label
        >
        <div class="dialog-buttons full">
          <button type="button" class="btn" @click="newOpen = false">
            取消</button
          ><button class="btn primary" :disabled="saving">保存预约</button>
        </div>
      </form></el-dialog
    >
    <el-dialog v-model="detailOpen" title="预约详情" width="560px"
      ><dl v-if="view" class="detail-list">
        <dt>患者</dt>
        <dd>{{ view.name }} · {{ view.sex }} · {{ view.age }}岁</dd>
        <dt>预约编号</dt>
        <dd>{{ view.appointmentNo }}</dd>
        <dt>体检时间</dt>
        <dd>{{ view.examDate }} {{ view.timeSlot }}</dd>
        <dt>体检套餐</dt>
        <dd>{{ view.packageName }}</dd>
        <dt>预约来源</dt>
        <dd>{{ view.source }}</dd>
        <dt>当前状态</dt>
        <dd>{{ view.status }}</dd>
      </dl>
      <template #footer
        ><button
          v-if="view?.status === '已确认'"
          class="btn primary"
          @click="changeStatus(view, '已完成')"
        >
          标记体检完成
        </button></template
      ></el-dialog
    >
    <el-dialog v-model="supportOpen" title="联系客服" width="420px"
      ><p>如需协助，请联系体检中心服务台。</p>
      <p class="support-phone">024-88880001</p>
      <p>服务时间：周一至周日 07:30–17:00</p></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import UiIcon from "./UiIcon.vue";
import Pagination from "./Pagination.vue";
import {
  api,
  run,
  confirm,
  avatar,
  maskName,
  maskPhone,
  packages,
  today,
} from "./api";
const router = useRouter(),
  route = useRoute(),
  records = ref([]),
  total = ref(0),
  stats = ref({}),
  page = ref(1),
  status = ref(""),
  loading = ref(false),
  error = ref(""),
  selectedDate = ref(""),
  nameInput = ref(null);
const filters = reactive({ name: "", phone: "", packageName: "" }),
  year = ref(2025),
  month = ref(5),
  calendarData = ref([]);
const cards = computed(() => [
  {
    label: "今日预约",
    value: stats.value.today,
    icon: "Calendar",
    color: "blue",
  },
  { label: "本周预约", value: stats.value.week, icon: "Clock", color: "cyan" },
  {
    label: "本月预约",
    value: stats.value.month,
    icon: "Checked",
    color: "green",
  },
]);
const tabs = [
  { label: "全部预约", status: "", count: "total" },
  { label: "待确认", status: "待确认", count: "pending" },
  { label: "已确认", status: "已确认", count: "confirmed" },
  { label: "已完成", status: "已完成", count: "completed" },
  { label: "已取消", status: "已取消", count: "cancelled" },
];
const statusColor = (s) =>
  ({ 待确认: "blue", 已确认: "green", 已完成: "purple", 已取消: "neutral" }[s]);
const dateStr = (d) =>
  `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(
    d.getDate()
  ).padStart(2, "0")}`;
const calendarDays = computed(() => {
  const start = new Date(year.value, month.value, 1),
    offset = (start.getDay() + 6) % 7;
  return Array.from({ length: 42 }, (_, i) => {
    const d = new Date(year.value, month.value, i - offset + 1),
      date = dateStr(d);
    return {
      date,
      number: d.getDate(),
      inMonth: d.getMonth() === month.value,
      count: calendarData.value.find((v) => v.date === date)?.count,
    };
  });
});
const weekDay = (s) =>
  ["周日", "周一", "周二", "周三", "周四", "周五", "周六"][
    new Date(s + "T00:00:00").getDay()
  ];
async function loadCalendar() {
  try {
    calendarData.value = await api.get("/calendar", {
      month: `${year.value}-${String(month.value + 1).padStart(2, "0")}`,
    });
  } catch {}
}
function shiftMonth(step) {
  const d = new Date(year.value, month.value + step, 1);
  year.value = d.getFullYear();
  month.value = d.getMonth();
  loadCalendar();
}
function goToday() {
  const d = new Date();
  year.value = d.getFullYear();
  month.value = d.getMonth();
  loadCalendar();
}
function selectDate(d) {
  selectedDate.value = selectedDate.value === d ? "" : d;
  search();
}
async function load() {
  loading.value = true;
  try {
    const d = await api.get("/appointments", {
      ...filters,
      status: status.value,
      date: selectedDate.value,
      page: page.value,
      pageSize: 3,
    });
    records.value = d.records;
    total.value = d.total;
    stats.value = d.stats;
    error.value = "";
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
function search() {
  page.value = 1;
  load();
}
function reset() {
  Object.assign(filters, { name: "", phone: "", packageName: "" });
  status.value = "";
  selectedDate.value = "";
  search();
}
async function changeStatus(a, to) {
  if (!(await confirm(`确认将该预约设为“${to}”？`))) return;
  try {
    await run(
      () => api.put(`/appointments/${a.id}/status`, { status: to }),
      "预约状态已更新"
    );
    detailOpen.value = false;
    await load();
    loadCalendar();
  } catch {}
}
const bookingRules = ref({
  morningStart: "08:00",
  morningEnd: "12:00",
  afternoonStart: "13:30",
  afternoonEnd: "17:00",
  slotMinutes: 30,
});
const bookingSlots = computed(() => {
  const r = bookingRules.value,
    out = [];
  const minute = (t) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5)),
    clock = (n) =>
      String(Math.floor(n / 60)).padStart(2, "0") +
      ":" +
      String(n % 60).padStart(2, "0");
  for (const [start, end] of [
    [r.morningStart, r.morningEnd],
    [r.afternoonStart, r.afternoonEnd],
  ])
    for (
      let m = minute(start);
      m + Number(r.slotMinutes) <= minute(end);
      m += Number(r.slotMinutes)
    )
      out.push(clock(m) + "–" + clock(m + Number(r.slotMinutes)));
  return out;
});
const newOpen = ref(false),
  saving = ref(false),
  patients = ref([]),
  detailOpen = ref(false),
  view = ref(null),
  supportOpen = ref(false),
  form = reactive({
    patientId: "",
    examDate: today(),
    timeSlot: "09:00–10:00",
    packageName: packages[0],
  });
async function openNew() {
  try {
    bookingRules.value = (await api.get("/settings")).rules;
    if (!bookingSlots.value.includes(form.timeSlot))
      form.timeSlot = bookingSlots.value[0] || "";
    patients.value = (
      await run(() => api.get("/patients", { pageSize: 500 }))
    ).records;
    newOpen.value = true;
  } catch {}
}
async function save() {
  saving.value = true;
  try {
    await run(() => api.post("/appointments", form), "预约已新增");
    newOpen.value = false;
    if (route.query.returnTo === "schedule") {
      router.push("/doctor/schedule");
      return;
    }
    await load();
    loadCalendar();
  } catch {
  } finally {
    saving.value = false;
  }
}
async function viewReport(a) {
  try {
    const d = await run(() => api.get("/reports", { patientId: a.patientId }));
    if (d.records.length) router.push("/doctor/reports/" + d.records[0].id);
    else ElMessage.info("该患者暂未生成报告");
  } catch {}
}
onMounted(async () => {
  if (route.query.date) {
    selectedDate.value = String(route.query.date);
    const d = new Date(selectedDate.value + "T00:00:00");
    year.value = d.getFullYear();
    month.value = d.getMonth();
  }
  if (route.query.appointmentId) {
    const a = await api.get("/appointments/" + route.query.appointmentId);
    if (a) {
      view.value = a;
      detailOpen.value = true;
      filters.name = a.name;
    }
  }
  load();
  loadCalendar();
  if (route.query.new === "1") {
    try {
      if (route.query.packageId) {
        const p = await run(() =>
          api.get("/packages/" + route.query.packageId)
        );
        if (!p.enabled) throw new Error("套餐已下架");
        form.packageName = p.name;
      }
      if (route.query.date) form.examDate = String(route.query.date);
      await openNew();
    } catch (e) {
      ElMessage.error(e.message);
    }
  }
});
</script>
