<template>
  <div v-loading="loading" class="report-detail-page">
    <template v-if="report"
      ><section class="report-hero">
        <div class="report-person">
          <img :src="avatar(report.avatar)" alt="患者头像" />
          <div>
            <h2>
              {{ maskName(report.name) }}
              <small>{{ report.sex }}　{{ report.age }}岁</small>
            </h2>
            <p>ID：{{ report.reportNo.replace("HT", "H") }}</p>
            <p>手机号：{{ maskPhone(report.phone) }}</p>
            <span class="pill orange">会员</span>
            <span class="pill green">已完成体检</span>
          </div>
        </div>
        <div class="report-meta">
          <UiIcon name="Calendar" /><span>体检日期</span
          ><strong>{{ report.examDate }}</strong>
        </div>
        <div class="report-meta">
          <UiIcon name="Document" /><span>体检套餐</span
          ><strong>{{ report.packageName }}</strong>
        </div>
        <div class="report-meta">
          <UiIcon name="SuccessFilled" class="green-text" /><span>报告状态</span
          ><strong
            :class="report.status === '已生成' ? 'green-text' : 'blue-text'"
            >{{ report.status }}</strong
          >
        </div>
        <div class="report-tools">
          <button class="btn" @click="downloadReport(report)">
            <UiIcon name="Download" />下载报告</button
          ><button class="btn" @click="printReport">
            <UiIcon name="Printer" />打印报告</button
          ><button class="btn" @click="share">
            <UiIcon name="Share" />分享报告
          </button>
        </div>
      </section>
      <div class="report-columns">
        <aside class="panel report-history">
          <div class="panel-heading">
            <h3>报告列表</h3>
            <select v-model="historyStatus" aria-label="报告状态">
              <option value="">全部状态</option>
              <option>已生成</option>
              <option>待审核</option>
              <option>草稿</option>
            </select>
          </div>
          <RouterLink
            v-for="h in history"
            :key="h.id"
            :to="'/doctor/reports/' + h.id"
            class="report-history-item"
            :class="{ active: h.id === report.id }"
            ><UiIcon name="Document" />
            <div>
              <strong>{{ formatDate(h.examDate) }}</strong>
              <p>{{ h.packageName }}</p>
            </div>
            <span
              class="pill"
              :class="h.status === '已生成' ? 'green' : 'neutral'"
              >{{ h.status }}</span
            ><UiIcon name="ArrowRight"
          /></RouterLink>
          <div v-if="!history.length" class="empty-state">暂无报告</div>
        </aside>
        <div class="report-body">
          <div class="tabs report-tabs">
            <button
              v-for="t in reportTabs"
              :key="t"
              :class="{ active: tab === t }"
              @click="tab = t"
            >
              {{ t }}</button
            ><RouterLink
              v-if="['草稿', '待审核'].includes(report.status)"
              :to="'/doctor/reports/' + report.id + '/edit'"
              class="edit-report-link"
              >编辑报告 ›</RouterLink
            >
          </div>
          <template v-if="tab === '报告总览'"
            ><section class="panel health-overview">
              <div class="health-score-card">
                <h3><UiIcon name="HelpFilled" />健康总评</h3>
                <div class="health-score-content">
                  <div class="score-ring" :style="ringStyle">
                    <div>
                      <strong>{{ report.score }}分</strong
                      ><span>{{
                        report.score >= 90 ? "健康优秀" : "需关注"
                      }}</span>
                    </div>
                  </div>
                  <div class="health-copy">
                    <h3>
                      <UiIcon name="CircleCheckFilled" />{{
                        report.score >= 90
                          ? "您的身体状况良好"
                          : "请关注您的健康指标"
                      }}
                    </h3>
                    <p>
                      各项指标基本正常，建议保持健康的生活方式，<br />定期体检。
                    </p>
                    <button class="btn rounded" @click="tab = '健康评估'">
                      查看评估详情 ›
                    </button>
                  </div>
                </div>
              </div>
              <div class="abnormal-card">
                <h3><UiIcon name="WarningFilled" />主要异常项</h3>
                <div
                  v-for="item in abnormal.slice(0, 1)"
                  :key="item.name"
                  class="abnormal-inner"
                >
                  <div class="abnormal-heading">
                    <span class="round-icon red"
                      ><UiIcon name="FirstAidKit"
                    /></span>
                    <div>
                      <h3>{{ item.name }}偏高</h3>
                      <p>
                        {{ item.name }}：{{ item.value }} {{ item.unit
                        }}<br />（参考值：{{ item.range }} {{ item.unit }}）
                      </p>
                    </div>
                    <button
                      class="pill red"
                      @click="
                        activeItem = item;
                        abnormalOpen = true;
                      "
                    >
                      查看详情 ›
                    </button>
                  </div>
                  <p class="abnormal-advice">建议：控制饮食，适量运动</p>
                </div>
                <div v-if="!abnormal.length" class="normal-message">
                  <UiIcon name="CircleCheckFilled" />本次检查未见异常指标
                </div>
              </div>
            </section>
            <section class="panel result-overview">
              <h3><UiIcon name="Management" />体检结果概览</h3>
              <div class="result-cards">
                <div
                  v-for="group in groups"
                  :key="group.name"
                  class="result-card"
                >
                  <div class="result-card-heading">
                    <UiIcon :name="group.icon" /><strong>{{
                      group.name
                    }}</strong
                    ><button
                      class="pill"
                      :class="
                        group.items.some((i) => i.abnormal) ? 'orange' : 'green'
                      "
                      @click="
                        tab = '体检结果';
                        itemGroup = group.name;
                      "
                    >
                      {{
                        group.items.some((i) => i.abnormal)
                          ? "异常" +
                            group.items.filter((i) => i.abnormal).length +
                            "项"
                          : "正常"
                      }}
                      ›
                    </button>
                  </div>
                  <img
                    v-if="group.name === '影像检查'"
                    class="xray-image"
                    src="/doctor-assets/xray.png"
                    alt="胸部X线演示图像"
                  /><template v-else
                    ><div
                      v-for="i in group.items"
                      :key="i.name"
                      class="result-value"
                    >
                      <span v-if="group.name === '其他检查'" class="green-text"
                        ><UiIcon name="CircleCheckFilled" /></span
                      ><span>{{ i.name }}</span
                      ><strong
                        :class="{
                          'red-text': i.abnormal,
                          'green-text': group.name === '其他检查',
                        }"
                        >{{ i.value || "—" }} <small>{{ i.unit }}</small
                        ><b v-if="i.abnormal"> ↑</b></strong
                      >
                    </div></template
                  >
                </div>
              </div>
            </section>
            <section class="panel doctor-advice">
              <h3><UiIcon name="Opportunity" />医生建议</h3>
              <div class="advice-lines">
                <p v-for="(line, i) in adviceLines" :key="i">
                  <b>{{ i + 1 }}</b
                  >{{ line }}
                </p>
              </div>
              <div class="doctor-sign">
                <p>报告医生：{{ report.doctorName }}</p>
                <p>报告时间：{{ report.reportedAt?.slice(0, 16) }}</p>
                <strong>{{ report.doctorName }}</strong>
              </div>
            </section></template
          >
          <section v-else-if="tab === '体检结果'" class="panel all-results">
            <div class="chips">
              <button
                v-for="g in ['全部', ...groups.map((g) => g.name)]"
                :key="g"
                :class="{ active: itemGroup === g }"
                @click="itemGroup = g"
              >
                {{ g }}
              </button>
            </div>
            <table class="data-table">
              <thead>
                <tr>
                  <th>项目名称</th>
                  <th>结果</th>
                  <th>单位</th>
                  <th>参考范围</th>
                  <th>异常提示</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="i in report.items.filter(
                    (i) => itemGroup === '全部' || i.group === itemGroup
                  )"
                  :key="i.name"
                >
                  <td>{{ i.name }}</td>
                  <td :class="{ 'red-text': i.abnormal }">
                    {{ i.value || "待录入" }}
                  </td>
                  <td>{{ i.unit || "—" }}</td>
                  <td>{{ i.range }}</td>
                  <td>
                    <span :class="i.abnormal ? 'red-text' : 'green-text'">{{
                      i.abnormal ? "↑ 异常" : "正常"
                    }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </section>
          <section
            v-else-if="tab === '健康评估'"
            class="panel assessment-panel"
          >
            <h3>健康评估</h3>
            <div class="score-ring" :style="ringStyle">
              <div>
                <strong>{{ report.score }}分</strong><span>总体评估</span>
              </div>
            </div>
            <h3>
              {{
                report.score >= 90
                  ? "您的身体状况良好"
                  : "建议进一步关注健康状况"
              }}
            </h3>
            <p>{{ report.conclusion }}</p>
            <div class="assessment-metrics">
              <div>
                正常项<strong class="green-text"
                  >{{ report.items.length - abnormal.length }} 项</strong
                >
              </div>
              <div>
                异常项<strong class="red-text">{{ abnormal.length }} 项</strong>
              </div>
            </div>
          </section>
          <section v-else class="panel guidance-panel">
            <h3>诊断结论</h3>
            <p>{{ report.conclusion }}</p>
            <h3>健康建议与指导</h3>
            <p v-for="(line, i) in adviceLines" :key="i">
              {{ i + 1 }}. {{ line }}
            </p>
            <p class="muted">
              报告医生：{{ report.doctorName }} ·
              {{ report.reportedAt?.slice(0, 16) }}
            </p>
          </section>
        </div>
      </div></template
    >
    <div v-else-if="error" class="panel empty-state">
      {{ error
      }}<RouterLink to="/doctor/reports" class="btn">返回报告列表</RouterLink>
    </div>
    <el-dialog v-model="abnormalOpen" title="异常指标详情" width="560px"
      ><template v-if="activeItem"
        ><h3 class="red-text">
          {{ activeItem.name }}：{{ activeItem.value }} {{ activeItem.unit }}
        </h3>
        <p>参考范围：{{ activeItem.range }} {{ activeItem.unit }}</p>
        <p>建议结合既往病史及其他检查结果，由医生综合评估。</p>
        <p>{{ report?.advice }}</p></template
      ></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import UiIcon from "./UiIcon.vue";
import { api, avatar, maskName, maskPhone } from "./api";
import { downloadReport } from "./reportExport";
const route = useRoute(),
  report = ref(null),
  loading = ref(false),
  error = ref(""),
  tab = ref("报告总览"),
  historyStatus = ref(""),
  itemGroup = ref("全部"),
  abnormalOpen = ref(false),
  activeItem = ref(null);
const reportTabs = ["报告总览", "体检结果", "健康评估", "建议与指导"],
  abnormal = computed(
    () => report.value?.items.filter((i) => i.abnormal) || []
  ),
  history = computed(
    () =>
      report.value?.history.filter(
        (h) => !historyStatus.value || h.status === historyStatus.value
      ) || []
  );
const groups = computed(() =>
  ["基础项目", "生化检查", "影像检查", "其他检查"].map((name, i) => ({
    name,
    icon: ["Management", "FirstAidKit", "Picture", "Memo"][i],
    items: report.value?.items.filter((it) => it.group === name) || [],
  }))
);
const ringStyle = computed(() => ({
    "--score": (report.value?.score || 0) + "%",
  })),
  adviceLines = computed(
    () => report.value?.advice.split(/[\n；]/).filter(Boolean) || []
  );
const formatDate = (s) => s.replace(/(\d{4})-(\d{2})-(\d{2})/, "$1年$2月$3日");
async function load() {
  loading.value = true;
  error.value = "";
  try {
    report.value = await api.get("/reports/" + route.params.id);
  } catch (e) {
    report.value = null;
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
function printReport() {
  window.print();
}
async function share() {
  try {
    await navigator.clipboard.writeText(window.location.href);
    ElMessage.success("报告链接已复制");
  } catch {
    ElMessage.info("请复制浏览器地址栏中的报告链接");
  }
}
watch(
  () => route.params.id,
  () => {
    tab.value = "报告总览";
    load();
  }
);
onMounted(load);
</script>
