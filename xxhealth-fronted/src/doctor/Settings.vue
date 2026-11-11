<template>
  <div class="settings-page" v-loading="loading">
    <section class="panel settings-banner">
      <span class="settings-banner-icon"><UiIcon name="Setting" /></span>
      <div>
        <h1>系统设置</h1>
        <p>管理个人信息与工作偏好，让健康服务更有序</p>
      </div>
      <span class="settings-saved"
        ><UiIcon name="CircleCheckFilled" />{{
          dirty ? "有未保存的修改" : "设置已同步"
        }}</span
      >
    </section>
    <div class="settings-columns">
      <aside class="panel settings-nav">
        <div class="settings-profile">
          <img :src="avatar('doctor')" alt="医生头像" /><strong>{{
            saved.profile?.doctorName || "张医生"
          }}</strong
          ><span
            >{{ saved.profile?.department }} · {{ saved.profile?.title }}</span
          >
        </div>
        <button
          v-for="t in tabs"
          :key="t.key"
          :class="{ active: tab === t.key }"
          @click="switchTab(t.key)"
        >
          <UiIcon :name="t.icon" />{{ t.label }}<UiIcon name="ArrowRight" />
        </button>
      </aside>
      <section class="panel settings-editor">
        <div class="settings-heading">
          <div>
            <h2>{{ currentTab.label }}</h2>
            <p>{{ currentTab.description }}</p>
          </div>
          <span class="pill green">医生工作空间</span>
        </div>
        <form v-if="tab !== 'logs'" @submit.prevent="save">
          <div v-if="tab === 'profile'" class="dialog-form settings-form">
            <label
              >医生姓名<input
                v-model="draft.doctorName"
                required
                maxlength="60" /></label
            ><label
              >所属科室<input
                v-model="draft.department"
                required
                maxlength="60" /></label
            ><label
              >职称<input
                v-model="draft.title"
                required
                maxlength="60" /></label
            ><label
              >所属机构<input
                v-model="draft.hospital"
                required
                maxlength="60" /></label
            ><label
              >联系电话<input
                v-model="draft.phone"
                pattern="1[3-9][0-9]{9}"
                placeholder="请输入11位手机号" /></label
            ><label
              >联系邮箱<input
                v-model="draft.email"
                type="email"
                placeholder="请输入联系邮箱" /></label
            ><label class="full"
              >个人简介<textarea
                v-model="draft.bio"
                rows="5"
                maxlength="1000"
              ></textarea>
            </label>
          </div>
          <div
            v-else-if="tab === 'preferences'"
            class="dialog-form settings-form"
          >
            <label
              >默认进入页面<select v-model="draft.landingPage">
                <option v-for="t in landingPages" :key="t.key" :value="t.key">
                  {{ t.label }}
                </option></select
              ><small>从系统首页进入时优先打开此页面</small></label
            ><label
              >患者列表默认每页条数<select v-model.number="draft.pageSize">
                <option :value="10">10条/页</option>
                <option :value="20">20条/页</option>
                <option :value="50">50条/页</option>
              </select></label
            ><label
              >日程默认视图<select v-model="draft.calendarView">
                <option value="day">日视图</option>
                <option value="week">周视图</option>
                <option value="month">月视图</option>
              </select></label
            ><label
              >新增预约默认诊室<input
                v-model="draft.defaultRoom"
                required
                maxlength="60"
            /></label>
            <div class="full settings-tip">
              <UiIcon
                name="InfoFilled"
              />默认视图和列表条数将在重新进入相应页面时生效。
            </div>
          </div>
          <div
            v-else-if="tab === 'notifications'"
            class="settings-notification-options"
          >
            <div v-for="n in notificationOptions" :key="n.key">
              <span class="notification-row-icon" :class="n.color"
                ><UiIcon :name="n.icon"
              /></span>
              <div>
                <strong>{{ n.label }}</strong>
                <p>{{ n.description }}</p>
              </div>
              <el-switch v-model="draft[n.key]" :aria-label="n.label" />
            </div>
            <p class="settings-tip">
              <UiIcon
                name="InfoFilled"
              />开关控制后续站内消息的生成；已有消息及异常处理记录会保留。
            </p>
          </div>
          <div v-else-if="tab === 'rules'" class="settings-form">
            <ClinicRules v-model="draft" />
          </div>
          <div class="settings-footer">
            <span>{{
              saved.updatedAt ? "上次更新：" + saved.updatedAt.slice(0, 16) : ""
            }}</span>
            <div>
              <button
                type="button"
                class="btn"
                :disabled="!dirty || saving"
                @click="reset"
              >
                撤销修改</button
              ><button class="btn primary" :disabled="saving || !dirty">
                <UiIcon name="Check" />{{ saving ? "保存中…" : "保存设置" }}
              </button>
            </div>
          </div>
        </form>
        <div v-else class="settings-logs">
          <table class="data-table">
            <thead>
              <tr>
                <th>操作时间</th>
                <th>操作人</th>
                <th>功能模块</th>
                <th>操作内容</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs" :key="log.id">
                <td>{{ log.createdAt?.slice(0, 19) }}</td>
                <td>{{ log.actor }}</td>
                <td>
                  <span class="pill blue">{{ log.module }}</span>
                </td>
                <td>{{ log.action }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="!logs.length" class="empty-state">暂无操作记录</div>
          <Pagination
            :total="logTotal"
            :page="logPage"
            :page-size="10"
            @change="
              logPage = $event;
              loadLogs();
            "
          />
        </div>
        <div v-if="error" class="empty-state">
          {{ error }}<button class="btn" @click="load">重新加载</button>
        </div>
      </section>
    </div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from "vue";
import { onBeforeRouteLeave } from "vue-router";
import UiIcon from "./UiIcon.vue";
import ClinicRules from "./ClinicRules.vue";
import Pagination from "./Pagination.vue";
import { api, run, confirm, avatar, loadSettings } from "./api";
const loading = ref(false),
  saving = ref(false),
  error = ref(""),
  tab = ref("profile"),
  saved = ref({}),
  draft = ref({}),
  logs = ref([]),
  logTotal = ref(0),
  logPage = ref(1);
const tabs = [
    {
      key: "profile",
      label: "个人信息",
      icon: "User",
      description: "完善医生档案，报告署名将使用您保存的医生姓名",
    },
    {
      key: "preferences",
      label: "工作偏好",
      icon: "Monitor",
      description: "设置常用页面、列表条数与日程视图",
    },
    {
      key: "notifications",
      label: "通知设置",
      icon: "Bell",
      description: "选择需要接收的站内通知类型",
    },
    {
      key: "rules",
      label: "体检与排班规则",
      icon: "Calendar",
      description: "统一管理预约容量、工作时间和预约须知",
    },
    {
      key: "logs",
      label: "操作记录",
      icon: "Document",
      description: "查看套餐、日程及设置的最近操作记录",
    },
  ],
  landingPages = [
    { key: "dashboard", label: "工作台" },
    { key: "appointments", label: "我的预约" },
    { key: "reports", label: "体检报告" },
    { key: "patients", label: "患者管理" },
    { key: "packages", label: "体检套餐" },
    { key: "schedule", label: "日程管理" },
    { key: "notifications", label: "消息通知" },
    { key: "settings", label: "系统设置" },
  ],
  notificationOptions = [
    {
      key: "appointment",
      label: "预约提醒",
      description: "接收新增预约、预约状态和日程调整通知",
      icon: "Calendar",
      color: "blue",
    },
    {
      key: "report",
      label: "报告通知",
      description: "体检报告审核通过后接收报告生成通知",
      icon: "Document",
      color: "green",
    },
    {
      key: "alert",
      label: "异常提醒",
      description: "报告中存在异常指标时接收提醒",
      icon: "WarningFilled",
      color: "red",
    },
    {
      key: "system",
      label: "系统通知",
      description: "接收系统相关的站内通知",
      icon: "Promotion",
      color: "blue",
    },
    {
      key: "other",
      label: "其他消息",
      description: "接收其他工作消息与服务提示",
      icon: "MoreFilled",
      color: "purple",
    },
  ];
const currentTab = computed(() => tabs.find((t) => t.key === tab.value)),
  dirty = computed(
    () =>
      tab.value !== "logs" &&
      !!saved.value[tab.value] &&
      JSON.stringify(draft.value) !== JSON.stringify(saved.value[tab.value])
  );
function reset() {
  draft.value = JSON.parse(JSON.stringify(saved.value[tab.value] || {}));
}
async function switchTab(key) {
  if (key === tab.value) return;
  if (dirty.value && !(await confirm("当前设置尚未保存，确认放弃修改并切换？")))
    return;
  tab.value = key;
  if (key === "logs") loadLogs();
  else reset();
}
async function load() {
  loading.value = true;
  error.value = "";
  try {
    saved.value = await api.get("/settings");
    reset();
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
async function save() {
  saving.value = true;
  try {
    saved.value = await run(
      () =>
        api.put("/settings/" + tab.value, {
          version: saved.value.version,
          values: draft.value,
        }),
      "设置已保存"
    );
    reset();
    await loadSettings();
  } catch {
  } finally {
    saving.value = false;
  }
}
async function loadLogs() {
  try {
    const r = await run(() =>
      api.get("/operation-logs", { page: logPage.value, pageSize: 10 })
    );
    logs.value = r.records;
    logTotal.value = r.total;
  } catch {}
}
onBeforeRouteLeave(
  async () =>
    !dirty.value || (await confirm("设置尚未保存，确认离开并放弃修改？"))
);
onMounted(load);
</script>
