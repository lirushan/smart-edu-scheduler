<template>
  <div class="page-card my-schedule">
    <h2 class="page-title">我的课表</h2>
    <p class="page-subtitle">学期: 2024-2025-1</p>

    <div class="schedule-table-wrap">
      <table class="schedule-table">
        <thead>
          <tr>
            <th class="time-col">节次</th>
            <th v-for="d in 7" :key="d" :class="{ today: d === today }">
              {{ dayLabels[d-1] }}
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in 8" :key="p">
            <td class="time-col">第{{ p }}节</td>
            <td v-for="d in 7" :key="d" class="cell"
              :class="{ today: d === today, has-course: getCourse(d, p) }"
            >
              <div v-if="getCourse(d, p)" class="schedule-item" :style="{ background: getColor(getCourse(d,p)?.courseName || '') }">
                <div class="s-name">{{ getCourse(d, p)?.courseName }}</div>
                <div class="s-loc">{{ getCourse(d, p)?.location }}</div>
                <div class="s-teacher">{{ getCourse(d, p)?.teacherName }}</div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { scheduleApi } from '@/api'

const dayLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const today = new Date().getDay() || 7

const scheduleData = ref<any[]>([])

const colors = [
  'linear-gradient(135deg, rgba(139,92,246,0.25), rgba(102,126,234,0.2))',
  'linear-gradient(135deg, rgba(59,130,246,0.2), rgba(37,99,235,0.15))',
  'linear-gradient(135deg, rgba(16,185,129,0.2), rgba(5,150,105,0.15))',
  'linear-gradient(135deg, rgba(245,158,11,0.2), rgba(234,88,12,0.15))',
  'linear-gradient(135deg, rgba(236,72,153,0.2), rgba(219,39,119,0.15))',
  'linear-gradient(135deg, rgba(139,92,246,0.18), rgba(102,126,234,0.12))',
]

function getColor(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) hash = name.charCodeAt(i) + ((hash << 5) - hash)
  return colors[Math.abs(hash) % colors.length]
}

function getCourse(day: number, period: number) {
  return scheduleData.value.find(s =>
    s.weekday === day && period >= s.periodStart && period <= s.periodEnd
  )
}

onMounted(async () => {
  try {
    scheduleData.value = await scheduleApi.mySchedule()
  } catch {
    scheduleData.value = []
  }
})
</script>

<style lang="scss" scoped>
.my-schedule {
  .page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
  .page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 16px; }

  .schedule-table-wrap { overflow-x: auto; }

  .schedule-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 13px;

    th, td { border: 1px solid rgba(139,92,246,0.08); padding: 6px 8px; text-align: center; min-width: 100px; height: 56px; }
    th { color: var(--color-text-secondary); font-weight: 500; font-size: 12px; }
    th.today { background: rgba(139,92,246,0.08); color: var(--color-brand-light); }

    .time-col { color: var(--color-text-muted); font-size: 11px; min-width: 60px; font-weight: 500; }

    .cell { position: relative; }
    .cell.today { background: rgba(139,92,246,0.03); }

    .schedule-item {
      position: absolute; inset: 2px;
      border-radius: 6px; padding: 4px 6px;
      display: flex; flex-direction: column; justify-content: center;
      backdrop-filter: blur(4px); border: 1px solid rgba(139,92,246,0.1);
      .s-name { font-size: 12px; font-weight: 600; color: var(--color-text); }
      .s-loc { font-size: 10px; color: var(--color-text-secondary); }
      .s-teacher { font-size: 10px; color: var(--color-text-muted); }
    }
  }
}
</style>
