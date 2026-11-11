<template>
  <div class="pagination">
    <span>共 {{ total.toLocaleString() }} 条</span>
    <select
      v-if="sizes"
      :value="pageSize"
      @change="$emit('size', Number($event.target.value))"
    >
      <option v-if="pageSize===6" :value="6">6条/页</option><option :value="10">10条/页</option>
      <option :value="20">20条/页</option>
      <option :value="50">50条/页</option>
    </select>
    <button :disabled="page <= 1" @click="change(page - 1)">
      <UiIcon name="ArrowLeft" />
    </button>
    <button
      v-for="p in pages"
      :key="p"
      :class="{ active: p === page }"
      @click="change(p)"
    >
      {{ p }}
    </button>
    <span v-if="count > 5">…</span
    ><button v-if="count > 5" @click="change(count)">{{ count }}</button>
    <button :disabled="page >= count" @click="change(page + 1)">
      <UiIcon name="ArrowRight" />
    </button>
    <span>前往</span
    ><input
      v-model="jump"
      type="number"
      min="1"
      :max="count"
      aria-label="跳转页码"
      @keydown.enter="change(Number(jump))"
      @change="change(Number(jump))"
    /><span>页</span>
  </div>
</template>
<script setup>
import { computed, ref } from "vue";
import UiIcon from "./UiIcon.vue";
const props = defineProps({
  total: { type: Number, default: 0 },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  sizes: Boolean,
});
const emit = defineEmits(["change", "size"]);
const jump = ref(1);
const count = computed(() =>
  Math.max(1, Math.ceil(props.total / props.pageSize))
);
const pages = computed(() =>
  Array.from(
    { length: Math.min(5, count.value) },
    (_, i) => Math.max(1, Math.min(props.page - 2, count.value - 4)) + i
  )
);
function change(p) {
  if (p >= 1 && p <= count.value) {
    jump.value = p;
    emit("change", p);
  }
}
</script>
