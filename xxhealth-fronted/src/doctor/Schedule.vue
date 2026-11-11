<template>
  <div class="schedule-page" v-loading="loading">
    <div class="schedule-stats">
      <div v-for="s in statCards" :key="s.label" class="panel schedule-stat">
        <img :src="'/doctor-assets/schedule-' + s.asset + '.png'" alt="" />
        <div>
          <span>{{ s.label }}</span
          ><strong
            >{{ s.value || 0 }} <small>{{ s.unit }}</small></strong
          >
          <p v-if="s.key === 'alerts'" class="orange-text">需关注</p>
          <p v-else>
            较昨日
            <span :class="rate(s.key) >= 0 ? 'green-text' : 'red-text'">{{
              rate(s.key) === null
                ? "暂无数据"
                : (rate(s.key) >= 0 ? "↑ " : "↓ ") + Math.abs(rate(s.key)) + "%"
            }}</span>
          </p>
        </div>
      </div>
    </div>
    <div class="schedule-columns">
      <aside class="schedule-left">
        <section class="panel schedule-calendar">
          <h2>日历视图</h2>
          <div class="schedule-calendar-toolbar">
            <div>
              <button aria-label="上个月" @click="moveMonth(-1)">
                <UiIcon name="ArrowLeft" /></button
              ><button aria-label="下个月" @click="moveMonth(1)">
                <UiIcon name="ArrowRight" /></button
              ><strong>{{ monthYear }}年{{ monthIndex + 1 }}月</strong
              ><UiIcon name="Calendar" />
            </div>
            <div class="schedule-view-buttons">
              <button
                v-for="v in views"
                :key="v.key"
                :class="{ active: view === v.key }"
                @click="
                  view = v.key;
                  load();
                "
              >
                {{ v.label }}
              </button>
            </div>
          </div>
          <div class="schedule-month-grid">
            <span
              v-for="w in ['一', '二', '三', '四', '五', '六', '日']"
              :key="w"
              >{{ w }}</span
            ><button
              v-for="d in calendarDays"
              :key="d.date"
              :class="{ outside: !d.inMonth, active: d.date === selectedDate }"
              @click="selectDay(d.date)"
            >
              <b>{{ d.day }}</b
              ><i
                v-if="d.count"
                :class="{ completed: d.completed === d.count }"
              ></i>
            </button>
          </div>
          <div class="schedule-legend">
            <span><i></i>已预约</span><span><i class="orange"></i>进行中</span
            ><span><i class="green"></i>已完成</span>
          </div>
        </section>
        <section class="panel schedule-quick">
          <h2>快捷操作</h2>
          <div class="shortcut-grid">
            <button @click="newAppointment">
              <UiIcon name="Calendar" />新增预约</button
            ><button @click="openRules">
              <UiIcon name="UserFilled" />排班设置</button
            ><button
              @click="
                router.push({
                  path: '/doctor/appointments',
                  query: { date: selectedDate },
                })
              "
            >
              <UiIcon name="Document" />查看预约列表</button
            ><button @click="openBatch"><UiIcon name="Grid" />批量调整</button>
          </div>
        </section>
      </aside>
      <section class="panel schedule-center">
        <header>
          <button
            class="icon-btn"
            aria-label="前一日或周期"
            @click="movePeriod(-1)"
          >
            <UiIcon name="ArrowLeft" /></button
          ><button
            class="icon-btn"
            aria-label="后一日或周期"
            @click="movePeriod(1)"
          >
            <UiIcon name="ArrowRight" /></button
          ><strong>{{ dateTitle }}</strong
          ><button class="btn small" @click="selectDay(today())">今日</button
          ><select v-model="period" aria-label="日程时段">
            <option value="all">全天</option>
            <option value="morning">上午</option>
            <option value="afternoon">下午</option>
          </select>
        </header>
        <div v-if="error" class="empty-state">
          {{ error }}<button class="btn" @click="load">重试</button>
        </div>
        <div v-else-if="view === 'month'" class="schedule-month-view">
          <div
            v-for="d in calendarDays"
            :key="d.date"
            :class="{ outside: !d.inMonth, active: d.date === selectedDate }"
            @click="
              selectDay(d.date);
              view = 'day';
            "
          >
            <strong>{{ d.day }}</strong
            ><button
              v-for="e in data.events
                .filter((e) => e.scheduleDate === d.date)
                .slice(0, 3)"
              :key="e.id"
              @click.stop="editEvent(e)"
            >
              {{ e.startTime.slice(0, 5) }} {{ maskName(e.name) }}</button
            ><small v-if="d.count > 3">还有 {{ d.count - 3 }} 项</small>
          </div>
        </div>
        <div v-else class="schedule-timeline-scroll">
          <div v-if="view === 'week'" class="week-timeline-head">
            <span></span
            ><button
              v-for="d in weekDays"
              :key="d.date"
              @click="
                selectDay(d.date);
                view = 'day';
              "
            >
              <span>{{ d.label }}</span
              ><strong>{{ d.date.slice(5) }}</strong>
            </button>
          </div>
          <div
            class="schedule-timeline"
            :class="{ weekly: view === 'week' }"
            :style="{ height: times.length * 52 + 'px' }"
          >
            <div
              v-for="(t, i) in times"
              :key="t"
              class="timeline-rule"
              :style="{ top: i * 52 + 'px' }"
            >
              <span>{{ t }}</span
              ><i></i>
            </div>
            <div
              v-for="(d, col) in displayedDays"
              :key="d.date"
              class="timeline-day"
              :style="{
                left:
                  (view === 'week' ? 66 + (col * (centerWidth - 80)) / 7 : 76) +
                  'px',
                width:
                  view === 'week'
                    ? 'calc((100% - 80px) / 7)'
                    : 'calc(100% - 91px)',
              }"
            >
              <button
                v-for="e in visibleEvents.filter(
                  (e) => e.scheduleDate === d.date
                )"
                :key="e.id"
                class="schedule-event"
                :class="[
                  eventColor(e.status),
                  { compact: eventHeight(e) < 60 },
                ]"
                :style="{
                  top: timeY(e.startTime) + 10 + 'px',
                  height: eventHeight(e) + 'px',
                  ...eventLane(e),
                }"
                @click="editEvent(e)"
              >
                <img :src="avatar(e.avatar)" alt="" />
                <div>
                  <strong
                    >{{ maskName(e.name) }}
                    <span>{{ e.sex }}　{{ e.age }}岁</span></strong
                  >
                  <p>体检套餐：{{ e.packageName }}</p>
                </div>
                <span class="pill" :class="eventColor(e.status)">{{
                  e.status
                }}</span
                ><UiIcon name="ArrowRight" />
              </button>
            </div>
            <div v-if="!visibleEvents.length" class="timeline-empty">
              当日暂无安排
            </div>
          </div>
        </div>
      </section>
      <aside class="panel schedule-right">
        <div class="tabs">
          <button
            v-for="t in ['预约列表', '检查提醒', '异常提醒']"
            :key="t"
            :class="{ active: sideTab === t }"
            @click="sideTab = t"
          >
            {{ t }}
          </button>
        </div>
        <template v-if="sideTab !== '异常提醒'"
          ><button
            v-for="e in sideEvents"
            :key="e.id"
            class="schedule-side-event"
            @click="editEvent(e)"
          >
            <img :src="avatar(e.avatar)" alt="" />
            <div>
              <strong
                >{{ maskName(e.name) }}　{{ e.sex }}　{{ e.age }}岁</strong
              >
              <p>
                预约时间：{{ e.startTime.slice(0, 5) }}–{{
                  e.endTime.slice(0, 5)
                }}
              </p>
              <p>体检套餐：{{ e.packageName }}</p>
            </div>
            <span class="pill" :class="eventColor(e.status)">{{
              e.status
            }}</span>
          </button>
          <div v-if="!sideEvents.length" class="empty-state">
            {{ sideTab === "检查提醒" ? "暂无待检提醒" : "暂无预约安排" }}
          </div></template
        ><template v-else
          ><RouterLink
            v-for="a in data.alerts"
            :key="a.id"
            :to="'/doctor/reports/' + a.reportId"
            class="schedule-alert"
            ><UiIcon name="WarningFilled" />
            <div>
              <strong>{{ maskName(a.name) }} · {{ a.type }}</strong>
              <p>{{ a.indicator }}</p>
              <small>{{ a.createdAt?.slice(0, 16) }}</small>
            </div></RouterLink
          >
          <div v-if="!data.alerts?.length" class="empty-state">
            暂无未处理异常
          </div></template
        ><RouterLink
          :to="
            sideTab === '异常提醒'
              ? '/doctor/alerts'
              : '/doctor/appointments?date=' + selectedDate
          "
          class="schedule-more"
          >查看更多 <UiIcon name="ArrowRight"
        /></RouterLink>
      </aside>
    </div>
    <el-dialog v-model="editOpen" title="日程详情与调整" width="640px"
      ><form class="dialog-form" @submit.prevent="saveEvent">
        <div class="full schedule-dialog-person">
          <img :src="avatar(eventForm.avatar)" alt="" />
          <div>
            <h3>
              {{ maskName(eventForm.name) }} · {{ eventForm.sex }} ·
              {{ eventForm.age }}岁
            </h3>
            <p>{{ eventForm.packageName }}　{{ maskPhone(eventForm.phone) }}</p>
          </div>
        </div>
        <label
          >体检日期<input
            v-model="eventForm.scheduleDate"
            type="date"
            required
            :disabled="locked" /></label
        ><label
          >诊室<input
            v-model="eventForm.room"
            required
            maxlength="60"
            :disabled="locked" /></label
        ><label
          >开始时间<input
            v-model="eventForm.startTime"
            type="time"
            required
            :disabled="locked" /></label
        ><label
          >结束时间<input
            v-model="eventForm.endTime"
            type="time"
            required
            :disabled="locked" /></label
        ><label
          >体检状态<select v-model="eventForm.status" :disabled="locked">
            <option>已预约</option>
            <option>待检</option>
            <option>进行中</option>
            <option>已完成</option>
            <option>已取消</option>
          </select></label
        ><label class="full"
          >备注<textarea
            v-model="eventForm.notes"
            rows="3"
            maxlength="1000"
            :disabled="locked"
          ></textarea>
        </label>
        <div class="dialog-buttons full">
          <button type="button" class="btn" @click="editOpen = false">
            关闭</button
          ><button v-if="!locked" class="btn primary" :disabled="saving">
            保存调整
          </button>
        </div>
      </form></el-dialog
    >
    <el-dialog v-model="rulesOpen" title="排班设置" width="640px"
      ><form @submit.prevent="saveRules">
        <ClinicRules v-model="ruleForm" />
        <div class="dialog-buttons">
          <button type="button" class="btn" @click="rulesOpen = false">
            取消</button
          ><button class="btn primary" :disabled="saving">保存排班规则</button>
        </div>
      </form></el-dialog
    >
    <el-dialog v-model="batchOpen" title="批量调整日程" width="680px"
      ><form class="dialog-form" @submit.prevent="saveBatch">
        <div class="full schedule-batch-list">
          <label
            v-for="e in dayEvents.filter((e) =>
              ['已预约', '待检'].includes(e.status)
            )"
            :key="e.id"
            ><input v-model="batchIds" type="checkbox" :value="e.id" />{{
              e.startTime.slice(0, 5)
            }}
            {{ maskName(e.name) }} · {{ e.packageName }}</label
          >
          <p
            v-if="!dayEvents.some((e) => ['已预约', '待检'].includes(e.status))"
          >
            当日没有可调整的日程
          </p>
        </div>
        <label
          >调整到日期<input v-model="batchDate" type="date" required /></label
        ><label
          >时间整体偏移<select v-model.number="offsetMinutes">
            <option :value="0">保持原时段</option>
            <option :value="30">延后30分钟</option>
            <option :value="60">延后1小时</option>
            <option :value="-30">提前30分钟</option>
            <option :value="-60">提前1小时</option>
          </select></label
        >
        <p class="full muted">
          已完成和进行中的体检不可批量调整。遇到时段冲突或超出工作时间时，整批不会保存。
        </p>
        <div class="dialog-buttons full">
          <button type="button" class="btn" @click="batchOpen = false">
            取消</button
          ><button class="btn primary" :disabled="saving || !batchIds.length">
            保存批量调整
          </button>
        </div>
      </form></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import UiIcon from "./UiIcon.vue";
