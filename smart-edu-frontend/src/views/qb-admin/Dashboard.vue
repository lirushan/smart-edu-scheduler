<template>
  <div class="page-card">
    <h2 class="page-title">题库管理工作台</h2>
    <p class="page-subtitle">全局题库维护与审核</p>
    <div class="stats-row">
      <StatCard icon="Collection" label="题库总数" :value="stats.totalQuestions" color="purple" />
      <StatCard icon="Checked" label="待审核" :value="stats.pendingAudit" color="orange" />
      <StatCard icon="Document" label="知识点" :value="stats.knowledgeCount" color="blue" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Collection, Checked, Document } from '@element-plus/icons-vue'
import StatCard from '@/components/StatCard.vue'

const stats = ref({ totalQuestions: 4, pendingAudit: 0, knowledgeCount: 3 })
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 20px; }
.stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
</style>
