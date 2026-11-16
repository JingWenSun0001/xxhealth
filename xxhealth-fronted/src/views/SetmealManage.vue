<template>
  <div class="setmeal-page">
    <!-- 顶部横幅 -->
    <section class="page-banner banner">
      <div class="banner-main">
        <div class="banner-icon"><el-icon><FirstAidKit /></el-icon></div>
        <div>
          <h2>体检套餐</h2>
          <p>科学定制 · 全面体检 · 守护健康，为受检者提供专业、全面、个性化的体检方案。</p>
        </div>
      </div>
      <div class="banner-right">
        <span>套餐总数</span>
        <strong>{{ packages.length }}</strong>
      </div>
    </section>

    <!-- 筛选 -->
    <section class="card filter-card">
      <div class="cat-tabs">
        <span
          v-for="cat in categories"
          :key="cat.key"
          class="cat-tab"
          :class="{ active: activeCategory === cat.key }"
          @click="activeCategory = cat.key"
        >{{ cat.label }}</span>
      </div>

      <div class="filter-row">
        <div class="filter-item">
          <span class="field-label">适用人群</span>
          <el-select v-model="audience" size="default" class="filter-select">
            <el-option label="全部" value="all" />
            <el-option label="青年（18-40）" value="young" />
            <el-option label="中年（40-60）" value="middle" />
            <el-option label="老年（60+）" value="senior" />
          </el-select>
        </div>
        <div class="filter-item">
          <span class="field-label">价格区间</span>
          <el-select v-model="priceRange" size="default" class="filter-select">
            <el-option label="全部" value="all" />
            <el-option label="500 元以下" value="lt500" />
            <el-option label="500-1000 元" value="mid" />
            <el-option label="1000 元以上" value="gt1000" />
          </el-select>
        </div>
        <div class="filter-item">
          <span class="field-label">套餐特色</span>
          <el-select v-model="feature" size="default" class="filter-select">
            <el-option label="全部" value="all" />
            <el-option label="热门" value="热门" />
            <el-option label="全面" value="全面" />
            <el-option label="精准" value="精准" />
            <el-option label="尊享" value="尊享" />
          </el-select>
        </div>
        <el-input v-model="keyword" placeholder="请输入套餐名称或关键词…" clearable class="search-input">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button @click="resetFilters"><el-icon><RefreshLeft /></el-icon>&nbsp;重置</el-button>
        <el-button type="primary" @click="page = 1"><el-icon><Search /></el-icon>&nbsp;搜索</el-button>
      </div>
    </section>

    <!-- 主体 -->
    <section class="body-grid">
      <aside class="card category-card">
        <div class="category-head">套餐分类</div>
        <ul class="category-list">
          <li
            v-for="cat in categories"
            :key="cat.key"
            class="category-item"
            :class="{ active: activeCategory === cat.key }"
            @click="activeCategory = cat.key"
          >
            <span class="category-name">{{ cat.label }}</span>
            <small>{{ categoryCount(cat.key) }} 款</small>
          </li>
        </ul>
      </aside>

      <div class="card list-card">
        <div class="list-head">
          <span>共 <strong>{{ filtered.length }}</strong> 款套餐</span>
          <span class="sort-text">综合排序</span>
        </div>

        <div v-loading="loading" class="pkg-grid">
          <article v-for="pkg in pagedPackages" :key="pkg.smid" class="pkg-card">
            <div class="pkg-cover" :class="pkg.type === 0 ? 'cover-female' : 'cover-male'">
              <el-icon class="cover-icon"><UserFilled /></el-icon>
              <span class="cover-badge">{{ packageMeta(pkg).tags[0] }}</span>
            </div>

            <div class="pkg-body">
              <h3>{{ pkg.name }}</h3>
              <p class="pkg-crowd">{{ packageMeta(pkg).crowd }}</p>

              <div class="pkg-tags">
                <span
                  v-for="tag in packageMeta(pkg).tags"
                  :key="tag"
                  class="tag"
                  :class="tag === '热门' ? 'tag-warn' : 'tag-info'"
                >{{ tag }}</span>
              </div>

              <ul class="pkg-features">
                <li v-for="f in packageMeta(pkg).features" :key="f">
                  <el-icon><CircleCheckFilled /></el-icon>{{ f }}
                </li>
              </ul>

              <div class="pkg-foot">
                <div class="pkg-price">
                  <strong>¥{{ pkg.price }}</strong>
                  <small>¥{{ Math.round(pkg.price * 1.5) }}</small>
                </div>
                <el-button type="primary" @click="openDetail(pkg)">查看详情</el-button>
              </div>
            </div>
          </article>

          <el-empty
            v-if="!loading && !filtered.length"
            class="empty-block"
            description="没有符合条件的套餐，试试重置筛选"
            :image-size="110"
          />
        </div>

        <div v-if="filtered.length > pageSize" class="pager">
          <el-pagination
            v-model:current-page="page"
            :page-size="pageSize"
            :total="filtered.length"
            layout="prev, pager, next, total"
            background
          />
        </div>
      </div>
    </section>

    <!-- 套餐详情 -->
    <el-dialog v-model="detailVisible" title="套餐详情" width="680px" top="7vh">
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <div class="detail-head">
            <div>
              <h3>{{ detail.name }}</h3>
              <p>{{ packageMeta(detail).crowd }}</p>
            </div>
            <div class="detail-price">¥{{ detail.price }}</div>
          </div>

          <div class="detail-tags">
            <span v-for="tag in packageMeta(detail).tags" :key="tag" class="tag tag-info">{{ tag }}</span>
            <span class="tag tag-muted">{{ detail.type === 0 ? '女士套餐' : '男士套餐' }}</span>
          </div>

          <div class="detail-section-title">包含检查项（{{ (detail.items || []).length }}）</div>
          <div v-if="detail.items && detail.items.length" class="item-list">
            <div v-for="item in detail.items" :key="item.ciid" class="item-row">
              <div class="item-name">{{ item.ciname }}</div>
              <div class="item-content">{{ item.cicontent || '—' }}</div>
            </div>
          </div>
          <el-empty v-else description="该套餐暂无检查项" :image-size="70" />
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { listSetmealByType, getSetmealById } from '@/api/setmeal'
import { packageMeta } from '@/mock/doctorData'

