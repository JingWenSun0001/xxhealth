<template>
  <div class="panel alerts-page" v-loading="loading">
    <div class="alerts-toolbar">
      <div class="tabs">
        <button
          v-for="t in ['全部', '血糖异常', '血脂异常', '体检异常']"
          :key="t"
          :class="{ active: type === t }"
          @click="
            type = t;
            search();
          "
        >
          {{ t }} ({{
            t === "全部" ? all.length : all.filter((a) => a.type === t).length
          }})
        </button>
      </div>
      <form class="input-icon" @submit.prevent="search">
        <input v-model="name" placeholder="搜索患者姓名…" /><button
          class="icon-btn"
          aria-label="搜索"
        >
          <UiIcon name="Search" />
        </button>
      </form>
    </div>
    <table class="data-table">
      <thead>
        <tr>
          <th>提醒类型</th>
          <th>患者姓名</th>
          <th>年龄</th>
          <th>异常指标</th>
          <th>提醒时间</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="a in records" :key="a.id">
          <td>
            <span class="pill" :class="a.type === '血脂异常' ? 'orange' : 'red'"
              ><UiIcon name="WarningFilled" />{{ a.type }}</span
            >
          </td>
          <td>{{ maskName(a.name) }}</td>
          <td>{{ a.age }}岁</td>
          <td>{{ a.indicator }}</td>
          <td>{{ a.createdAt?.slice(0, 16) }}</td>
          <td>
            <span class="pill" :class="a.resolved ? 'green' : 'red'">{{
              a.resolved ? "已处理" : "待处理"
            }}</span>
          </td>
          <td>
            <div class="row-actions">
              <RouterLink :to="'/doctor/reports/' + a.reportId">查看</RouterLink
              ><button v-if="!a.resolved" @click="resolve(a)">标记处理</button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="error || !records.length" class="empty-state">
      {{ error || "暂无异常提醒" }}
    </div>
    <Pagination
      :total="total"
      :page="page"
      :page-size="10"
      @change="
        page = $event;
        load();
      "
    />
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue";
import UiIcon from "./UiIcon.vue";
import Pagination from "./Pagination.vue";
import { api, run, confirm, maskName } from "./api";
const records = ref([]),
  all = ref([]),
  total = ref(0),
  page = ref(1),
  type = ref("全部"),
  name = ref(""),
  loading = ref(false),
  error = ref("");
async function load() {
  loading.value = true;
  try {
    const d = await api.get("/alerts", {
      type: type.value === "全部" ? "" : type.value,
      name: name.value,
      page: page.value,
    });
    records.value = d.records;
    total.value = d.total;
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
async function resolve(a) {
  if (!(await confirm("确认已查看并处理该异常提醒？"))) return;
  try {
    await run(() => api.put("/alerts/" + a.id, {}), "已标记处理");
    await load();
  } catch {}
}
onMounted(async () => {
  load();
  try {
    all.value = (await api.get("/alerts", { pageSize: 500 })).records;
  } catch {}
});
</script>
