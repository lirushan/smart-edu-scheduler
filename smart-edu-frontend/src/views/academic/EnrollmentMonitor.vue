<template>
  <div class="page-card">
    <PageHeader title="选课监控" />

    <!-- 统计仪表盘 -->
    <div class="stats-row">
      <StatCard icon="Reading" label="总课程数" :value="stats.totalOfferings || 0" color="purple" />
      <StatCard icon="UserFilled" label="总选课人数" :value="stats.totalEnrollments || 0" color="blue" />
      <StatCard icon="TrendCharts" label="总容量" :value="stats.totalCapacity || 0" color="green" />
      <StatCard icon="DataLine" label="容量使用率" :value="usagePercent" color="orange" />
    </div>

    <!-- 课程选课列表 -->
    <GlassCard title="课程选课详情" :gradient-top="true">
      <div class="toolbar">
        <el-button type="primary" size="small" @click="exportCSV">导出 CSV</el-button>
      </div>
      <el-table :data="details" stripe v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="teacherName" label="教师" min-width="100" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="capacity" label="容量" width="80" />
        <el-table-column prop="enrolledCount" label="已选" width="80" />
        <el-table-column label="选课比率" min-width="200">
          <template #default="{ row }">
            <div class="progress-wrapper">
              <el-progress
                :percentage="Math.round((row.fillRate || 0) * 100)"
                :color="progressColor(row.fillRate)"
                :stroke-width="18"
              />
              <span class="progress-text">{{ row.enrolledCount }} / {{ row.capacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.fillRate >= 0.8 ? 'danger' : row.fillRate >= 0.5 ? 'warning' : 'success'" size="small">
              {{ row.fillRate >= 0.8 ? '紧张' : row.fillRate >= 0.5 ? '适中' : '充裕' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { academicEnrollmentApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import GlassCard from '@/components/GlassCard.vue'
import StatCard from '@/components/StatCard.vue'
import { Reading, UserFilled, TrendCharts, DataLine } from '@element-plus/icons-vue'

const stats = ref<any>({})
const details = ref<any[]>([])
const loading = ref(false)

const usagePercent = computed(() => {
  if (!stats.value.capacityUsageRate) return '0%'
  return Math.round(stats.value.capacityUsageRate * 100) + '%'
})

function progressColor(rate: number): string {
  if (rate >= 0.8) return '#f87171'
  if (rate >= 0.5) return '#fbbf24'
  return '#34d399'
}

async function fetchData() {
  loading.value = true
  try {
    const [s, d] = await Promise.all([
      academicEnrollmentApi.stats(),
      academicEnrollmentApi.details(),
    ])
    stats.value = s
    details.value = d
  } catch { /* ignore */ } finally { loading.value = false }
}

function exportCSV() {
  const headers = ['课程名称', '教师', '学期', '容量', '已选', '选课比率']
  const rows = details.value.map(d => [
    d.courseName, d.teacherName, d.semester,
    d.capacity, d.enrolledCount,
    Math.round((d.fillRate || 0) * 100) + '%',
  ])
  const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url; a.download = '选课监控_' + new Date().toISOString().slice(0, 10) + '.csv'; a.click()
  URL.revokeObjectURL(url)
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.toolbar { display: flex; justify-content: flex-end; margin-bottom: 12px; }
.progress-wrapper { display: flex; align-items: center; gap: 12px; }
.progress-text { font-size: 12px; color: var(--color-text-secondary); white-space: nowrap; }
</style>
