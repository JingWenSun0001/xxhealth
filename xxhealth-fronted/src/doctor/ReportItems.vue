<template>
  <div>
    <div class="chips">
      <button
        v-for="g in ['全部', ...groups]"
        :key="g"
        type="button"
        :class="{ active: group === g }"
        @click="group = g"
      >
        {{ g }}
      </button>
    </div>
    <div class="table-scroll">
      <table class="data-table editable-table">
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
            v-for="(i, index) in items"
            :key="index"
            v-show="group === '全部' || group === i.group"
          >
            <td>{{ i.name }}</td>
            <td>
              <input
                :value="i.value"
                :aria-label="i.name + '检查结果'"
                placeholder="请输入结果"
                :disabled="disabled"
                @input="update(index, $event.target.value)"
              />
            </td>
            <td>{{ i.unit || "—" }}</td>
            <td>{{ i.range }}</td>
            <td>
              <label
                class="abnormal-toggle"
                :class="i.abnormal ? 'red-text' : 'green-text'"
                ><input
                  type="checkbox"
                  :checked="i.abnormal"
                  :disabled="disabled"
                  @change="toggle(index, $event.target.checked)"
                />{{ i.abnormal ? "↑ 异常" : "正常" }}</label
              >
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>
<script setup>
import { ref, computed } from "vue";
const props = defineProps({ items: Array, disabled: Boolean }),
  emit = defineEmits(["update:items"]),
  group = ref("全部"),
  groups = computed(() => [...new Set(props.items.map((i) => i.group))]);
function update(index, value) {
  const items = props.items.map((i) => ({ ...i })),
    it = items[index];
  it.value = value;
  if (
    it.min !== null &&
    it.max !== null &&
    value.trim() !== "" &&
    !isNaN(Number(value))
  )
    it.abnormal = Number(value) < it.min || Number(value) > it.max;
  emit("update:items", items);
}
function toggle(index, v) {
  const items = props.items.map((i) => ({ ...i }));
  items[index].abnormal = v;
  emit("update:items", items);
}
</script>
