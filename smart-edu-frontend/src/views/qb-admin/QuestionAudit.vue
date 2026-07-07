<template>
  <div class="page-card question-audit">
    <h2 class="page-title">题库审核</h2>
    <p class="page-subtitle">待审核试题 {{ total }} 道</p>

    <el-table :data="questions" stripe v-loading="loading">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="content" label="题目内容" min-width="250" show-overflow-tooltip />
      <el-table-column label="题型" width="80">
        <template #default="{ row }">{{ qTypeLabel(row.questionType) }}</template>
      </el-table-column>
      <el-table-column prop="knowledgePoint" label="知识点" width="100" />
      <el-table-column label="难度" width="70">
        <template #default="{ row }">{{ '⭐'.repeat(row.difficulty || 3) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="audit(row, 1)">通过</el-button>
          <el-button type="danger" size="small" @click="audit(row, 2)">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @change="fetchData" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { questionApi } from '@/api'
import { ElMessage } from 'element-plus'

const questions = ref<any[]>([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)

function qTypeLabel(t: number) { return ['','单选题','多选题','判断题','填空题'][t] || '' }

async function fetchData() {
  loading.value = true
  try {
    const data = await questionApi.auditList({ page: page.value, size: size.value })
    questions.value = data.records || []
    total.value = data.total || 0
  } catch { questions.value = [] } finally { loading.value = false }
}

async function audit(row: any, status: number) {
  try {
    await questionApi.audit(row.id, { auditStatus: status, comment: status === 1 ? '审核通过' : '内容不符合要求' })
    ElMessage.success(status === 1 ? '已通过' : '已驳回')
    fetchData()
  } catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

onMounted(fetchData)
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0; color: var(--color-text); }
.page-subtitle { font-size: 13px; color: var(--color-text-muted); margin: 4px 0 16px; }
</style>
