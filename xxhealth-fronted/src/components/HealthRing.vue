<template>
  <div class="health-ring" :style="{ width: size + 'px', height: size + 'px' }">
    <svg :width="size" :height="size" :viewBox="`0 0 ${size} ${size}`">
      <circle
        :cx="center"
        :cy="center"
        :r="radius"
        fill="none"
        stroke="#eef2f9"
        :stroke-width="stroke"
      />
      <circle
        :cx="center"
        :cy="center"
        :r="radius"
        fill="none"
        :stroke="color"
        :stroke-width="stroke"
        stroke-linecap="round"
        :stroke-dasharray="dashArray"
        :transform="`rotate(-90 ${center} ${center})`"
      />
    </svg>
    <div class="ring-center">
      <strong :style="{ color, fontSize: scoreFont + 'px' }">{{ score }}<small>%</small></strong>
      <span v-if="label">{{ label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  score: { type: Number, default: 0 },
  size: { type: Number, default: 150 },
  stroke: { type: Number, default: 12 },
  label: { type: String, default: '健康评分' },
  color: { type: String, default: '' }
})

const center = computed(() => props.size / 2)
const radius = computed(() => props.size / 2 - props.stroke - 4)
const scoreFont = computed(() => Math.round(props.size * 0.22))

const color = computed(() => {
  if (props.color) return props.color
  if (props.score >= 90) return '#12b76a'
  if (props.score >= 70) return '#f79009'
  return '#f04438'
})

const dashArray = computed(() => {
  const c = 2 * Math.PI * radius.value
  return `${(props.score / 100) * c} ${c}`
})
</script>

<style scoped>
.health-ring {
  position: relative;
  flex-shrink: 0;
}

.ring-center {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.ring-center strong {
  font-weight: 800;
  line-height: 1;
}

.ring-center strong small {
  font-size: 0.42em;
  margin-left: 2px;
}

.ring-center span {
  font-size: 12px;
  color: var(--text-3);
}
</style>