const packages = ref([])
const loading = ref(false)

const activeCategory = ref('all')
const audience = ref('all')
const priceRange = ref('all')
const feature = ref('all')
const keyword = ref('')

const page = ref(1)
const pageSize = 6

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

const categories = [
  { key: 'all', label: '全部' },
  { key: '基础体检', label: '基础体检' },
  { key: '中青年体检', label: '中青年体检' },
  { key: '高端体检', label: '高端体检' },
  { key: '女性专项', label: '女性专项' },
  { key: '男性专项', label: '男性专项' },
  { key: '老年体检', label: '老年体检' },
  { key: '职场体检', label: '职场体检' }
]

function inCategory(pkg, key) {
  if (key === 'all') return true
  if (key === '女性专项') return pkg.type === 0
  if (key === '男性专项') return pkg.type === 1
  const word = key.replace('体检', '')
  return (pkg.name || '').indexOf(word) >= 0
}

function inAudience(pkg, value) {
  if (value === 'all') return true
  const crowd = packageMeta(pkg).crowd
  if (value === 'young') return crowd.indexOf('18-40') >= 0
  if (value === 'middle') return crowd.indexOf('40-60') >= 0
  if (value === 'senior') return crowd.indexOf('60') >= 0
  return true
}

function inPrice(pkg, value) {
  if (value === 'all') return true
  if (value === 'lt500') return pkg.price < 500
  if (value === 'mid') return pkg.price >= 500 && pkg.price <= 1000
  if (value === 'gt1000') return pkg.price > 1000
  return true
}

function inFeature(pkg, value) {
  if (value === 'all') return true
  const meta = packageMeta(pkg)
  return meta.tags.indexOf(value) >= 0 || meta.features.join('').indexOf(value) >= 0
}

const filtered = computed(() => {
  const kw = keyword.value.trim()
  return packages.value.filter((pkg) => {
    if (!inCategory(pkg, activeCategory.value)) return false
    if (!inAudience(pkg, audience.value)) return false
    if (!inPrice(pkg, priceRange.value)) return false
    if (!inFeature(pkg, feature.value)) return false
    if (kw && (pkg.name || '').indexOf(kw) < 0) return false
    return true
  })
})

const pagedPackages = computed(() => {
  const start = (page.value - 1) * pageSize
  return filtered.value.slice(start, start + pageSize)
})

function categoryCount(key) {
  return packages.value.filter((pkg) => inCategory(pkg, key)).length
}

function resetFilters() {
  activeCategory.value = 'all'
  audience.value = 'all'
  priceRange.value = 'all'
  feature.value = 'all'
  keyword.value = ''
  page.value = 1
}

watch(filtered, () => {
  page.value = 1
})

async function loadPackages() {
  loading.value = true
  try {
    const data = await listSetmealByType(null)
    packages.value = Array.isArray(data) ? data : []
  } catch (e) {
    packages.value = []
  } finally {
    loading.value = false
  }
}

