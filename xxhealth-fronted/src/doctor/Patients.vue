<template>
  <div class="patients-page" v-loading="loading">
    <div class="patient-stats">
      <div v-for="s in statCards" :key="s.label" class="stat-card">
        <div class="stat-icon" :class="s.color">
          <img :src="'/doctor-assets/stat-' + s.asset + '.png'" alt="" />
        </div>
        <div>
          <span>{{ s.label }}</span>
          <div class="stat-number">
            {{ Number(s.value || 0).toLocaleString() }}
            <small :class="s.color">{{ s.note }}</small>
          </div>
        </div>
      </div>
    </div>
    <div class="panel patient-filters">
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="t.key"
          :class="{ active: tab === t.key }"
          @click="
            tab = t.key;
            search();
          "
        >
          {{ t.label }} ({{ stats[t.count] || 0 }})
        </button>
      </div>
      <form @submit.prevent="search()" class="filter-grid">
        <label
          >姓名：
          <div class="input-icon">
            <input v-model="filters.name" placeholder="请输入患者姓名" /><UiIcon
              name="Search"
            /></div></label
        ><label
          >手机号：<input
            v-model="filters.phone"
            placeholder="请输入手机号" /></label
        ><label
          >证件号：<input
            v-model="filters.identityCard"
            placeholder="请输入证件号" /></label
        ><label
          >状态：<select v-model="filters.healthStatus">
            <option value="">全部</option>
            <option>正常</option>
            <option>异常</option>
            <option>关注</option>
          </select></label
        >
        <label
          >年龄：<select v-model="filters.age">
            <option value="">全部</option>
            <option value="young">35岁以下</option>
            <option value="middle">35–59岁</option>
            <option value="senior">60岁以上</option>
          </select></label
        ><label
          >性别：<select v-model="filters.sex">
            <option value="">全部</option>
            <option>男</option>
            <option>女</option>
          </select></label
        ><label class="date-filter"
          >体检时间：<input
            v-model="filters.startDate"
            type="date"
            aria-label="体检开始日期" /><span>~</span
          ><input
            v-model="filters.endDate"
            type="date"
            aria-label="体检结束日期"
        /></label>
        <div class="filter-actions">
          <button type="button" class="btn" @click="reset">
            <UiIcon name="Refresh" />重置</button
          ><button class="btn primary"><UiIcon name="Search" />查询</button
          ><button type="button" class="btn" @click="expanded = !expanded">
            {{ expanded ? "收起" : "展开" }}<UiIcon name="ArrowDown" />
          </button>
        </div>
        <div v-if="expanded" class="filter-note">
          支持组合筛选；姓名和手机号支持模糊查询，体检时间包含起止日期。
        </div>
      </form>
    </div>
    <div class="panel patient-table-panel">
      <div class="toolbar">
        <div>
          <button class="btn primary" @click="openEdit()">
            <UiIcon name="Plus" />新建患者</button
          ><button class="btn" @click="fileInput.click()">
            <UiIcon name="Upload" />导入</button
          ><input
            ref="fileInput"
            type="file"
            accept=".csv"
            hidden
            @change="importFile"
          /><button class="btn" @click="exportRows">
            <UiIcon name="Download" />导出</button
          ><el-dropdown @command="bulk"
            ><button class="btn">
              <UiIcon name="Memo" />批量操作<UiIcon name="ArrowDown" /></button
            ><template #dropdown
              ><el-dropdown-menu
                ><el-dropdown-item command="export"
                  >导出所选患者</el-dropdown-item
                ><el-dropdown-item command="delete"
                  >删除所选患者</el-dropdown-item
                ><el-dropdown-item command="template"
                  >下载导入模板</el-dropdown-item
                ></el-dropdown-menu
              ></template
            ></el-dropdown
          >
        </div>
        <div>
          <div class="view-toggle">
            <button
              :class="{ active: !grid }"
              aria-label="表格视图"
              @click="grid = false"
            >
              <UiIcon name="List" /></button
            ><button
              :class="{ active: grid }"
              aria-label="卡片视图"
              @click="grid = true"
            >
              <UiIcon name="Grid" />
            </button>
          </div>
          <el-popover trigger="click" width="200"
            ><template #reference
              ><button class="text-btn">
                <UiIcon name="Setting" />列设置
              </button></template
            >
            <div class="column-options">
              <label
                v-for="col in ['phone', 'identityCard', 'lastExamDate', 'tags']"
                :key="col"
                ><input v-model="columns" type="checkbox" :value="col" />{{
                  columnNames[col]
                }}</label
              >
            </div></el-popover
          >
        </div>
      </div>
      <div v-if="error" class="empty-state">
        {{ error }}<button class="btn" @click="load">重试</button>
      </div>
      <template v-else
        ><div v-if="!grid" class="table-scroll">
          <table class="data-table patient-table">
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    :checked="
                      records.length > 0 && selected.length === records.length
                    "
                    aria-label="选择本页全部患者"
                    @change="
                      selected = $event.target.checked
                        ? records.map((r) => r.id)
                        : []
                    "
                  />
                </th>
                <th>序号</th>
                <th>
                  <button class="text-btn dark" @click="sort = !sort">
                    姓名<UiIcon name="Sort" />
                  </button>
                </th>
                <th>性别</th>
                <th>年龄</th>
                <th v-if="columns.includes('phone')">手机号</th>
                <th v-if="columns.includes('identityCard')">证件号</th>
                <th v-if="columns.includes('lastExamDate')">体检时间</th>
                <th>健康状态</th>
                <th v-if="columns.includes('tags')">标签</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(p, i) in displayed" :key="p.id">
                <td>
                  <input
                    v-model="selected"
                    type="checkbox"
                    :value="p.id"
                    :aria-label="'选择' + p.name"
                  />
                </td>
                <td>{{ (page - 1) * pageSize + i + 1 }}</td>
                <td>
                  <div class="patient-name">
                    <img :src="avatar(p.avatar)" alt="" /><strong>{{
                      maskName(p.name)
                    }}</strong>
                  </div>
                </td>
                <td>
                  <span class="pill" :class="p.sex === '女' ? 'red' : 'blue'">{{
                    p.sex
                  }}</span>
                </td>
                <td>{{ p.age }}</td>
                <td v-if="columns.includes('phone')">
                  {{ maskPhone(p.phone) }}
                </td>
                <td v-if="columns.includes('identityCard')">
                  {{ maskCard(p.identityCard) }}
                </td>
                <td v-if="columns.includes('lastExamDate')">
                  {{ p.lastExamDate || "—" }}
                </td>
                <td>
                  <span class="pill" :class="statusColor(p.healthStatus)">{{
                    p.healthStatus
                  }}</span>
                </td>
                <td v-if="columns.includes('tags')">
                  <span class="pill neutral">{{ p.tags }}</span>
                </td>
                <td>
                  <div class="row-actions">
                    <button @click="showDetail(p.id)">详情</button
                    ><button @click="openEdit(p)">编辑</button
                    ><button @click="remove(p)">删除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="patient-card-grid">
          <div v-for="p in displayed" :key="p.id" class="patient-grid-card">
            <img :src="avatar(p.avatar)" alt="" />
            <h3>
              {{ maskName(p.name) }}
              <span class="pill" :class="statusColor(p.healthStatus)">{{
                p.healthStatus
              }}</span>
            </h3>
            <p>{{ p.sex }} · {{ p.age }}岁 · {{ maskPhone(p.phone) }}</p>
            <p>最近体检：{{ p.lastExamDate || "暂无" }} · {{ p.tags }}</p>
            <div class="row-actions">
              <button @click="showDetail(p.id)">详情</button
              ><button @click="openEdit(p)">编辑</button
              ><button @click="remove(p)">删除</button>
            </div>
          </div>
        </div>
        <div v-if="!records.length" class="empty-state">
          <UiIcon name="User" />暂无符合条件的患者
        </div></template
      >
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
    </div>
    <el-dialog
      v-model="editOpen"
      :title="form.id ? '编辑患者' : '新建患者'"
      width="620px"
      destroy-on-close
      ><form ref="editForm" class="dialog-form" @submit.prevent="save">
        <label
          >患者姓名<input
            v-model="form.name"
            required
            maxlength="60"
            placeholder="请输入真实姓名" /></label
        ><label
          >性别<select v-model="form.sex">
            <option>女</option>
            <option>男</option>
          </select></label
        ><label
          >年龄<input
            v-model.number="form.age"
            type="number"
            min="0"
            max="150"
            required /></label
        ><label
          >手机号码<input
            v-model="form.phone"
            required
            pattern="1[3-9][0-9]{9}"
            placeholder="11位手机号码" /></label
        ><label class="full"
          >身份证号<input
            v-model="form.identityCard"
            required
            pattern="([0-9]{15}|[0-9]{17}[0-9Xx])" /></label
        ><label
          >健康状态<select v-model="form.healthStatus">
            <option>正常</option>
            <option>异常</option>
            <option>关注</option>
          </select></label
        ><label
          >最近体检日期<input v-model="form.lastExamDate" type="date" /></label
        ><label class="full"
          >健康标签<input
            v-model="form.tags"
            placeholder="如高血压、高血脂" /></label
        ><label class="full">地址<input v-model="form.address" /></label
        ><label class="full"
          >备注<textarea v-model="form.notes" rows="3"></textarea>
        </label>
        <div class="dialog-buttons full">
          <button type="button" class="btn" @click="editOpen = false">
            取消</button
          ><button class="btn primary" :disabled="saving">
            {{ saving ? "保存中…" : "保存患者" }}
          </button>
        </div>
      </form></el-dialog
    >
    <el-dialog v-model="detailOpen" title="患者详情" width="660px"
      ><div v-if="detail" class="patient-detail">
        <div class="detail-person">
          <img :src="avatar(detail.avatar)" alt="" />
          <div>
            <h2>
              {{ detail.name }}
              <span class="pill" :class="statusColor(detail.healthStatus)">{{
                detail.healthStatus
              }}</span>
            </h2>
            <p>
              {{ detail.sex }} · {{ detail.age }}岁 ·
              {{ maskPhone(detail.phone) }}
            </p>
          </div>
        </div>
        <dl>
          <dt>身份证号</dt>
          <dd>{{ maskCard(detail.identityCard) }}</dd>
          <dt>健康标签</dt>
          <dd>{{ detail.tags }}</dd>
          <dt>联系地址</dt>
          <dd>{{ detail.address || "暂无" }}</dd>
          <dt>备注</dt>
          <dd>{{ detail.notes || "暂无" }}</dd>
        </dl>
        <h3>体检记录</h3>
        <div v-for="r in detail.reports" :key="r.id" class="history-line">
          <span>{{ r.examDate }} · {{ r.packageName }}</span
          ><RouterLink
            :to="'/doctor/reports/' + r.id"
            @click="detailOpen = false"
            >查看报告 ›</RouterLink
          >
        </div>
        <p v-if="!detail.reports?.length" class="empty-state">暂无体检报告</p>
      </div></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from "vue";
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
  maskCard,
  downloadCsv,
  workspaceSettings,
  loadSettings,
} from "./api";
const records = ref([]),
  total = ref(0),
  stats = ref({}),
  page = ref(1),
  pageSize = ref(Number(workspaceSettings.preferences.pageSize) || 10),
  tab = ref("all"),
  loading = ref(false),
  error = ref(""),
  expanded = ref(false),
  grid = ref(false),
  sort = ref(false),
  selected = ref([]),
  fileInput = ref(null);
