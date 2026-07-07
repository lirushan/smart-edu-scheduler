<template>
  <div class="page-header">
    <div class="page-header__text">
      <h2 class="page-header__title">{{ title }}</h2>
      <p v-if="subtitle" class="page-header__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.toolbar" class="page-header__toolbar">
      <slot name="toolbar" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 8px;
}

.page-header__title {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: var(--color-text);
}

.page-header__subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
  margin: 4px 0 0;
}

.page-header__toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
</style>
