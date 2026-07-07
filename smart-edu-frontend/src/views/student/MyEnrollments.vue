<template>
  <div class="page-card my-enrollments">
    <h2 class="page-title">我的选课</h2>
    <p class="page-subtitle">已选 {{ enrollments.length }} 门课程</p>

    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="3" animated /></div>
    <div v-else-if="enrollments.length === 0" class="empty-hint">
      <p>你还没有选任何课程</p>
      <el-button type="primary" @click="$router.push('/courses')">去课程广场选课</el-button>
    </div>
    <el-table v-else :data="enrollments" stripe class="enroll-table">
      <el-table-column prop="courseName" label="课程名称" min-width="160" />
      <el-table-column prop="teacherName" label="授课教师" width="100" />
      <el-table-column label="上课时间" width="140">
        <template #default="{ row }">
          周{{ dayLabel(row.weekday) }} {{ row.periodStart }}-{{ row.periodEnd }}节
        </template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column prop="credit" label="学分" width="70" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" plain @click="handleDrop(row)">退课</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { enrollmentApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const enrollments = ref<any[]>([])
const loading = ref(false)

const dayLabel = (d: number) => ['','一','二','三','四','五','六','日'][d] || ''

async function fetchData() {
  loading.value = true
  try {
    enrollments.value = await enrollmentApi.myList()
  } catch {
    enrollments.value = []
  } finally {
    loading.value = false
  }
}

async function handleDrop(row: any) {
  try {
    await ElMessageBox.confirm(`确定要退选「${row.courseName}」吗？`, '确认退课', { type: 'warning' })
  } catch { return }

  try {
    await enrollmentApi.drop(row.id)
    ElMessage.success('退课成功')
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '退课失败')
  }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.my-enrollments {
  .page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
  .page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 16px; }
  .enroll-table { margin-top: 8px; }
  .empty-hint { text-align: center; padding: 60px 0; p { color: var(--color-text-muted); margin-bottom: 16px; } }
  .loading-wrap { padding: 40px 0; }
}
</style>
