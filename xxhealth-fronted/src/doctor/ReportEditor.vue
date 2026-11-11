<template>
  <div class="panel report-editor" v-loading="loading">
    <template v-if="report"
      ><div class="editor-patient">
        <img :src="avatar(report.avatar)" alt="" /><strong>{{
          maskName(report.name)
        }}</strong
        ><span>{{ report.sex }}　{{ report.age }}岁</span
        ><span class="muted"
          >身份证：{{ maskCard(report.identityCard) }}　体检日期：{{
            report.examDate
          }}</span
        ><RouterLink :to="'/doctor/reports/' + report.id" class="btn"
          >预览报告</RouterLink
        >
      </div>
      <div class="editor-columns">
        <div class="editor-main">
          <div class="tabs">
            <button
              :class="{ active: tab === '基本信息' }"
              @click="tab = '基本信息'"
            >
              基本信息</button
            ><button
              :class="{ active: tab === '体检结果' }"
              @click="tab = '体检结果'"
            >
              体检结果
            </button>
          </div>
          <ReportItems
            v-if="tab === '体检结果'"
            v-model:items="report.items"
            :disabled="report.status === '已生成'"
          />
          <div v-else class="dialog-form editor-basics">
            <label>体检套餐<input :value="report.packageName" disabled /></label
            ><label>体检日期<input :value="report.examDate" disabled /></label
            ><label>报告编号<input :value="report.reportNo" disabled /></label
            ><label
              >健康评分<input
                v-model.number="report.score"
                type="number"
                min="0"
                max="100"
                :disabled="report.status === '已生成'" /></label
            ><label class="full"
              >报告状态<span class="pill blue">{{ report.status }}</span></label
            >
          </div>
        </div>
        <aside class="editor-conclusion">
          <div class="panel-heading">
            <h3>结论与建议</h3>
            <span class="muted">{{ report.doctorName }}</span>
          </div>
          <div class="editor-format">B　<i>I</i>　<u>U</u>　☷　≡　↶</div>
          <label
            >诊断结论<textarea
              v-model="report.conclusion"
              :disabled="report.status === '已生成'"
              rows="8"
              placeholder="请输入体检结论…"
            ></textarea></label
          ><label
            >健康建议<textarea
              v-model="report.advice"
              :disabled="report.status === '已生成'"
              rows="7"
              placeholder="请输入健康建议…"
            ></textarea>
          </label>
        </aside>
      </div>
      <div class="editor-footer">
        <RouterLink to="/doctor/reports" class="btn">返回列表</RouterLink>
        <div>
          <button
            v-if="report.status !== '已生成'"
            class="btn"
            :disabled="saving"
            @click="save(false)"
          >
            保存草稿</button
          ><button
            v-if="report.status === '草稿'"
            class="btn primary"
            :disabled="saving"
            @click="save(true)"
          >
            提交审核</button
          ><button
            v-if="report.status === '待审核'"
            class="btn"
            :disabled="saving"
            @click="review('草稿')"
          >
            退回修改</button
          ><button
            v-if="report.status === '待审核'"
            class="btn primary"
            :disabled="saving"
            @click="review('已生成')"
          >
            审核通过
          </button>
        </div>
      </div></template
    >
    <div v-if="error" class="empty-state">{{ error }}</div>
  </div>
</template>
<script setup>
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import ReportItems from "./ReportItems.vue";
import { api, run, confirm, avatar, maskName, maskCard } from "./api";
const route = useRoute(),
  router = useRouter(),
  report = ref(null),
  tab = ref("体检结果"),
  loading = ref(false),
  saving = ref(false),
  error = ref("");
async function load() {
  loading.value = true;
  try {
    report.value = await api.get("/reports/" + route.params.id);
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
async function save(submit) {
  saving.value = true;
  try {
    report.value = await run(() =>
      api.put("/reports/" + report.value.id, report.value)
    );
    if (submit) {
      report.value = await run(
        () =>
          api.put(`/reports/${report.value.id}/status`, { status: "待审核" }),
        "已提交审核"
      );
      router.push("/doctor/reports");
    } else {
      await run(() => Promise.resolve(), "草稿已保存");
    }
  } catch {
  } finally {
    saving.value = false;
  }
}
async function review(status) {
  if (
    !(await confirm(
      status === "已生成"
        ? "确认审核通过并生成正式报告？"
        : "确认退回草稿继续修改？"
    ))
  )
    return;
  saving.value = true;
  try {
    await api.put("/reports/" + report.value.id, report.value);
    report.value = await run(
      () => api.put(`/reports/${report.value.id}/status`, { status }),
      status === "已生成" ? "报告已生成" : "已退回草稿"
    );
    if (status === "已生成") router.push("/doctor/reports/" + report.value.id);
  } catch (e) {
    await run(() => Promise.reject(e)).catch(() => {});
  } finally {
    saving.value = false;
  }
}
onMounted(load);
</script>
