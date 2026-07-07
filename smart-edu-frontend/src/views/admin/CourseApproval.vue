<template>
  <div class="page-card course-approval">
    <h2 class="page-title">课程审核</h2>
    <p class="page-subtitle">待审核课程 {{ total }} 门</p>

    <el-table :data="approvals" stripe v-loading="loading">
      <el-table-column prop="courseName" label="课程名称" min-width="150" />
      <el-table-column prop="teacherName" label="授课教师" width="100" />
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column label="时间" width="140">
        <template #default="{ row }">周{{ dayLabel(row.weekday) }} {{ row.periodStart }}-{{ row.periodEnd }}节</template>
      </el-table-column>
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column label="容量" width="80">
        <template #default="{ row }">{{ row.enrolledCount }}/{{ row.capacity }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="approve(row)">通过</el-button>
          <el-button type="danger" size="small" @click="showReject(row)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @change="fetchData" />
    </div>

    <!-- 驳回弹窗 -->
    <el-dialog v-model="rejectVisible" title="驳回课程" width="400px" append-to-body>
      <el-input v-model="rejectComment" type="textarea" :rows="3" placeholder="请输入驳回理由..." />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { approvalApi } from '@/api'
import { ElMessage } from 'element-plus'

const approvals = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const rejectVisible = ref(false)
const rejectComment = ref('')
const rejectTarget = ref<any>(null)

const dayLabel = (d: number) => ['','一','二','三','四','五','六','日'][d] || ''

async function fetchData() {
  loading.value = true
  try {
    const data = await approvalApi.list({ page: page.value, size: size.value })
    approvals.value = data.records || []
    total.value = data.total || 0
  } catch { approvals.value = [] } finally { loading.value = false }
}

async function approve(row: any) {
  try { await approvalApi.approve(row.id); ElMessage.success('已通过'); fetchData() }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

function showReject(row: any) { rejectTarget.value = row; rejectComment.value = ''; rejectVisible.value = true }

async function doReject() {
  if (!rejectComment.value.trim()) { ElMessage.warning('请填写驳回理由'); return }
  try {
    await approvalApi.reject(rejectTarget.value.id, rejectComment.value)
    ElMessage.success('已驳回'); rejectVisible.value = false; fetchData()
  } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 16px; }
</style>
