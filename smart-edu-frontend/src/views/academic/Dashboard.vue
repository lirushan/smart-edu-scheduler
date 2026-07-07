<template>
  <div class="page-card">
    <PageHeader title="教务工作台" :subtitle="`欢迎，${userStore.userInfo?.realName || '教务老师'}`" />

    <div class="stats-row">
      <StatCard icon="Checked" label="待审课程" :value="stats.pendingCourses" color="orange" />
      <StatCard icon="Clock" label="活跃轮次" :value="stats.activeRounds" color="purple" />
      <StatCard icon="UserFilled" label="选课人数" :value="stats.enrollCount" color="blue" />
      <StatCard icon="Reading" label="开课总数" :value="stats.totalOfferings" color="green" />
    </div>

    <GlassCard title="快捷操作" :gradient-top="true">
      <div class="quick-actions">
        <div class="quick-item card-hover" @click="$router.push('/academic/rounds')">
          <div class="icon-3d-clay icon-3d-purple icon-3d-sm"><el-icon :size="14"><Clock /></el-icon></div>
          <span>选课轮次</span>
        </div>
        <div class="quick-item card-hover" @click="$router.push('/approvals')">
          <div class="icon-3d-clay icon-3d-orange icon-3d-sm"><el-icon :size="14"><Checked /></el-icon></div>
          <span>课程审核</span>
        </div>
      </div>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Checked, Clock, UserFilled, Reading } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import GlassCard from '@/components/GlassCard.vue'
import StatCard from '@/components/StatCard.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const stats = ref({ pendingCourses: 2, activeRounds: 1, enrollCount: 6, totalOfferings: 10 })
</script>

<style lang="scss" scoped>
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.quick-actions { display: flex; gap: 16px; }
.quick-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 20px 24px; border-radius: 12px; background: rgba(139,92,246,0.04); cursor: pointer; font-size: 13px; color: var(--color-text-secondary); }
.quick-item:hover { background: rgba(139,92,246,0.08); color: var(--color-text); }
</style>
