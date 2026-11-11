<template>
  <div class="panel reports-list-page" v-loading="loading">
    <div class="panel-heading">
      <h3>{{ route.meta.title }}</h3>
      <RouterLink to="/doctor/reports/new" class="btn primary"
        ><UiIcon name="Plus" />新建报告</RouterLink
      >
    </div>
    <form class="reports-search" @submit.prevent="search">
      <label
        >患者姓名：<input
          v-model="filters.name"
          placeholder="请输入患者姓名" /></label
      ><label
        >报告类型：<select v-model="filters.packageName">
          <option value="">全部</option>
          <option v-for="p in packages" :key="p">{{ p }}</option>
          <option v-for="p in packages" :key="p + '报告'">
            {{ p.replace("套餐", "报告") }}
          </option>
        </select></label
      ><label
        >报告状态：<select v-model="filters.status">
          <option value="">全部</option>
          <option>草稿</option>
          <option>待审核</option>
          <option>已生成</option>
        </select></label
      ><label
        >时间范围：<select v-model="filters.range">
          <option value="">全部时间</option>
          <option value="year">近一年</option>
        </select></label
      ><button class="btn primary"><UiIcon name="Search" />查询</button
      ><button class="btn" type="button" @click="reset">重置</button>
    </form>
    <div class="table-scroll">
      <table class="data-table">
        <thead>
          <tr>
            <th>报告编号</th>
            <th>患者姓名</th>
            <th>性别</th>
            <th>年龄</th>
            <th>体检日期</th>
            <th>报告类型</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.id">
            <td class="blue-text">{{ r.reportNo }}</td>
            <td>{{ maskName(r.name) }}</td>
            <td>{{ r.sex }}</td>
            <td>{{ r.age }}</td>
            <td>{{ r.examDate }}</td>
            <td>{{ r.packageName }}</td>
            <td>
              <span
                class="pill"
                :class="
                  r.status === '已生成'
                    ? 'green'
                    : r.status === '草稿'
                    ? 'orange'
                    : 'blue'
                "
                >{{ r.status }}</span
              >
            </td>
            <td>
              <div class="row-actions">
                <RouterLink :to="'/doctor/reports/' + r.id">查看</RouterLink
                ><RouterLink
                  v-if="r.status !== '已生成'"
                  :to="'/doctor/reports/' + r.id + '/edit'"
                  >编辑</RouterLink
                ><button v-if="r.status === '已生成'" @click="download(r)">
                  下载
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    <div v-if="error || !records.length" class="empty-state">
      {{ error || "暂无符合条件的报告" }}
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
import { ref, reactive, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import UiIcon from "./UiIcon.vue";
import Pagination from "./Pagination.vue";
import { api, run, packages, maskName } from "./api";
import { downloadReport } from "./reportExport";
const route = useRoute(),
  records = ref([]),
  total = ref(0),
  page = ref(1),
  loading = ref(false),
  error = ref(""),
  filters = reactive({
    name: "",
    packageName: "",
    status: String(route.query.status || ""),
    range: "",
  });
async function load() {
  loading.value = true;
  try {
    const d = await api.get("/reports", { ...filters, page: page.value });
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
function reset() {
  Object.assign(filters, { name: "", packageName: "", status: "", range: "" });
  search();
}
async function download(r) {
  try {
    const data = await run(() => api.get("/reports/" + r.id));
    downloadReport(data);
  } catch {}
}
watch(
  () => route.query.status,
  (v) => {
    filters.status = String(v || "");
    search();
  }
);
onMounted(load);
</script>
