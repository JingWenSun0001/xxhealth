<template>
  <div class="packages-page" v-loading="loading">
    <section class="package-hero">
      <div class="package-hero-icon">
        <img src="/doctor-assets/package-icon.png" alt="" />
      </div>
      <div>
        <h1>体检套餐</h1>
        <h3>科学定制 · 全面体检 · 守护健康</h3>
        <p>为您和家人提供专业、全面、个性化的体检套餐</p>
      </div>
    </section>
    <section class="panel package-filters">
      <div class="package-category-tabs">
        <button
          v-for="c in ['全部', ...categories]"
          :key="c"
          :class="{ active: filters.category === (c === '全部' ? '' : c) }"
          @click="
            filters.category = c === '全部' ? '' : c;
            search();
          "
        >
          {{ c }}
        </button>
      </div>
      <form @submit.prevent="search">
        <label
          >适用人群：<select v-model="filters.gender">
            <option value="">全部</option>
            <option value="男">男性</option>
            <option value="女">女性</option>
          </select></label
        ><label
          >价格区间：<select v-model="filters.price">
            <option value="">全部</option>
            <option value="low">500元以下</option>
            <option value="middle">500–999元</option>
            <option value="high">1000元以上</option>
          </select></label
        ><label
          >套餐特色：<select v-model="filters.feature">
            <option value="">全部</option>
            <option v-for="f in features" :key="f">{{ f }}</option>
          </select></label
        >
        <div class="input-icon">
          <input
            v-model="filters.keyword"
            placeholder="请输入套餐名称或关键词…"
          /><UiIcon name="Search" />
        </div>
        <button type="button" class="btn" @click="reset">
          <UiIcon name="Refresh" />重置</button
        ><button class="btn primary"><UiIcon name="Search" />搜索</button>
      </form>
    </section>
    <div class="package-content panel">
      <aside class="package-category-panel">
        <h3><UiIcon name="Grid" />套餐分类</h3>
        <button
          v-for="(c, i) in categories"
          :key="c"
          :class="{ active: filters.category === c }"
          @click="
            filters.category = c;
            search();
          "
        >
          <UiIcon :name="categoryIcons[i]" />
          <div>
            {{ c
            }}<small
              >({{
                categoryCounts.find((v) => v.category === c)?.count || 0
              }}款)</small
            >
          </div>
        </button>
      </aside>
      <section class="package-results">
        <div class="catalog-toolbar">
          <span>共 {{ total }} 款套餐</span>
          <div>
            <button class="text-btn" @click="manage = !manage">
              {{ manage ? "退出管理" : "管理套餐" }}</button
            ><button v-if="manage" class="btn primary small" @click="edit()">
              <UiIcon name="Plus" />新建套餐</button
            ><select
              v-model="filters.sort"
              aria-label="套餐排序"
              @change="search"
            >
              <option value="">综合排序</option>
              <option value="priceAsc">价格从低到高</option>
              <option value="priceDesc">价格从高到低</option>
              <option value="newest">最新上架</option>
            </select>
          </div>
        </div>
        <div v-if="error" class="empty-state">
          {{ error }}<button class="btn" @click="load">重新加载</button>
        </div>
        <div v-else class="package-card-grid">
          <article
            v-for="p in records"
            :key="p.id"
            class="package-card"
            :class="{ disabled: !p.enabled }"
          >
            <div class="package-card-main">
              <img :src="picture(p.picture)" alt="套餐适用人群示意" />
              <div>
                <button
                  class="package-name"
                  @click="
                    detail = p;
                    detailOpen = true;
                  "
                >
                  {{ p.name }}
                </button>
                <p class="package-audience">{{ p.audience }}</p>
                <div class="package-badges">
                  <span
                    v-for="(f, i) in p.features"
                    :key="f"
                    :class="i % 2 ? 'green' : 'orange'"
                    ><UiIcon
                      :name="i % 2 ? 'CircleCheckFilled' : 'Opportunity'"
                    />{{ f }}</span
                  ><span v-if="!p.enabled" class="neutral">已下架</span>
                </div>
                <ul>
                  <li v-for="item in p.items.slice(0, 3)" :key="item">
                    <UiIcon name="CircleCheckFilled" />{{ item }}
                  </li>
                </ul>
              </div>
            </div>
            <div class="package-card-bottom">
              <div class="package-price">
                <small>¥</small>{{ Number(p.price).toLocaleString() }}
                <del>¥{{ Number(p.originalPrice).toLocaleString() }}</del>
              </div>
              <button
                v-if="!manage"
                class="btn primary small"
                :disabled="!p.enabled"
                @click="choose(p)"
              >
                选择套餐
              </button>
              <div v-else class="row-actions">
                <button @click="edit(p)">编辑</button
                ><button @click="toggleStatus(p)">
                  {{ p.enabled ? "下架" : "上架" }}
                </button>
              </div>
            </div>
          </article>
        </div>
        <div v-if="!loading && !records.length && !error" class="empty-state">
          暂无符合条件的体检套餐
        </div>
      </section>
    </div>
    <Pagination
      :total="total"
      :page="page"
      :page-size="6"
      @change="
        page = $event;
        load();
      "
    />
    <el-dialog v-model="detailOpen" title="套餐详情" width="700px"
      ><template v-if="detail"
        ><div class="package-dialog-hero">
          <img :src="picture(detail.picture)" alt="" />
          <div>
            <h2>{{ detail.name }}</h2>
            <p>{{ detail.audience }}</p>
            <p class="package-price">
              ¥{{ detail.price }} <del>¥{{ detail.originalPrice }}</del>
            </p>
          </div>
        </div>
        <h3>检查项目</h3>
        <div class="package-full-items">
          <span v-for="item in detail.items" :key="item"
            ><UiIcon name="CircleCheckFilled" />{{ item }}</span
          >
        </div>
        <p class="muted">{{ bookingNotes }}</p></template
      ><template #footer
        ><button class="btn" @click="detailOpen = false">关闭</button
        ><button
          class="btn primary"
          :disabled="!detail?.enabled"
          @click="choose(detail)"
        >
          选择套餐并预约
        </button></template
      ></el-dialog
    >
    <el-dialog
      v-model="editOpen"
      :title="form.id ? '编辑套餐' : '新建套餐'"
      width="700px"
      ><form class="dialog-form" @submit.prevent="save">
        <label
          >套餐名称<input v-model="form.name" required maxlength="80" /></label
        ><label
          >套餐分类<select v-model="form.category">
            <option v-for="c in categories" :key="c">{{ c }}</option>
          </select></label
        ><label
          >售价（元）<input
            v-model="form.price"
            type="number"
            min="0"
            step="0.01"
            required /></label
        ><label
          >原价（元）<input
            v-model="form.originalPrice"
            type="number"
            :min="form.price"
            step="0.01"
            required /></label
        ><label
          >适用人群说明<input
            v-model="form.audience"
            required
            maxlength="100" /></label
        ><label
          >性别要求<select v-model="form.gender">
            <option>全部</option>
            <option>男</option>
            <option>女</option>
          </select></label
        ><label
          >套餐封面<select v-model="form.picture">
            <option v-for="(label, key) in pictures" :key="key" :value="key">
              {{ label }}
            </option>
          </select></label
        ><label
          >展示排序<input
            v-model.number="form.sortOrder"
            type="number" /></label
        ><label class="full"
          >特色标签（以顿号分隔，最多4个）<input
            v-model="form.featureText"
            required
            placeholder="热门、推荐" /></label
        ><label class="full"
          >检查项目（每行一项）<textarea
            v-model="form.itemText"
            rows="5"
            required
          ></textarea></label
        ><label
          >上架状态<select v-model.number="form.enabled">
            <option :value="1">已上架</option>
            <option :value="0">已下架</option>
          </select></label
        >
        <div class="dialog-buttons full">
          <button type="button" class="btn" @click="editOpen = false">
            取消</button
          ><button class="btn primary" :disabled="saving">保存套餐</button>
        </div>
      </form></el-dialog
    >
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import UiIcon from "./UiIcon.vue";
import Pagination from "./Pagination.vue";
import { api, run, confirm, loadCatalog } from "./api";
const router = useRouter(),
  loading = ref(false),
  saving = ref(false),
  error = ref(""),
  records = ref([]),
  total = ref(0),
  page = ref(1),
  manage = ref(false),
  categoryCounts = ref([]),
  detail = ref(null),
  detailOpen = ref(false),
  editOpen = ref(false),
  bookingNotes = ref("请空腹到院，携带身份证及既往检查资料。");