import ClinicRules from "./ClinicRules.vue";
import {
  api,
  run,
  confirm,
  avatar,
  maskName,
  maskPhone,
  today,
  workspaceSettings,
  loadSettings,
} from "./api";
const router = useRouter(),
  loading = ref(false),
  saving = ref(false),
  error = ref(""),
  selectedDate = ref(today()),
  view = ref(workspaceSettings.preferences.calendarView || "day"),
  period = ref("all"),
  sideTab = ref("预约列表"),
  data = ref({
    events: [],
    calendar: [],
    stats: {},
    previous: {},
    alerts: [],
    rules: {},
  }),
  monthYear = ref(new Date().getFullYear()),
  monthIndex = ref(new Date().getMonth()),
  centerWidth = ref(550);
const views = [
    { key: "day", label: "日" },
    { key: "week", label: "周" },
    { key: "month", label: "月" },
  ],
  formatDate = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(
      d.getDate()
    ).padStart(2, "0")}`,
  calendarDays = computed(() => {
    const first = new Date(monthYear.value, monthIndex.value, 1),
      offset = (first.getDay() + 6) % 7;
    return Array.from({ length: 42 }, (_, i) => {
      const d = new Date(monthYear.value, monthIndex.value, i - offset + 1),
        date = formatDate(d),
        mark = data.value.calendar.find((v) => v.date === date);
      return {
        date,
        day: d.getDate(),
        inMonth: d.getMonth() === monthIndex.value,
        count: mark?.count || 0,
        completed: mark?.completed || 0,
      };
    });
  }),
  weekDays = computed(() => {
    const d = new Date(selectedDate.value + "T00:00:00");
    d.setDate(d.getDate() - ((d.getDay() + 6) % 7));
    return Array.from({ length: 7 }, (_, i) => {
      const next = new Date(d);
      next.setDate(d.getDate() + i);
      return {
        date: formatDate(next),
        label: ["一", "二", "三", "四", "五", "六", "日"][i],
      };
    });
  }),
  displayedDays = computed(() =>
    view.value === "week" ? weekDays.value : [{ date: selectedDate.value }]
  );
const dayEvents = computed(() =>
    data.value.events.filter((e) => e.scheduleDate === selectedDate.value)
  ),
  sideEvents = computed(() =>
    dayEvents.value
      .filter(
        (e) =>
          sideTab.value !== "检查提醒" || ["已预约", "待检"].includes(e.status)
      )
      .slice(0, 5)
  ),
  visibleEvents = computed(() =>
    data.value.events.filter(
      (e) =>
        period.value === "all" ||
        (period.value === "morning"
          ? e.startTime < "12:00"
          : e.startTime >= "12:00")
    )
  );
const dateTitle = computed(() => {
  const d = new Date(selectedDate.value + "T00:00:00");
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日（${
    ["周日", "周一", "周二", "周三", "周四", "周五", "周六"][d.getDay()]
  }）`;
});
const statCards = computed(() => [
  {
    label: selectedDate.value === today() ? "今日预约" : "当日预约",
    value: data.value.stats.total,
    key: "total",
    asset: "total",
    unit: "人",
  },
  {
    label: "待检人数",
    value: data.value.stats.pending,
    key: "pending",
    asset: "pending",
    unit: "人",
  },
  {
    label: "已检人数",
    value: data.value.stats.completed,
    key: "completed",
    asset: "completed",
    unit: "人",
  },
  {
    label: "异常提醒",
    value: data.value.alerts.length,
    key: "alerts",
    asset: "alert",
    unit: "条",
  },
]);
function rate(key) {
  const before = Number(data.value.previous[key] || 0);
  return before
    ? Math.round(((Number(data.value.stats[key] || 0) - before) / before) * 100)
    : null;
}
const mins = (t) => Number(t.slice(0, 2)) * 60 + Number(t.slice(3, 5)),
  clock = (n) =>
    `${String(Math.floor(n / 60)).padStart(2, "0")}:${String(n % 60).padStart(
      2,
      "0"
    )}`;
