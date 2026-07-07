<template>
  <div class="page-card my-enrollments">
    <PageHeader title="我的选课" :subtitle="`已选 ${enrollments.length} 门课程`" />

    <LoadingState v-if="loading" :rows="3" />
    <EmptyState
      v-else-if="enrollments.length === 0"
      description="你还没有选任何课程"
      action-text="去课程广场选课"
      @action="$router.push('/courses')"
    />
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
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
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
  .enroll-table { margin-top: 8px; }
  .empty-hint { display: none; }
  .loading-wrap { display: none; }
}
</style>
