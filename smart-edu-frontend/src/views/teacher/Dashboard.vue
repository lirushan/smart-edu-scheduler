<template>
  <div class="page-card teacher-dashboard">
    <h2 class="page-title">教师工作台</h2>
    <p class="page-subtitle">欢迎，{{ userStore.userInfo?.realName || '老师' }}</p>

    <div class="stats-row">
      <StatCard icon="Reading" label="授课课程" :value="stats.courseCount" color="purple" />
      <StatCard icon="UserFilled" label="学生总数" :value="stats.studentCount" color="blue" />
      <StatCard icon="DataLine" label="待录入成绩" :value="stats.pendingScores" color="orange" />
      <StatCard icon="Collection" label="题库试题" :value="stats.questionCount" color="green" />
    </div>

    <GlassCard title="快捷操作" :gradient-top="true">
      <div class="quick-actions">
        <div class="quick-item card-hover" v-for="item in quickLinks" :key="item.path" @click="$router.push(item.path)">
          <div class="icon-3d-clay" :class="'icon-3d-' + item.color + ' icon-3d-sm'">
            <el-icon :size="14"><component :is="item.icon" /></el-icon>
          </div>
          <span>{{ item.label }}</span>
        </div>
      </div>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Reading, UserFilled, DataLine, Collection } from '@element-plus/icons-vue'
import GlassCard from '@/components/GlassCard.vue'
import StatCard from '@/components/StatCard.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const stats = ref({ courseCount: 4, studentCount: 120, pendingScores: 15, questionCount: 36 })

const quickLinks = [
  { path: '/schedule', label: '我的课表', icon: Reading, color: 'purple' },
  { path: '/teacher/scores', label: '成绩录入', icon: DataLine, color: 'blue' },
  { path: '/teacher/questions', label: '题库管理', icon: Collection, color: 'green' },
]
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 20px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }

.quick-actions { display: flex; gap: 16px; }
.quick-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 20px 24px; border-radius: 12px;
  background: rgba(139,92,246,0.04); cursor: pointer;
  font-size: 13px; color: var(--color-text-secondary);
  &:hover { background: rgba(139,92,246,0.08); color: var(--color-text); }
}
</style>