const categories = [
    "基础体检",
    "中青年体检",
    "高端体检",
    "女性专项",
    "男性专项",
    "老年体检",
    "职场体检",
  ],
  categoryIcons = [
    "FirstAidKit",
    "UserFilled",
    "PriceTag",
    "Female",
    "Male",
    "UserFilled",
    "Suitcase",
  ],
  features = [
    "热门",
    "推荐",
    "畅销",
    "全面",
    "专业",
    "深度",
    "呵护",
    "精准",
    "高效",
    "尊享",
  ],
  pictures = {
    basic: "基础体检",
    middle: "中青年体检",
    senior: "老年体检",
    women: "女性健康",
    men: "男性健康",
    premium: "高端尊享",
  };
const empty = () => ({
    category: "",
    gender: "",
    price: "",
    feature: "",
    keyword: "",
    sort: "",
  }),
  filters = reactive(empty()),
  form = reactive({});
const picture = (key) => "/doctor-assets/package-" + (key || "basic") + ".png";
async function load() {
  loading.value = true;
  error.value = "";
  try {
    const d = await api.get("/packages", {
      ...filters,
      page: page.value,
      pageSize: 6,
    });
    records.value = d.records;
    total.value = d.total;
    categoryCounts.value = d.categories;
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
  Object.assign(filters, empty());
  search();
}
function choose(p) {
  detailOpen.value = false;
  router.push({
    path: "/doctor/appointments",
    query: { new: "1", packageId: p.id },
  });
}
function edit(p) {
  Object.keys(form).forEach((k) => delete form[k]);
  Object.assign(
    form,
    p
      ? {
          ...p,
          featureText: p.features.join("、"),
          itemText: p.items.join("\n"),
        }
      : {
          name: "",
          category: "基础体检",
          audience: "",
          gender: "全部",
          price: 399,
          originalPrice: 599,
          picture: "basic",
          enabled: 1,
          sortOrder: 0,
          featureText: "热门、推荐",
          itemText: "",
        }
  );
  editOpen.value = true;
}
async function save() {
  saving.value = true;
  try {
    const payload = {
      ...form,
      features: form.featureText
        .split(/[、，,]/)
        .map((v) => v.trim())
        .filter(Boolean),
      items: form.itemText
        .split("\n")
        .map((v) => v.trim())
        .filter(Boolean),
    };
    await run(
      () =>
        form.id
          ? api.put("/packages/" + form.id, payload)
          : api.post("/packages", payload),
      "套餐已保存"
    );
    editOpen.value = false;
    await Promise.all([load(), loadCatalog()]);
  } catch {
  } finally {
    saving.value = false;
  }
}
async function toggleStatus(p) {
  if (
    !(await confirm(
      `确认${p.enabled ? "下架" : "上架"}“${
        p.name
      }”？已存在的预约将保留原套餐记录。`
    ))
  )
    return;
  try {
    await run(
      () =>
        api.put(`/packages/${p.id}/status`, {
          version: p.version,
          enabled: p.enabled ? 0 : 1,
        }),
      "套餐状态已更新"
    );
    await Promise.all([load(), loadCatalog()]);
  } catch {}
}
onMounted(async () => {
  load();
  try {
    bookingNotes.value = (await api.get("/settings")).rules.bookingNotes;
  } catch {}
});
</script>
