<template>
  <div class="stat-card page-card card-hover" :style="{ cursor: clickable ? 'pointer' : 'default' }" @click="clickable && $emit('click')">
    <div class="stat-card__inner">
      <div class="stat-card__icon" :class="'icon-3d-clay icon-3d-' + color + ' icon-3d-md'">
        <el-icon :size="18"><component :is="icon" /></el-icon>
      </div>
      <div class="stat-card__content">
        <div class="stat-card__value">{{ value }}</div>
        <div class="stat-card__label">{{ label }}</div>
      </div>
    </div>
    <div v-if="trend !== undefined" class="stat-card__trend" :class="trend >= 0 ? 'up' : 'down'">
      <el-icon :size="12"><component :is="trend >= 0 ? 'CaretTop' : 'CaretBottom'" /></el-icon>
      {{ Math.abs(trend) }}%
    </div>
  </div>
</template>

<script setup lang="ts">
import { CaretTop, CaretBottom } from '@element-plus/icons-vue'

defineProps<{
  icon: any
  label: string
  value: string | number
  color?: string
  trend?: number
  clickable?: boolean
}>()

defineEmits<{ click: [] }>()
</script>

<style lang="scss" scoped>
.stat-card {
  padding: 16px 20px;
  position: relative;

  &__inner {
    display: flex;
    align-items: center;
    gap: 14px;
  }

  &__content { flex: 1; }

  &__value {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-text);
    line-height: 1.2;
    font-family: 'Space Grotesk', sans-serif;
  }

  &__label {
    font-size: 12px;
    color: var(--color-text-muted);
    margin-top: 2px;
  }

  &__trend {
    position: absolute;
    top: 16px;
    right: 20px;
    display: flex;
    align-items: center;
    gap: 2px;
    font-size: 12px;
    font-weight: 600;
    &.up { color: #34d399; }
    &.down { color: #f87171; }
  }
}
</style>
