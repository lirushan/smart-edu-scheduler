<template>
  <div class="loading-state">
    <el-skeleton :rows="rows" animated />
    <p v-if="text" class="loading-state__text">{{ text }}</p>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  rows?: number
  text?: string
}>(), {
  rows: 4,
})
</script>

<style lang="scss" scoped>
.loading-state {
  padding: 40px 0;
}

.loading-state__text {
  margin-top: 12px;
  font-size: 13px;
  color: var(--color-text-muted);
  text-align: center;
}
</style>
