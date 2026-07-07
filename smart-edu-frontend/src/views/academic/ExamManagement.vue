<template>
  <div class="page-card">
    <PageHeader title="考试管理" />

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索考试名称" clearable style="width:220px" @input="fetchExams" />
      <el-button type="primary" @click="showDialog()">新增考试</el-button>
    </div>

    <el-table :data="exams" stripe v-loading="loading">
      <el-table-column prop="examName" label="考试名称" min-width="180" />
      <el-table-column prop="courseName" label="关联课程" min-width="140" />
      <el-table-column label="考试时间" width="300">
        <template #default="{ row }">{{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" />
      <el-table-column prop="totalScore" label="满分" width="80" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'info' : row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 0 ? '未开始' : row.status === 1 ? '进行中' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" link @click="showDialog(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="deleteExam(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      layout="total, prev, pager, next"
      :total="total" :page-size="pageSize" v-model:current-page="currentPage"
      @current-change="fetchExams"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑考试' : '新增考试'" width="520px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="考试名称"><el-input v-model="form.examName" /></el-form-item>
        <el-form-item label="关联课程">
          <el-select v-model="form.offeringId" filterable placeholder="选择课程" style="width:100%">
            <el-option v-for="o in offerings" :key="o.id" :label="(o.courseName || '') + ' — ' + (o.teacherName || '')" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="时长(分钟)"><el-input-number v-model="form.durationMinutes" :min="10" :max="480" /></el-form-item>
        <el-form-item label="满分"><el-input-number v-model="form.totalScore" :min="1" :max="500" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveExam">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { academicExamApi, academicScheduleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'

const exams = ref<any[]>([])
const offerings = ref<any[]>([])
const loading = ref(false)
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<any>({
  examName: '', offeringId: null, startTime: '', endTime: '',
  durationMinutes: 120, totalScore: 100,
})

function formatTime(t: string) { return t ? new Date(t).toLocaleString('zh-CN') : '-' }

async function fetchExams() {
  loading.value = true
  try {
    const res: any = await academicExamApi.list({ page: currentPage.value, size: pageSize.value, keyword: keyword.value })
    exams.value = res.records || []
    total.value = res.total || 0
  } catch { exams.value = [] } finally { loading.value = false }
}

async function loadOfferings() {
  try { offerings.value = await academicScheduleApi.offerings() } catch { offerings.value = [] }
}

function showDialog(row?: any) {
  editingId.value = row ? row.id : null
  if (row) {
    form.examName = row.examName; form.offeringId = row.offeringId
    form.startTime = row.startTime; form.endTime = row.endTime
    form.durationMinutes = row.durationMinutes; form.totalScore = row.totalScore
  } else {
    form.examName = ''; form.offeringId = null
    form.startTime = ''; form.endTime = ''
    form.durationMinutes = 120; form.totalScore = 100
  }
  dialogVisible.value = true
}

async function saveExam() {
  const data = {
    examName: form.examName,
    offeringId: form.offeringId,
    startTime: form.startTime ? new Date(form.startTime).toISOString() : null,
    endTime: form.endTime ? new Date(form.endTime).toISOString() : null,
    durationMinutes: form.durationMinutes,
    totalScore: form.totalScore,
  }
  try {
    if (editingId.value) await academicExamApi.update(editingId.value, data)
    else await academicExamApi.create(data)
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchExams()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}

async function deleteExam(row: any) {
  try { await ElMessageBox.confirm('确定删除该考试？', '确认', { type: 'warning' }) } catch { return }
  try { await academicExamApi.delete(row.id); ElMessage.success('删除成功'); fetchExams() }
  catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

onMounted(() => { loadOfferings(); fetchExams() })
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
</style>
