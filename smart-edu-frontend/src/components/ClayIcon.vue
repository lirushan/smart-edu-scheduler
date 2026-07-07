<template>
  <div class="icon-3d-clay" :class="['icon-3d-' + color, 'icon-3d-' + size]">
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  color?: string
  size?: string
}>(), {
  color: 'purple',
  size: 'md',
})
</script>
