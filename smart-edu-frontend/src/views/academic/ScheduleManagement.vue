<template>
  <div class="page-card">
    <PageHeader title="排课管理" />

    <!-- 筛选区 -->
    <GlassCard :gradient-top="true" style="margin-bottom:16px">
      <div class="filter-row">
        <el-select v-model="filterOfferingId" placeholder="选择课程" clearable filterable style="width:280px" @change="fetchSchedules">
          <el-option v-for="o in offerings" :key="o.id" :label="o.courseName + ' — ' + (o.teacherName || '')" :value="o.id" />
        </el-select>
        <el-select v-model="filterSemester" placeholder="选择学期" clearable style="width:200px" @change="fetchSchedules">
          <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </GlassCard>

    <!-- 课表 8×7 周视图 -->
    <GlassCard title="课程表">
      <div class="schedule-grid">
        <div class="schedule-header">
          <div class="header-cell corner">节次</div>
          <div v-for="d in 7" :key="d" class="header-cell">{{ weekLabels[d - 1] }}</div>
        </div>
        <div v-for="p in 8" :key="p" class="schedule-row">
          <div class="period-cell">第{{ p }}节</div>
          <div v-for="d in 7" :key="d" class="schedule-cell" :class="{ 'has-course': getCourse(p, d) }">
            <div v-if="getCourse(p, d)" class="course-block" :style="{ background: getCourseColor(getCourse(p, d)!.courseName || '') }">
              <div class="course-name">{{ getCourse(p, d)!.courseName }}</div>
              <div class="course-teacher">{{ getCourse(p, d)!.teacherName }}</div>
              <div class="course-location">{{ getCourse(p, d)!.location }}</div>
            </div>
          </div>
        </div>
      </div>
      <div v-if="schedules.length === 0" class="empty-hint">暂无课表数据，请选择筛选条件</div>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { academicScheduleApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import GlassCard from '@/components/GlassCard.vue'

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const schedules = ref<any[]>([])
const offerings = ref<any[]>([])
const semesters = ref<string[]>([])
const filterOfferingId = ref<number | null>(null)
const filterSemester = ref<string | null>(null)

const colorPool = [
  'rgba(139,92,246,0.25)', 'rgba(59,130,246,0.25)', 'rgba(34,197,94,0.25)',
  'rgba(249,115,22,0.25)', 'rgba(236,72,153,0.25)', 'rgba(20,184,166,0.25)',
  'rgba(168,85,247,0.25)', 'rgba(251,191,36,0.25)',
]
const colorMap: Record<string, string> = {}

function getCourseColor(name: string): string {
  if (!colorMap[name]) {
    colorMap[name] = colorPool[Object.keys(colorMap).length % colorPool.length]
  }
  return colorMap[name]
}

function getCourse(period: number, day: number) {
  return schedules.value.find((s: any) =>
    s.weekday === day && period >= s.periodStart && period <= s.periodEnd
  )
}

async function fetchSchedules() {
  const params: any = {}
  if (filterOfferingId.value) params.offeringId = filterOfferingId.value
  if (filterSemester.value) params.semester = filterSemester.value
  try { schedules.value = await academicScheduleApi.schedules(params) } catch { schedules.value = [] }
}

async function loadFilters() {
  try {
    const [offRes, semRes] = await Promise.all([
      academicScheduleApi.offerings(),
      academicScheduleApi.semesters(),
    ])
    offerings.value = offRes
    semesters.value = semRes
  } catch { /* ignore */ }
}

function resetFilters() {
  filterOfferingId.value = null
  filterSemester.value = null
  fetchSchedules()
}

onMounted(() => { loadFilters(); fetchSchedules() })
</script>

<style lang="scss" scoped>
.filter-row { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.schedule-grid { border-radius: 12px; overflow: hidden; border: 1px solid rgba(139,92,246,0.12); }
.schedule-header, .schedule-row { display: grid; grid-template-columns: 72px repeat(7, 1fr); }
.header-cell { padding: 10px 4px; text-align: center; font-size: 13px; font-weight: 600; color: var(--color-text); background: rgba(139,92,246,0.06); border-bottom: 1px solid rgba(139,92,246,0.08); }
.corner { color: var(--color-text-muted); font-size: 12px; }
.period-cell { padding: 10px 4px; text-align: center; font-size: 11px; color: var(--color-text-muted); background: rgba(139,92,246,0.03); border-right: 1px solid rgba(139,92,246,0.06); display: flex; align-items: center; justify-content: center; }
.schedule-cell { min-height: 64px; padding: 2px; border-bottom: 1px solid rgba(139,92,246,0.04); border-right: 1px solid rgba(139,92,246,0.04); }
.schedule-cell.has-course { padding: 0; }
.course-block { height: 100%; padding: 6px 8px; border-radius: 4px; display: flex; flex-direction: column; gap: 1px; }
.course-name { font-size: 12px; font-weight: 600; color: var(--color-text); }
.course-teacher { font-size: 10px; color: var(--color-text-secondary); }
.course-location { font-size: 10px; color: var(--color-text-muted); margin-top: auto; }
.empty-hint { text-align: center; padding: 40px; color: var(--color-text-muted); font-size: 13px; }
</style>
