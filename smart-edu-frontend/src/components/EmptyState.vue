<template>
  <div class="empty-state">
    <div class="empty-state__icon">
      <slot name="icon">
        <el-icon :size="48" color="var(--color-text-muted)"><FolderOpened /></el-icon>
      </slot>
    </div>
    <p class="empty-state__text">{{ description }}</p>
    <el-button
      v-if="actionText"
      type="primary"
      size="default"
      class="empty-state__action"
      @click="$emit('action')"
    >
      {{ actionText }}
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { FolderOpened } from '@element-plus/icons-vue'

defineProps<{
  description: string
  actionText?: string
}>()

defineEmits<{
  action: []
}>()
</script>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-state__icon {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state__text {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0 0 16px;
}

.empty-state__action {
  min-width: 140px;
}
</style>
