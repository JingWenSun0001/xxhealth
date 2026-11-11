<template>
  <DoctorLayout v-if="route.path.startsWith('/doctor')"
    ><RouterView /></DoctorLayout
  ><RouterView v-else />
</template>
<script setup>
import { useRoute } from "vue-router";
import DoctorLayout from "./doctor/DoctorLayout.vue";
const route = useRoute();
</script>
