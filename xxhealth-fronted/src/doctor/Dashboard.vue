<template>
  <div class="dashboard-page" v-loading="loading">
    <div class="dashboard-hero">
      <div class="welcome">
        <img src="/doctor-assets/welcome.png" alt="医生" />
        <div>
          <h2>{{data.doctor||'张医生'}}，您好！</h2>
          <p>今天是 {{ dateLabel }}，<br />祝您工作顺利！</p>
        </div>
      </div>
      <div
        v-for="s in cards"
        :key="s.label"
        class="dashboard-stat"
        @click="router.push(s.path)"
      >
        <span class="round-icon" :class="s.color"
          ><UiIcon :name="s.icon" /></span
        ><span>{{ s.label }}</span
        ><strong>{{ Number(s.value || 0).toLocaleString() }}</strong>
      </div>
    </div>
    <div class="dashboard-grid">
      <section class="panel task-panel">
        <div class="panel-heading">
          <h3>待处理任务</h3>
          <RouterLink to="/doctor/reports?status=草稿">查看全部 ›</RouterLink>
        </div>
        <div v-for="t in data.tasks" :key="t.id" class="task-row">
          <span
            class="pill"
            :class="t.status === '草稿' ? 'orange' : 'green'"
            >{{ t.status === "草稿" ? "待编辑" : "待审核" }}</span
          >
          <div class="task-copy">
            <strong
              >{{ maskName(t.name) }}　{{ t.sex }}　{{ t.age }}岁　
              <small>体检套餐：{{ t.packageName }}</small></strong
            >
            <p>体检时间：{{ t.examDate }} 09:00</p>
          </div>
          <RouterLink :to="'/doctor/reports/' + t.id + '/edit'" class="btn soft"
            >去处理</RouterLink
          >
        </div>
        <div v-if="!data.tasks?.length" class="empty-state">
          待处理任务已完成
        </div>
      </section>
      <section class="panel shortcut-panel">
        <div class="panel-heading"><h3>快捷入口</h3></div>
        <div class="shortcut-grid">
          <RouterLink
            v-for="q in quick"
            :key="q.label"
            :to="q.path"
            :class="q.color"
            ><UiIcon :name="q.icon" /><strong>{{ q.label }}</strong></RouterLink
          >
        </div>
      </section>
      <section class="panel trend-panel">
        <div class="panel-heading">
          <h3>近一周工作量趋势</h3>
          <div class="legend"><i></i>报告数 <i class="gray"></i>异常数</div>
        </div>
        <div class="trend-chart">
          <div class="chart-y">
            <span>{{ chartMax }}</span
            ><span>{{ Math.ceil(chartMax / 2) }}</span
            ><span>0</span>
          </div>
          <svg
            viewBox="0 0 700 150"
            preserveAspectRatio="none"
            role="img"
            aria-label="近七天报告数量趋势"
          >
            <defs>
              <linearGradient id="area" x1="0" y1="0" x2="0" y2="1">
                <stop stop-color="#79b9ff" stop-opacity=".45" />
                <stop offset="1" stop-color="#79b9ff" stop-opacity=".03" />
              </linearGradient>
            </defs>
            <path
              d="M0 20H700M0 75H700M0 130H700"
              stroke="#e1eaff"
              fill="none"
            />
            <path :d="area" fill="url(#area)" />
            <polyline
              :points="points"
              fill="none"
              stroke="#1677ff"
              stroke-width="3"
            />
            <polyline
              :points="abnormalPoints"
              fill="none"
              stroke="#a3abc7"
              stroke-width="2"
            />
            <circle
              v-for="(v, i) in trendValues"
              :key="i"
              :cx="i * 116.6"
              :cy="130 - (v / chartMax) * 110"
              r="3"
              fill="#1677ff"
            />
          </svg>
          <div class="chart-labels">
            <span v-for="d in days" :key="d.date">{{ d.label }}</span>
          </div>
        </div>
      </section>
      <section class="panel notice-panel">
        <div class="panel-heading">
          <h3>系统公告</h3>
          <RouterLink to="/doctor/alerts">更多 ›</RouterLink>
        </div>
        <RouterLink class="notice-row" to="/doctor/alerts"
          ><span class="round-icon orange"><UiIcon name="WarningFilled" /></span
          >体检报告异常提醒
          <small>{{ data.alerts?.length || 0 }} 条</small></RouterLink
        >
        <div class="notice-row">
          <span class="round-icon blue"><UiIcon name="Lock" /></span
          >关于数据安全的通知 <small>06–12</small>
        </div>
        <p class="notice-caption">请及时处理异常指标，持续关注患者健康。</p>
      </section>
    </div>
    <div v-if="error" class="panel empty-state">
      {{ error }}<button class="btn" @click="load">重新加载</button>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import UiIcon from "./UiIcon.vue";
import { api, maskName, today } from "./api";
const router = useRouter(),
  data = ref({ stats: {}, tasks: [], trend: [] }),
  loading = ref(false),
  error = ref("");
const dateLabel = computed(() =>
  new Date((data.value.date || today()) + "T00:00:00").toLocaleDateString(
    "zh-CN",
    { year: "numeric", month: "long", day: "numeric" }
  )
);
const cards = computed(() => [
  {
    label: "待审核报告",
    value: data.value.stats.pendingReports,
    icon: "DocumentChecked",
    color: "cyan",
    path: "/doctor/reports?status=待审核",
  },
  {
    label: "今日预约",
    value: data.value.stats.todayAppointments,
    icon: "Calendar",
    color: "green",
    path: "/doctor/appointments",
  },
  {
    label: "患者总数",
    value: data.value.stats.patients,
    icon: "UserFilled",
    color: "purple",
    path: "/doctor/patients",
  },
]);
const quick = [
  {
    label: "新建报告",
    path: "/doctor/reports/new",
    color: "green",
    icon: "DocumentAdd",
  },
  {
    label: "报告模板",
    path: "/doctor/templates",
    color: "purple",
    icon: "Tickets",
  },
  { label: "历史报告", path: "/doctor/history", color: "cyan", icon: "Clock" },
  {
    label: "异常提醒",
    path: "/doctor/alerts",
    color: "red",
    icon: "BellFilled",
  },
];
const days = computed(() =>
  Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - 6 + i);
    return {
      date: `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(
        2,
        "0"
      )}-${String(d.getDate()).padStart(2, "0")}`,
      label: ["周日", "周一", "周二", "周三", "周四", "周五", "周六"][
        d.getDay()
      ],
    };
  })
);
const trendValues = computed(() =>
  days.value.map((d) =>
    Number(data.value.trend.find((t) => t.date === d.date)?.reports || 0)
  )
);
const chartMax = computed(() => Math.max(5, ...trendValues.value)),
  points = computed(() =>
    trendValues.value
      .map((v, i) => `${i * 116.6},${130 - (v / chartMax.value) * 110}`)
      .join(" ")
  ),
  abnormalPoints = computed(() =>
    days.value
      .map(
        (d, i) =>
          `${i * 116.6},${
            130 -
            (Number(
              data.value.trend.find((t) => t.date === d.date)?.abnormal || 0
            ) /
              chartMax.value) *
              110
          }`
      )
      .join(" ")
  ),
  area = computed(
    () => `M0,130 L${points.value.replaceAll(" ", " L")} L700,130 Z`
  );
async function load() {
  loading.value = true;
  try {
    data.value = await api.get("/dashboard");
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
onMounted(load);
</script>
