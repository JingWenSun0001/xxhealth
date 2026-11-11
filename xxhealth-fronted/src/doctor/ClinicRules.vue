<template>
  <div class="dialog-form clinic-rules-form">
    <label
      >每日预约容量<input
        v-model.number="modelValue.dailyCapacity"
        type="number"
        min="1"
        max="300"
        required
      /><small>达到上限后停止接受当日新预约</small></label
    ><label
      >标准预约时长<select v-model.number="modelValue.slotMinutes">
        <option :value="15">15分钟</option>
        <option :value="30">30分钟</option>
        <option :value="60">60分钟</option></select
      ><small>新增预约时长为该值的整数倍</small></label
    >
    <div class="full">
      <label>工作日</label>
      <div class="workday-options">
        <label v-for="(d, i) in weekdays" :key="d"
          ><input
            v-model="modelValue.workdays"
            type="checkbox"
            :value="i + 1"
          />{{ d }}</label
        >
      </div>
    </div>
    <label
      >上午开始<input
        v-model="modelValue.morningStart"
        type="time"
        required /></label
    ><label
      >上午结束<input
        v-model="modelValue.morningEnd"
        type="time"
        required /></label
    ><label
      >下午开始<input
        v-model="modelValue.afternoonStart"
        type="time"
        required /></label
    ><label
      >下午结束<input
        v-model="modelValue.afternoonEnd"
        type="time"
        required /></label
    ><label class="full"
      >预约须知<textarea
        v-model="modelValue.bookingNotes"
        rows="4"
        maxlength="1000"
      ></textarea>
    </label>
  </div>
</template>
<script setup>
defineProps({ modelValue: { type: Object, required: true } });
const weekdays = ["周一", "周二", "周三", "周四", "周五", "周六", "周日"];
</script>
