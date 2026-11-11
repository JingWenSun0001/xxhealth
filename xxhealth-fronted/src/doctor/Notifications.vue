<template>
  <div class="notifications-page" v-loading="loading">
    <section class="notification-summary">
      <div v-for="s in cards" :key="s.label" class="panel notification-stat">
        <img :src="'/doctor-assets/notice-' + s.asset + '.png'" alt="" />
        <div>
          <span>{{ s.label }}</span
          ><strong>{{ summary[s.key] || 0 }} <small>条</small></strong>
        </div>
      </div>
      <button
        class="btn"
        :disabled="!summary.unread || saving"
        @click="markAll"
      >
        <UiIcon name="Check" />全部已读
      </button>
    </section>
    <div class="notification-columns">
      <aside class="panel notification-categories">
        <h2>消息分类</h2>
        <button
          v-for="c in categories"
          :key="c.label"
          :class="{ active: category === c.value }"
          @click="
            category = c.value;
            search();
          "
        >
          <span class="notification-mini-icon" :class="c.color"
            ><UiIcon :name="c.icon" /></span
          ><span>{{ c.label }}</span
          ><b :class="{ 'red-text': c.value === '异常提醒' }">{{
            c.value
              ? summary.categories?.find((v) => v.category === c.value)
                  ?.count || 0
              : summary.total || 0
          }}</b>
        </button>
      </aside>
      <section class="panel notification-list-panel">
        <form class="notification-search" @submit.prevent="search">
          <div class="input-icon">
            <input
              v-model="keyword"
              placeholder="请输入关键词搜索消息…"
            /><button class="icon-btn" aria-label="搜索消息">
              <UiIcon name="Search" />
            </button>
          </div>
          <select v-model="isRead" aria-label="消息状态" @change="search">
            <option value="">全部状态</option>
            <option value="0">未读消息</option>
            <option value="1">已读消息</option></select
          ><select v-model="range" aria-label="消息时间范围" @change="search">
            <option value="3months">近三个月</option>
            <option value="month">近一个月</option>
            <option value="">全部时间</option>
          </select>
        </form>
        <div v-if="error" class="empty-state">
          {{ error }}<button class="btn" @click="load">重试</button>
        </div>
        <button
          v-for="n in records"
          :key="n.id"
          class="notification-row"
          @click="open(n)"
        >
          <span
            class="notification-row-icon"
            :class="categoryMeta(n.category).color"
            ><img :src="noticeIcon(n.category)" alt=""
          /></span>
          <div class="notification-copy">
            <strong>{{ n.title }}</strong>
            <p>{{ n.content }}</p>
          </div>
          <time>{{ n.createdAt?.slice(0, 16) }}</time
          ><span class="notification-read" :class="{ unread: !n.isRead }"
            ><i></i>{{ n.isRead ? "已读" : "未读" }}</span
          ><UiIcon name="ArrowRight" />
        </button>
        <div v-if="!records.length && !error" class="empty-state">
          <UiIcon name="ChatDotRound" />暂无符合条件的消息
        </div>
        <Pagination
          :total="total"
          :page="page"
          :page-size="pageSize"
          sizes
          @change="
            page = $event;
            load();
          "
          @size="
            pageSize = $event;
            search();
          "
        />
      </section>
    </div>
    <el-dialog
      v-model="detailOpen"
      :title="detail?.title || '消息详情'"
      width="660px"
      ><template v-if="detail"
        ><div class="notification-detail-meta">
          <span class="pill blue">{{ detail.category }}</span
          ><time>{{ detail.createdAt?.slice(0, 16) }}</time>
        </div>
        <p class="notification-detail-content">
          {{ detail.content }}
        </p></template
      ><template #footer
        ><button class="btn" @click="detailOpen = false">关闭</button
        ><button
          v-if="detail?.relatedType"
          class="btn primary"
          @click="goRelated"
        >
          {{ relatedLabel }}</button
        ><button v-if="detail?.isRead" class="btn" @click="markUnread">
          标记未读
        </button></template
      ></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import UiIcon from "./UiIcon.vue";
import Pagination from "./Pagination.vue";
import { api, run, confirm } from "./api";
const router = useRouter(),
  loading = ref(false),
  saving = ref(false),
  records = ref([]),
  total = ref(0),
  page = ref(1),
  pageSize = ref(6),
  summary = ref({}),
  category = ref(""),
  keyword = ref(""),
  isRead = ref(""),
  range = ref("3months"),
  error = ref(""),
  detailOpen = ref(false),
  detail = ref(null);
const cards = [
    { label: "全部消息", key: "total", asset: "total" },
    { label: "未读消息", key: "unread", asset: "unread" },
    { label: "已读消息", key: "read", asset: "read" },
  ],
  categories = [
    { label: "全部消息", value: "", icon: "ChatDotRound", color: "blue" },
    { label: "预约提醒", value: "预约提醒", icon: "Calendar", color: "blue" },
    { label: "报告通知", value: "报告通知", icon: "Document", color: "green" },
    {
      label: "异常提醒",
      value: "异常提醒",
      icon: "WarningFilled",
      color: "red",
    },
    { label: "系统通知", value: "系统通知", icon: "Promotion", color: "blue" },
    { label: "其他消息", value: "其他消息", icon: "MoreFilled", color: "blue" },
  ];
const noticeIcon = (category) =>
  "/doctor-assets/notification-" +
  ({
    预约提醒: "appointment",
    报告通知: "report",
    异常提醒: "alert",
    系统通知: "system",
    其他消息: "other",
  }[category] || "other") +
  ".png";
const categoryMeta = (value) =>
    categories.find((c) => c.value === value) || categories[0],
  relatedLabel = computed(
    () =>
      ({ appointment: "查看预约", report: "查看报告", alert: "查看异常提醒" }[
        detail.value?.relatedType
      ] || "查看详情")
  );
async function load() {
  loading.value = true;
  error.value = "";
  try {
    const d = await api.get("/notifications", {
      category: category.value,
      keyword: keyword.value,
      isRead: isRead.value,
      range: range.value,
      page: page.value,
      pageSize: pageSize.value,
    });
    records.value = d.records;
    total.value = d.total;
    summary.value = d.summary;
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
async function open(n) {
  try {
    detail.value = { ...n };
    if (!n.isRead) {
      await run(() => api.put(`/notifications/${n.id}/read`, { read: true }));
      detail.value.isRead = 1;
      await load();
    }
    detailOpen.value = true;
  } catch {}
}
async function markAll() {
  if (!(await confirm("确认将全部未读消息标记为已读？"))) return;
  saving.value = true;
  try {
    await run(
      () => api.put("/notifications/read-all", {}),
      "全部消息已标记为已读"
    );
    await load();
  } catch {
  } finally {
    saving.value = false;
  }
}
async function markUnread() {
  try {
    await run(
      () => api.put(`/notifications/${detail.value.id}/read`, { read: false }),
      "已标记为未读"
    );
    detailOpen.value = false;
    await load();
  } catch {}
}
function goRelated() {
  detailOpen.value = false;
  const n = detail.value;
  if (n.relatedType === "report") router.push("/doctor/reports/" + n.relatedId);
  else if (n.relatedType === "alert") router.push("/doctor/alerts");
  else
    router.push({
      path: "/doctor/appointments",
      query: { appointmentId: n.relatedId },
    });
}
onMounted(load);
</script>
