<template>
  <div class="panel templates-page" v-loading="loading">
    <div class="template-toolbar">
      <div class="tabs">
        <button
          v-for="t in ['全部模板', '常规模板', '高级模板', '专项模板']"
          :key="t"
          :class="{ active: category === t }"
          @click="category = t"
        >
          {{ t }}
        </button>
      </div>
      <div class="input-icon">
        <input v-model="keyword" placeholder="搜索模板…" /><UiIcon
          name="Search"
        />
      </div>
      <button class="btn primary" @click="createOpen = true">新建模板</button>
    </div>
    <div class="template-grid">
      <article v-for="t in filtered" :key="t.id" class="template-card">
        <div class="template-info">
          <div class="document-art"><UiIcon name="Document" /></div>
          <div>
            <h3>{{ t.name }}</h3>
            <p>{{ t.description }}</p>
          </div>
        </div>
        <button
          class="btn"
          @click="router.push('/doctor/reports/new?templateId=' + t.id)"
        >
          使用
        </button>
      </article>
    </div>
    <div v-if="error || !filtered.length" class="empty-state">
      {{ error || "暂无匹配的模板" }}
    </div>
    <el-dialog v-model="createOpen" title="新建报告模板" width="700px"
      ><form class="dialog-form" @submit.prevent="save">
        <label
          >模板名称<input
            v-model="form.name"
            required
            placeholder="如年度体检报告" /></label
        ><label
          >模板分类<select v-model="form.category">
            <option>常规模板</option>
            <option>高级模板</option>
            <option>专项模板</option>
          </select></label
        ><label class="full"
          >适用人群<input v-model="form.description" required /></label
        ><label class="full"
          >基于现有模板<select v-model="baseId" @change="copyBase">
            <option v-for="t in templates" :key="t.id" :value="t.id">
              {{ t.name }}
            </option>
          </select></label
        >
        <div class="full template-item-list">
          <h4>检查项目</h4>
          <label v-for="(i, index) in form.items" :key="index"
            ><input v-model="enabled[index]" type="checkbox" />{{
              i.name
            }}</label
          >
        </div>
        <label class="full"
          >默认健康建议<textarea v-model="form.advice" rows="4"></textarea>
        </label>
        <div class="dialog-buttons full">
          <button class="btn" type="button" @click="createOpen = false">
            取消</button
          ><button class="btn primary" :disabled="saving">保存模板</button>
        </div>
      </form></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import UiIcon from "./UiIcon.vue";
import { api, run } from "./api";
const router = useRouter(),
  templates = ref([]),
  category = ref("全部模板"),
  keyword = ref(""),
  loading = ref(false),
  error = ref(""),
  createOpen = ref(false),
  saving = ref(false),
  baseId = ref(1),
  enabled = ref([]),
  form = reactive({
    name: "",
    category: "常规模板",
    description: "",
    advice: "",
    items: [],
  });
const filtered = computed(() =>
  templates.value.filter(
    (t) =>
      (category.value === "全部模板" || t.category === category.value) &&
      t.name.includes(keyword.value)
  )
);
function copyBase() {
  const t = templates.value.find((t) => t.id === Number(baseId.value));
  if (t) {
    form.items = JSON.parse(JSON.stringify(t.items));
    form.advice = t.advice;
    enabled.value = form.items.map(() => true);
  }
}
async function load() {
  loading.value = true;
  try {
    templates.value = await api.get("/templates");
    copyBase();
  } catch (e) {
    error.value = e.message;
  } finally {
    loading.value = false;
  }
}
async function save() {
  saving.value = true;
  try {
    await run(
      () =>
        api.post("/templates", {
          ...form,
          items: form.items.filter((_, i) => enabled.value[i]),
        }),
      "模板已保存"
    );
    createOpen.value = false;
    await load();
  } catch {
  } finally {
    saving.value = false;
  }
}
onMounted(load);
</script>
