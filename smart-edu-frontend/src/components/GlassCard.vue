<template>
  <div class="glass-card page-card" :class="{ 'gradient-border-top': gradientTop }">
    <div v-if="title" class="glass-card__header">
      <slot name="header">
        <h3 class="glass-card__title">{{ title }}</h3>
      </slot>
    </div>
    <div class="glass-card__body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="glass-card__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title?: string; gradientTop?: boolean }>()
</script>

<style lang="scss" scoped>
.glass-card {
  margin-bottom: 20px;

  &__header {
    padding-bottom: 16px;
    margin-bottom: 16px;
    border-bottom: 1px solid rgba(139, 92, 246, 0.08);
  }

  &__title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text);
  }

  &__body {
    min-height: 60px;
  }

  &__footer {
    padding-top: 16px;
    margin-top: 16px;
    border-top: 1px solid rgba(139, 92, 246, 0.08);
  }
}
</style>
