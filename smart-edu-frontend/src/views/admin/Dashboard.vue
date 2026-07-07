<template>
  <div class="page-card">
    <h2 class="page-title">系统管理后台</h2>
    <p class="page-subtitle">全局系统概览</p>
    <div class="stats-row">
      <StatCard icon="UserFilled" label="用户总数" :value="stats.userCount" color="purple" />
      <StatCard icon="Reading" label="课程总数" :value="stats.courseCount" color="blue" />
      <StatCard icon="List" label="选课记录" :value="stats.enrollCount" color="green" />
      <StatCard icon="Checked" label="待审核" :value="stats.pendingCount" color="orange" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { UserFilled, Reading, List, Checked } from '@element-plus/icons-vue'
import StatCard from '@/components/StatCard.vue'

const stats = ref({ userCount: 10, courseCount: 10, enrollCount: 6, pendingCount: 2 })
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 20px; }
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }
</style>
