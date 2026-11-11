<template>
  <component
    :is="icons[name] || icons.Document"
    class="ui-icon"
    aria-hidden="true"
  />
</template>
<script setup>
import * as icons from "@element-plus/icons-vue";
defineProps({ name: String });
</script>