const emptyFilters = () => ({
  name: "",
  phone: "",
  identityCard: "",
  healthStatus: "",
  age: "",
  sex: "",
  startDate: "",
  endDate: "",
});
const filters = reactive(emptyFilters()),
  columns = ref(["phone", "identityCard", "lastExamDate", "tags"]),
  columnNames = {
    phone: "手机号",
    identityCard: "证件号",
    lastExamDate: "体检时间",
    tags: "标签",
  };
const tabs = [
  { key: "all", label: "全部患者", count: "total" },
  { key: "today", label: "今日就诊", count: "todayVisit" },
  { key: "return", label: "复诊患者", count: "returning" },
  { key: "risk", label: "高风险患者", count: "risk" },
];
const statCards = computed(() => [
  {
    label: "患者总数",
    value: stats.value.total,
    asset: "patients",
    icon: "UserFilled",
    color: "blue",
    note: "",
  },
  {
    label: "今日新增",
    value: stats.value.today,
    asset: "today",
    icon: "Calendar",
    color: "green",
    note: "",
  },
  {
    label: "复诊患者",
    value: stats.value.returning,
    asset: "return",
    icon: "TrendCharts",
    color: "purple",
    note: "",
  },
  {
    label: "待随访",
    value: stats.value.followUp,
    asset: "follow",
    icon: "Clock",
    color: "orange",
    note: "",
  },
]);
const displayed = computed(() =>
  sort.value
    ? [...records.value].sort((a, b) => a.name.localeCompare(b.name, "zh-CN"))
    : records.value
);
const statusColor = (s) =>
  s === "正常" ? "green" : s === "异常" ? "red" : "orange";