async function openDetail(pkg) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    detail.value = await getSetmealById(pkg.smid)
  } catch (e) {
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

onMounted(loadPackages)
</script>

<style scoped>
.setmeal-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.banner-main {
  display: flex;
  align-items: center;
  gap: 18px;
  position: relative;
  z-index: 1;
}

.banner-right {
  position: relative;
  z-index: 1;
  text-align: right;
  padding: 12px 20px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.16);
}

.banner-right span {
  display: block;
  font-size: 11px;
  opacity: 0.8;
}

.banner-right strong {
  display: block;
  margin-top: 2px;
  font-size: 26px;
  font-weight: 800;
}

/* 筛选 */
.filter-card {
  padding: 18px 24px 16px;
}

.cat-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.cat-tab {
  padding: 7px 16px;
  border-radius: 999px;
  font-size: 12.5px;
  color: var(--text-2);
  background: #f5f8fe;
  cursor: pointer;
  transition: all 0.18s ease;
}

.cat-tab.active {
  background: var(--brand);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 8px 16px rgba(47, 107, 255, 0.25);
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.field-label {
  font-size: 12.5px;
  color: var(--text-2);
  white-space: nowrap;
}

.filter-select {
  width: 132px;
}

.search-input {
  width: 240px;
  margin-left: auto;
}

/* 主体 */
.body-grid {
  display: grid;
  grid-template-columns: 190px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.category-card {
  padding: 16px 12px;
  position: sticky;
  top: 0;
}

.category-head {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-1);
  padding: 0 10px 12px;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
  color: var(--text-2);
  cursor: pointer;
  border: 1px solid transparent;
}

.category-item:hover {
  background: #f7faff;
}

.category-item.active {
  background: var(--brand-light);
  color: var(--brand);
  font-weight: 600;
  border-color: #cfe0ff;
}

.category-item small {
  color: var(--text-3);
  font-size: 11px;
}

.list-card {
  padding: 18px 20px 22px;
  min-height: 560px;
}

.list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--text-2);
}

.list-head strong {
  color: var(--brand);
  font-size: 15px;
}

.sort-text {
  font-size: 12px;
  color: var(--text-3);
}

.pkg-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  min-height: 320px;
}

.pkg-card {
  border: 1px solid var(--line);
  border-radius: 16px;
  overflow: hidden;
  background: #fff;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
}

.pkg-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-hover);
}

.pkg-cover {
  position: relative;
  height: 118px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.cover-male {
  background: linear-gradient(135deg, #e8f0ff 0%, #d5e5ff 55%, #c5dbff 100%);
  color: #3d7bff;
}

.cover-female {
  background: linear-gradient(135deg, #ffe9f1 0%, #ffd9e7 55%, #ffc7dc 100%);
  color: #f4628f;
}

.cover-icon {
  font-size: 54px;
  opacity: 0.85;
}

.cover-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 2px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--brand);
  font-size: 11px;
  font-weight: 700;
}

.pkg-body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.pkg-body h3 {
  font-size: 15px;
  font-weight: 800;
  color: var(--text-1);
}

.pkg-crowd {
  margin-top: 5px;
  font-size: 11.5px;
  color: var(--text-3);
}

.pkg-tags {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.pkg-features {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
}

.pkg-features li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-2);
}

.pkg-features .el-icon {
  color: var(--green);
  font-size: 13px;
}

.pkg-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}

.pkg-price strong {
  font-size: 20px;
  font-weight: 800;
  color: var(--red);
}

.pkg-price small {
  margin-left: 6px;
  font-size: 12px;
  color: var(--text-3);
  text-decoration: line-through;
}

.empty-block {
  grid-column: 1 / -1;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

/* 详情弹窗 */
.detail-body {
  min-height: 160px;
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.detail-head h3 {
  font-size: 18px;
  font-weight: 800;
  color: var(--text-1);
}

.detail-head p {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-3);
}

.detail-price {
  font-size: 22px;
  font-weight: 800;
  color: var(--red);
}

.detail-tags {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.detail-section-title {
  margin: 20px 0 10px;
  font-size: 13px;
  font-weight: 700;
  color: var(--text-1);
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 46vh;
  overflow-y: auto;
}

.item-row {
  display: flex;
  gap: 14px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f8fafe;
  border: 1px solid var(--line);
  font-size: 12.5px;
}

.item-name {
  width: 96px;
  flex-shrink: 0;
  font-weight: 600;
  color: var(--text-1);
}

.item-content {
  color: var(--text-2);
  line-height: 1.6;
}
</style>
