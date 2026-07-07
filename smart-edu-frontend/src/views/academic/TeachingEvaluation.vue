<template>
  <div class="page-card">
    <PageHeader title="教学评价" />

    <!-- 统计列表 -->
    <GlassCard title="评价统计" :gradient-top="true">
      <el-table :data="stats" stripe v-loading="loading">
        <el-table-column prop="teacherName" label="教师" min-width="120" />
        <el-table-column label="均分" width="100">
          <template #default="{ row }">{{ row.avgScore ? row.avgScore.toFixed(2) : '-' }}</template>
        </el-table-column>
        <el-table-column prop="evalCount" label="评价人数" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" link @click="showDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </GlassCard>

    <!-- 评价详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="'评价详情 — ' + selectedTeacher" width="700px">
      <el-table :data="details" stripe max-height="400">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="courseName" label="课程" width="140" />
        <el-table-column label="教学质量" width="80"><template #default="{ row }">{{ row.score1 }}</template></el-table-column>
        <el-table-column label="课程内容" width="80"><template #default="{ row }">{{ row.score2 }}</template></el-table-column>
        <el-table-column label="课堂氛围" width="80"><template #default="{ row }">{{ row.score3 }}</template></el-table-column>
        <el-table-column label="师生互动" width="80"><template #default="{ row }">{{ row.score4 }}</template></el-table-column>
        <el-table-column label="综合评价" width="80"><template #default="{ row }">{{ row.score5 }}</template></el-table-column>
        <el-table-column label="均分" width="70">
          <template #default="{ row }">
            {{ ((row.score1 + row.score2 + row.score3 + row.score4 + row.score5) / 5).toFixed(1) }}
          </template>
        </el-table-column>
      </el-table>
      <el-table :data="details" stripe max-height="200" style="margin-top:16px">
        <el-table-column prop="studentName" label="学生" width="100" />
        <el-table-column prop="comment" label="文字评价" min-width="300">
          <template #default="{ row }">
            <span v-if="row.comment">{{ row.comment }}</span>
            <span v-else style="color:var(--color-text-muted)">暂无文字评价</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { academicEvaluationApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import GlassCard from '@/components/GlassCard.vue'

const stats = ref<any[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const details = ref<any[]>([])
const selectedTeacher = ref('')

async function fetchStats() {
  loading.value = true
  try { stats.value = await academicEvaluationApi.stats() } catch { stats.value = [] } finally { loading.value = false }
}

async function showDetail(row: any) {
  selectedTeacher.value = row.teacherName
  detailVisible.value = true
  try { details.value = await academicEvaluationApi.teacherDetails(row.teacherId) } catch { details.value = [] }
}

onMounted(fetchStats)
</script>

<style lang="scss" scoped>
</style>
