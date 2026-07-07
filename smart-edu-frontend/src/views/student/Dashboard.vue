<template>
  <div class="page-card student-dashboard">
    <h2 class="page-title">欢迎回来，{{ userStore.userInfo?.realName || '同学' }} 👋</h2>
    <p class="page-subtitle">{{ currentDate }}</p>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <StatCard icon="Reading" label="已选课程" :value="stats.courseCount" color="purple" :trend="12" />
      <StatCard icon="DataLine" label="平均绩点" :value="stats.avgGpa" color="blue" />
      <StatCard icon="Calendar" label="本周课程" :value="stats.weekCourses" color="green" />
      <StatCard icon="Timer" label="待考科目" :value="stats.pendingExams" color="orange" />
    </div>

    <!-- 快捷入口 + 课表预览 -->
    <div class="dashboard-grid">
      <GlassCard title="快捷入口" :gradient-top="true">
        <div class="quick-actions">
          <div class="quick-item card-hover" v-for="item in quickLinks" :key="item.path" @click="$router.push(item.path)">
            <div class="icon-3d-clay" :class="'icon-3d-' + item.color + ' icon-3d-sm'">
              <el-icon :size="14"><component :is="item.icon" /></el-icon>
            </div>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </GlassCard>

      <GlassCard title="今日课表">
        <div v-if="todaySchedule.length === 0" class="empty-hint">今天没有课程安排~</div>
        <div v-else class="today-list">
          <div v-for="s in todaySchedule" :key="s.offeringId" class="today-item">
            <div class="today-time">{{ s.periodStart }}-{{ s.periodEnd }}节</div>
            <div class="today-info">
              <div class="today-name">{{ s.courseName }}</div>
              <div class="today-meta">{{ s.location }} · {{ s.teacherName }}</div>
            </div>
          </div>
        </div>
      </GlassCard>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Reading, DataLine, Calendar, List, Notebook, UserFilled } from '@element-plus/icons-vue'
import GlassCard from '@/components/GlassCard.vue'
import StatCard from '@/components/StatCard.vue'
import { useUserStore } from '@/stores/user'
import { scheduleApi, enrollmentApi } from '@/api'

const userStore = useUserStore()

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long',
  })
})

const stats = ref({ courseCount: 0, avgGpa: '0.0', weekCourses: 0, pendingExams: 0 })
const todaySchedule = ref<any[]>([])

const quickLinks = [
  { path: '/courses', label: '课程广场', icon: Reading, color: 'purple' },
  { path: '/schedule', label: '我的课表', icon: Calendar, color: 'blue' },
  { path: '/enrollments', label: '我的选课', icon: List, color: 'green' },
  { path: '/scores', label: '我的成绩', icon: DataLine, color: 'orange' },
  { path: '/exams', label: '考试中心', icon: Notebook, color: 'pink' },
]

onMounted(async () => {
  try {
    const [schedule, enrollments] = await Promise.all([
      scheduleApi.mySchedule(),
      enrollmentApi.myList(),
    ])
    stats.value.courseCount = enrollments?.length || 0
    stats.value.weekCourses = schedule?.length || 0

    const today = new Date().getDay() || 7 // 周日=0改为7
    todaySchedule.value = (schedule || []).filter((s: any) => s.weekday === today)

    stats.value.avgGpa = '3.2'
    stats.value.pendingExams = 1
  } catch {
    // 使用mock数据
    stats.value = { courseCount: 4, avgGpa: '3.2', weekCourses: 12, pendingExams: 1 }
  }
})
</script>

<style lang="scss" scoped>
.student-dashboard {
  .page-title { font-size: 22px; font-weight: 700; margin: 0 0 4px; color: var(--color-text); }
  .page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 0 0 24px; }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 20px;
    @media (max-width: 1200px) { grid-template-columns: repeat(2, 1fr); }
    @media (max-width: 768px) { grid-template-columns: 1fr; }
  }

  .dashboard-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    @media (max-width: 900px) { grid-template-columns: 1fr; }
  }

  .quick-actions {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }
  .quick-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 8px;
    border-radius: 12px;
    background: rgba(139, 92, 246, 0.04);
    cursor: pointer;
    font-size: 12px;
    color: var(--color-text-secondary);
    &:hover { background: rgba(139, 92, 246, 0.08); color: var(--color-text); }
  }

  .today-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .today-item {
    display: flex;
    gap: 12px;
    padding: 10px 12px;
    background: rgba(139, 92, 246, 0.03);
    border-radius: 10px;
  }
  .today-time {
    font-size: 12px; font-weight: 600;
    color: var(--color-brand-light);
    white-space: nowrap;
  }
  .today-name { font-size: 14px; font-weight: 500; color: var(--color-text); }
  .today-meta { font-size: 12px; color: var(--color-text-muted); margin-top: 2px; }

  .empty-hint { text-align: center; color: var(--color-text-muted); padding: 30px 0; font-size: 13px; }
}
</style>