async function load() {
  loading.value = true;
  error.value = "";
  try {
    const d = await api.get("/patients", {
      ...filters,
      tab: tab.value,
      page: page.value,
      pageSize: pageSize.value,
    });
    records.value = d.records;
    total.value = d.total;
    stats.value = d.stats;
    selected.value = [];
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
  Object.assign(filters, emptyFilters());
  search();
}
const editOpen = ref(false),
  saving = ref(false),
  form = reactive({}),
  detailOpen = ref(false),
  detail = ref(null);
function openEdit(p) {
  Object.keys(form).forEach((k) => delete form[k]);
  Object.assign(
    form,
    p || {
      name: "",
      sex: "女",
      age: 30,
      phone: "",
      identityCard: "",
      healthStatus: "正常",
      tags: "无",
      lastExamDate: "",
      address: "",
      notes: "",
    }
  );
  editOpen.value = true;
}
async function save() {
  saving.value = true;
  try {
    await run(
      () =>
        form.id
          ? api.put("/patients/" + form.id, form)
          : api.post("/patients", form),
      "患者信息已保存"
    );
    editOpen.value = false;
    await load();
  } catch {
  } finally {
    saving.value = false;
  }
}
async function showDetail(id) {
  try {
    detail.value = await run(() => api.get("/patients/" + id));
    detailOpen.value = true;
  } catch {}
}
async function remove(p) {
  if (!(await confirm(`确认删除患者“${p.name}”的档案？`))) return;
  try {
    await run(() => api.delete("/patients/" + p.id), "患者档案已删除");
    await load();
  } catch {}
}
const csvHeaders = [
  ["姓名", "name"],
  ["性别", "sex"],
  ["年龄", "age"],
  ["手机号", "phone"],
  ["证件号", "identityCard"],
  ["健康状态", "healthStatus"],
  ["标签", "tags"],
  ["体检时间", "lastExamDate"],
];
async function exportRows() {
  try {
    const d = await run(() =>
      api.get("/patients", { ...filters, tab: tab.value, pageSize: 500 })
    );
    downloadCsv(d.records, csvHeaders, "患者档案.csv");
    if (d.total > 500) ElMessage.info("单次导出前500条，请通过筛选缩小范围");
  } catch {}
}
async function bulk(command) {
  if (command === "template") {
    downloadCsv(
      [
        {
          name: "示例姓名",
          sex: "女",
          age: 32,
          phone: "13812345678",
          identityCard: "110101199401011234",
          healthStatus: "正常",
          tags: "无",
          lastExamDate: "2025-06-16",
        },
      ],
      csvHeaders,
      "患者导入模板.csv"
    );
    return;
  }
  if (!selected.value.length) {
    ElMessage.warning("请先选择患者");
    return;
  }
  if (command === "export") {
    downloadCsv(
      records.value.filter((p) => selected.value.includes(p.id)),
      csvHeaders,
      "所选患者.csv"
    );
    return;
  }
  if (!(await confirm(`确认删除所选${selected.value.length}位患者？`))) return;
  try {
    for (const id of selected.value)
      await run(() => api.delete("/patients/" + id));
    ElMessage.success("删除完成");
  } catch {
  } finally {
    load();
  }
}
function parseCsv(text) {
  const out = [];
  let row = [],
    cell = "",
    quoted = false;
  for (let i = 0; i < text.length; i++) {
    const ch = text[i];
    if (ch === '"') {
      if (quoted && text[i + 1] === '"') {
        cell += '"';
        i++;
      } else quoted = !quoted;
    } else if (ch === "," && !quoted) {
      row.push(cell);
      cell = "";
    } else if ((ch === "\n" || ch === "\r") && !quoted) {
      if (ch === "\r" && text[i + 1] === "\n") i++;
      row.push(cell);
      if (row.some(Boolean)) out.push(row);
      row = [];
      cell = "";
    } else cell += ch;
  }
  if (cell || row.length) {
    row.push(cell);
    out.push(row);
  }
  if (quoted) throw new Error("CSV引号不匹配");
  return out;
}
async function importFile(e) {
  const f = e.target.files[0];
  if (!f) return;
  try {
    const rows = parseCsv((await f.text()).replace(/^\ufeff/, ""));
    const header = rows.shift();
    if (!header?.includes("姓名"))
      throw new Error("请使用“批量操作”中的导入模板");
    const data = rows.map((r) =>
      Object.fromEntries(
        csvHeaders.map(([label, key]) => [key, r[header.indexOf(label)] || ""])
      )
    );
    await run(() => api.post("/patients/import", data), "导入成功");
    await load();
  } catch (e) {
    ElMessage.error(e.message);
  } finally {
    e.target.value = "";
  }
}
onMounted(async () => {
  try {
    await loadSettings();
    pageSize.value = Number(workspaceSettings.preferences.pageSize) || 10;
  } catch {}
  load();
});
</script>