const times = computed(() => {
  const r = data.value.rules,
    slots = [];
  if (!r.morningStart) return [];
  for (const [a, b] of [
    [r.morningStart, r.morningEnd],
    [r.afternoonStart, r.afternoonEnd],
  ])
    for (let t = mins(a); t <= mins(b); t += 30)
      if (
        period.value === "all" ||
        (period.value === "morning" && t < 13 * 60) ||
        (period.value === "afternoon" && t >= 13 * 60)
      )
        slots.push(clock(t));
  return [...new Set(slots)];
});
function timeY(t) {
  const value = mins(t),
    slots = times.value.map(mins);
  if (!slots.length) return 0;
  let index = slots.findIndex((v) => v > value);
  if (index === -1)
    return Math.max(
      0,
      (slots.length - 1 + (value - slots[slots.length - 1]) / 30) * 52
    );
  if (index === 0) return 0;
  return (
    (index -
      1 +
      (value - slots[index - 1]) /
        Math.max(30, slots[index] - slots[index - 1])) *
    52
  );
}
function eventHeight(e) {
  const next = visibleEvents.value
    .filter(
      (v) => v.scheduleDate === e.scheduleDate && v.startTime > e.startTime
    )
    .sort((a, b) => a.startTime.localeCompare(b.startTime))[0];
  const room = next ? timeY(next.startTime) - timeY(e.startTime) - 12 : 76;
  return Math.max(44, Math.min(76, room));
}

