<template>
  <div class="page-card">
    <PageHeader title="成绩审核" />

    <!-- 筛选区 -->
    <div class="toolbar">
      <div class="filter-group">
        <el-select v-model="filterStatus" placeholder="审核状态" clearable style="width:140px" @change="fetchScores">
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
        </el-select>
      </div>
      <div>
        <el-button type="primary" :disabled="selectedIds.length === 0" @click="batchApprove">
          批量发布 ({{ selectedIds.length }})
        </el-button>
        <el-button :disabled="selectedIds.length === 0" @click="batchReject">
          批量驳回 ({{ selectedIds.length }})
        </el-button>
      </div>
    </div>

    <el-table :data="scores" stripe v-loading="loading" @selection-change="onSelectionChange">
      <el-table-column type="selection" width="44" />
      <el-table-column prop="studentName" label="学生" min-width="100" />
      <el-table-column prop="courseName" label="课程" min-width="140" />
      <el-table-column prop="rawScore" label="百分制分数" width="110" />
      <el-table-column prop="gradeLevel" label="五级制" width="90" />
      <el-table-column prop="gpa" label="GPA" width="70" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.status !== 1" size="small" link type="success" @click="approve(row)">发布</el-button>
          <el-button v-if="row.status === 1" size="small" link type="warning" @click="reject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      layout="total, prev, pager, next"
      :total="total" :page-size="pageSize" v-model:current-page="currentPage"
      @current-change="fetchScores"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { academicScoreApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const scores = ref<any[]>([])
const loading = ref(false)
const filterStatus = ref<number | null>(null)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedIds = ref<number[]>([])

function onSelectionChange(rows: any[]) { selectedIds.value = rows.map(r => r.id) }

async function fetchScores() {
  loading.value = true
  try {
    const params: any = { page: currentPage.value, size: pageSize.value }
    if (filterStatus.value !== null && filterStatus.value !== undefined) {
      params.status = filterStatus.value
    }
    const res: any = await academicScoreApi.reviewList(params)
    scores.value = res.records || []
    total.value = res.total || 0
  } catch { scores.value = [] } finally { loading.value = false }
}

async function approve(row: any) {
  try { await academicScoreApi.approve(row.id); ElMessage.success('已发布'); fetchScores() }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

async function batchApprove() {
  try { await ElMessageBox.confirm(`确定批量发布 ${selectedIds.value.length} 条成绩？`, '确认', { type: 'warning' }) } catch { return }
  try { await academicScoreApi.batchApprove(selectedIds.value); ElMessage.success('批量发布成功'); selectedIds.value = []; fetchScores() }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

async function reject(row: any) {
  try { await ElMessageBox.confirm('确定驳回该成绩（退回草稿）？', '确认', { type: 'warning' }) } catch { return }
  try { await academicScoreApi.reject(row.id); ElMessage.success('已驳回'); fetchScores() }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

async function batchReject() {
  try { await ElMessageBox.confirm(`确定批量驳回 ${selectedIds.value.length} 条成绩？`, '确认', { type: 'warning' }) } catch { return }
  try {
    for (const id of selectedIds.value) await academicScoreApi.reject(id)
    ElMessage.success('批量驳回完成'); selectedIds.value = []; fetchScores()
  } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

onMounted(fetchScores)
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.filter-group { display: flex; gap: 12px; }
</style>
