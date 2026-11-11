<template>
  <div class="panel report-create" v-loading="loading">
    <div class="report-steps">
      <div
        v-for="(s, i) in ['选择患者', '填写报告', '预览提交']"
        :key="s"
        :class="{ active: step === i, done: step > i }"
      >
        <b>{{ i + 1 }}</b
        ><span>{{ s }}</span>
      </div>
    </div>
    <div v-if="step === 0" class="create-select-columns">
      <section class="select-patient-panel">
        <h3>选择患者</h3>
        <div class="input-icon">
          <input
            v-model="keyword"
            placeholder="请输入姓名/身份证号/手机号"
            @input="filterPatients"
          /><UiIcon name="Search" />
        </div>
        <div class="chips">
          <button
            v-for="t in ['最近就诊', '本院患者', '全部患者']"
            :key="t"
            :class="{ active: patientTab === t }"
            @click="patientTab = t"
          >
            {{ t }}
          </button>
        </div>
        <button
          v-for="p in filteredPatients"
          :key="p.id"
          class="patient-select-row"
          :class="{ active: selected?.id === p.id }"
          @click="selected = p"
        >
          <img :src="avatar(p.avatar)" alt="" />
          <div>
            <strong>{{ maskName(p.name) }}　{{ p.sex }}　{{ p.age }}岁</strong>
            <p>身份证：{{ maskCard(p.identityCard) }}</p>
            <p>体检日期：{{ p.lastExamDate || "暂无" }}</p>
          </div>
          <UiIcon v-if="selected?.id === p.id" name="CircleCheckFilled" />
        </button>
        <div v-if="!filteredPatients.length" class="empty-state">
          没有符合条件的患者
        </div>
      </section>
      <section class="create-preview">
        <h3>报告信息预览</h3>
        <div class="dialog-form">
          <label class="full"
            >体检套餐：<select v-model="templateId">
              <option v-for="t in templates" :key="t.id" :value="t.id">
                {{ t.name }}
              </option>
            </select></label
          ><label class="full"
            >体检日期：<input v-model="examDate" type="date" required
          /></label>
          <p class="full">既往病史：{{ selected?.tags || "无" }}</p>
          <p class="full">主诉：{{ selected?.notes || "无" }}</p>
          <label class="full"
            >体检结论：<textarea
              v-model="conclusion"
              rows="6"
              placeholder="请输入体检结论…"
            ></textarea>
          </label>
        </div>
      </section>
    </div>
    <div v-if="step === 1" class="create-fill">
      <div class="panel-heading">
        <h3>{{ maskName(selected.name) }} · {{ chosenTemplate?.name }}</h3>
        <label
          >健康评分
          <input
            v-model.number="score"
            type="number"
            min="0"
            max="100"
            class="score-input"
        /></label>
      </div>
      <ReportItems v-model:items="items" />
      <div class="dialog-form">
        <label
          >诊断结论<textarea
            v-model="conclusion"
            rows="4"
            placeholder="请输入体检结论"
          ></textarea></label
        ><label>健康建议<textarea v-model="advice" rows="4"></textarea></label>
      </div>
    </div>
    <div v-if="step === 2" class="create-final">
      <h2>报告预览</h2>
      <p>
        {{ selected.name }} · {{ selected.sex }} ·
        {{ selected.age }}岁　体检日期：{{ examDate }}　健康评分：{{ score }}分
      </p>
      <h3>{{ chosenTemplate.name }}</h3>
      <ReportItems :items="items" disabled />
      <h3>诊断结论</h3>
      <p class="pre-wrap">{{ conclusion }}</p>
      <h3>健康建议</h3>
      <p class="pre-wrap">{{ advice }}</p>
    </div>
    <div class="editor-footer">
      <RouterLink to="/doctor/dashboard" class="btn">取消</RouterLink>
      <div>
        <button v-if="step > 0" class="btn" :disabled="saving" @click="step--">
          上一步</button
        ><button
          v-if="step < 2"
          class="btn primary"
          :disabled="!selected"
          @click="next"
        >
          {{ step === 0 ? "下一步：填写报告" : "下一步：预览报告" }} ›</button
        ><button
          v-if="step === 2"
          class="btn"
          :disabled="saving"
          @click="submit(false)"
        >
          保存草稿</button
        ><button
          v-if="step === 2"
          class="btn primary"
          :disabled="saving"
          @click="submit(true)"
        >
          提交审核
        </button>
      </div>
    </div>
    <div v-if="error" class="empty-state">{{ error }}</div>
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import ReportItems from "./ReportItems.vue";
import UiIcon from "./UiIcon.vue";
import { api, run, avatar, maskName, maskCard, today } from "./api";
const route = useRoute(),
  router = useRouter(),
  loading = ref(false),
  saving = ref(false),
  error = ref(""),
  step = ref(0),
  patients = ref([]),
  templates = ref([]),
  selected = ref(null),
  templateId = ref(1),
  examDate = ref(today()),
  keyword = ref(""),
  patientTab = ref("最近就诊"),
  conclusion = ref(""),
  advice = ref(""),
  items = ref([]),
  score = ref(95),
  createdId = ref(null);
const chosenTemplate = computed(() =>
    templates.value.find((t) => t.id === Number(templateId.value))
  ),
  filteredPatients = computed(() => {
    let list = patients.value.filter((p) =>
      [p.name, p.phone, p.identityCard].some((v) => v.includes(keyword.value))
    );
    if (patientTab.value === "最近就诊")
      list = [...list].sort((a, b) =>
        (b.lastExamDate || "").localeCompare(a.lastExamDate || "")
      );
    return list;
  });
function filterPatients() {}
function next() {
  if (step.value === 0) {
    if (!selected.value || !chosenTemplate.value || !examDate.value) {
      ElMessage.warning("请选择患者、模板与体检日期");
      return;
    }
    items.value = JSON.parse(JSON.stringify(chosenTemplate.value.items));
    advice.value = chosenTemplate.value.advice;
    step.value = 1;
  } else {
    if (score.value < 0 || score.value > 100) {
      ElMessage.warning("评分需为0–100");
      return;
    }
    step.value = 2;
  }
}
async function submit(review) {
  if (
    review &&
    (!conclusion.value.trim() ||
      items.value.some((i) => !String(i.value).trim()))
  ) {
    ElMessage.warning("提交审核前请填写全部检查结果和诊断结论");
    return;
  }
  saving.value = true;
  try {
    if (!createdId.value) {
      const r = await run(() =>
        api.post("/reports", {
          patientId: selected.value.id,
          templateId: templateId.value,
          examDate: examDate.value,
          conclusion: conclusion.value,
        })
      );
      createdId.value = r.id;
    }
    await run(() =>
      api.put("/reports/" + createdId.value, {
        items: items.value,
        score: score.value,
        conclusion: conclusion.value,
        advice: advice.value,
      })
    );
    if (review)
      await run(
        () =>
          api.put(`/reports/${createdId.value}/status`, { status: "待审核" }),
        "报告已提交审核"
      );
    else ElMessage.success("草稿已保存");
    router.push("/doctor/reports/" + createdId.value + (review ? "" : "/edit"));
  } catch {
  } finally {
    saving.value = false;
  }
}
onMounted(async () => {
  loading.value = true;
  try {
    const [p, t] = await Promise.all([
      api.get("/patients", { pageSize: 500 }),
      api.get("/templates"),
    ]);
    patients.value = p.records;
    templates.value = t;
    templateId.value = Number(route.query.templateId) || t[0]?.id || 1;
    selected.value =
      p.records.find((v) => v.id === Number(route.query.patientId)) || null;
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
});
</script>
