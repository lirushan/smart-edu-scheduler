<template>
  <div class="page-card my-scores">
    <h2 class="page-title">我的成绩</h2>
    <p class="page-subtitle">累计绩点 GPA: {{ avgGpa }}</p>

    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="4" animated /></div>
    <div v-else-if="scores.length === 0" class="empty-hint">暂无成绩记录</div>
    <el-table v-else :data="scores" stripe class="score-table">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="courseName" label="课程" min-width="150" />
      <el-table-column prop="credit" label="学分" width="70" />
      <el-table-column label="百分制" width="90">
        <template #default="{ row }">{{ row.rawScore }}分</template>
      </el-table-column>
      <el-table-column prop="gradeLevel" label="五级制" width="90">
        <template #default="{ row }">
          <el-tag :type="gradeTagType(row.gradeLevel)" size="small">{{ row.gradeLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="gpa" label="GPA" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { scoreApi } from '@/api'

const scores = ref<any[]>([])
const loading = ref(false)

const avgGpa = computed(() => {
  if (scores.value.length === 0) return '0.0'
  const sum = scores.value.reduce((a: number, s: any) => a + (parseFloat(s.gpa) || 0), 0)
  return (sum / scores.value.length).toFixed(1)
})

function gradeTagType(level: string) {
  const map: Record<string, string> = { '优秀': 'success', '良好': '', '中等': 'warning', '及格': 'warning', '不及格': 'danger' }
  return map[level] || 'info'
}

onMounted(async () => {
  loading.value = true
  try {
    scores.value = await scoreApi.myScores()
  } catch {
    scores.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style lang="scss" scoped>
.my-scores {
  .page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
  .page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 16px; }
  .score-table { margin-top: 8px; }
  .empty-hint { text-align: center; color: var(--color-text-muted); padding: 60px 0; }
  .loading-wrap { padding: 40px 0; }
}
</style>