function eventLane(event) {
  if (view.value === "week") return {};
  const events = visibleEvents.value
      .filter((e) => e.scheduleDate === event.scheduleDate)
      .sort((a, b) => a.startTime.localeCompare(b.startTime) || a.id - b.id),
    ends = [],
    lanes = new Map();
  for (const e of events) {
    const start = timeY(e.startTime);
    let lane = ends.findIndex((end) => end <= start);
    if (lane < 0) lane = ends.length;
    ends[lane] = start + eventHeight(e) + 4;
    lanes.set(e.id, lane);
  }
  return ends.length > 1
    ? {
        left: (lanes.get(event.id) * 100) / ends.length + "%",
        right: "auto",
        width: "calc(" + 100 / ends.length + "% - 4px)",
      }
    : {};
}
const eventColor = (s) =>
  ({
    已完成: "green",
    进行中: "blue",
    待检: "orange",
    已预约: "blue",
    已取消: "neutral",
  }[s]);
async function load() {
  loading.value = true;
  error.value = "";
  try {
    data.value = await api.get("/schedule", {
      date: selectedDate.value,
      view: view.value,
    });
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
function selectDay(date) {
  selectedDate.value = date;
  const d = new Date(date + "T00:00:00");
  monthYear.value = d.getFullYear();
  monthIndex.value = d.getMonth();
  load();
}
function moveMonth(n) {
  const d = new Date(monthYear.value, monthIndex.value + n, 1);
  selectDay(formatDate(d));
}
function movePeriod(n) {
  const d = new Date(selectedDate.value + "T00:00:00");
  if (view.value === "month") d.setMonth(d.getMonth() + n);
  else d.setDate(d.getDate() + n * (view.value === "week" ? 7 : 1));
  selectDay(formatDate(d));
}
function newAppointment() {
  router.push({
    path: "/doctor/appointments",
    query: { new: "1", date: selectedDate.value, returnTo: "schedule" },
  });
}
const editOpen = ref(false),
  locked = ref(false),
  eventForm = reactive({}),
  rulesOpen = ref(false),
  ruleForm = ref({}),
  ruleVersion = ref(0),
  batchOpen = ref(false),
  batchDate = ref(today()),
  batchIds = ref([]),
  offsetMinutes = ref(0);
function editEvent(e) {
  Object.assign(eventForm, {
    ...e,
    startTime: e.startTime.slice(0, 5),
    endTime: e.endTime.slice(0, 5),
  });
  locked.value = ["已完成", "已取消"].includes(e.status);
  editOpen.value = true;
}
async function saveEvent() {
  if (
    eventForm.status === "已取消" &&
    !(await confirm("确认取消该预约及对应日程？"))
  )
    return;
  saving.value = true;
  try {
    await run(
      () => api.put("/schedule/" + eventForm.id, eventForm),
      "日程已更新"
    );
    editOpen.value = false;
    await load();
  } catch {
  } finally {
    saving.value = false;
  }
}
async function openRules() {
  try {
    const s = await run(() => api.get("/settings"));
    ruleForm.value = JSON.parse(JSON.stringify(s.rules));
    ruleVersion.value = s.version;
    rulesOpen.value = true;
  } catch {}
}
async function saveRules() {
  saving.value = true;
  try {
    await run(
      () =>
        api.put("/settings/rules", {
          version: ruleVersion.value,
          values: ruleForm.value,
        }),
      "排班规则已保存"
    );
    rulesOpen.value = false;
    await Promise.all([load(), loadSettings()]);
  } catch {
  } finally {
    saving.value = false;
  }
}
function openBatch() {
  batchDate.value = selectedDate.value;
  batchIds.value = [];
  offsetMinutes.value = 0;
  batchOpen.value = true;
}
async function saveBatch() {
  if (!(await confirm(`确认调整所选${batchIds.value.length}条日程？`))) return;
  saving.value = true;
  try {
    await run(
      () =>
        api.put("/schedule/batch", {
          events: dayEvents.value
            .filter((e) => batchIds.value.includes(e.id))
            .map((e) => ({ id: e.id, version: e.version })),
          targetDate: batchDate.value,
          offsetMinutes: offsetMinutes.value,
        }),
      "日程批量调整已保存"
    );
    batchOpen.value = false;
    selectDay(batchDate.value);
  } catch {
  } finally {
    saving.value = false;
  }
}
function measure() {
  centerWidth.value =
    document.querySelector(".schedule-center")?.clientWidth || 550;
}
onMounted(async () => {
  try {
    await loadSettings();
    view.value = workspaceSettings.preferences.calendarView || "day";
  } catch {}
  load();
  measure();
  window.addEventListener("resize", measure);
});
onUnmounted(() => window.removeEventListener("resize", measure));
</script>
