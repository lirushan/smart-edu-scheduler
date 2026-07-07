<template>
  <div class="page-card round-config">
    <PageHeader title="选课轮次配置" />
    <el-button type="primary" @click="showCreateDialog" style="margin-bottom:16px">新建轮次</el-button>

    <el-table :data="rounds" stripe v-loading="loading">
      <el-table-column prop="roundName" label="轮次名称" min-width="180" />
      <el-table-column prop="semester" label="学期" width="130" />
      <el-table-column label="时间范围" width="300">
        <template #default="{ row }">{{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}</template>
      </el-table-column>
      <el-table-column label="限制" width="140">
        <template #default="{ row }">学分{{ row.maxCredits }}·{{ row.maxCourses }}门</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'info' : 'warning'" size="small">
            {{ row.status === 1 ? '进行中' : row.status === 2 ? '已结束' : '未开始' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button size="small" link @click="editRound(row)">编辑</el-button>
          <el-button size="small" link @click="toggleStatus(row)">
            {{ row.status === 1 ? '结束' : '启动' }}
          </el-button>
          <el-button size="small" link type="danger" @click="deleteRound(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑轮次' : '新建轮次'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="轮次名称"><el-input v-model="form.roundName" /></el-form-item>
        <el-form-item label="学期"><el-input v-model="form.semester" placeholder="如 2024-2025-1" /></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" /></el-form-item>
        <el-form-item label="最大学分"><el-input-number v-model="form.maxCredits" :min="1" :max="50" /></el-form-item>
        <el-form-item label="最大门数"><el-input-number v-model="form.maxCourses" :min="1" :max="20" /></el-form-item>
        <el-form-item label="目标年级"><el-input v-model="form.targetGradesStr" placeholder="逗号分隔，如: 2024级,2023级" /></el-form-item>
        <el-form-item label="年龄下限"><el-input-number v-model="form.ageMin" :min="0" :max="99" /></el-form-item>
        <el-form-item label="年龄上限"><el-input-number v-model="form.ageMax" :min="0" :max="99" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRound">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { roundApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const rounds = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<any>({
  roundName: '', semester: '2024-2025-1', startTime: '', endTime: '',
  maxCredits: 30, maxCourses: 8, targetGradesStr: '', ageMin: 16, ageMax: 30,
})

function formatTime(t: string) { return t ? new Date(t).toLocaleString('zh-CN') : '-' }

async function fetchRounds() {
  loading.value = true
  try { rounds.value = await roundApi.list() } catch { rounds.value = [] } finally { loading.value = false }
}

function showCreateDialog() {
  editingId.value = null
  Object.assign(form, { roundName: '', semester: '2024-2025-1', startTime: '', endTime: '', maxCredits: 30, maxCourses: 8, targetGradesStr: '', ageMin: 16, ageMax: 30 })
  dialogVisible.value = true
}

function editRound(row: any) {
  editingId.value = row.id
  form.roundName = row.roundName; form.semester = row.semester
  form.startTime = row.startTime; form.endTime = row.endTime
  form.maxCredits = row.maxCredits; form.maxCourses = row.maxCourses
  form.targetGradesStr = row.targetGrades ? JSON.parse(row.targetGrades).join(',') : ''
  form.ageMin = row.ageMin || 16; form.ageMax = row.ageMax || 30
  dialogVisible.value = true
}

async function saveRound() {
  const data = {
    ...form,
    targetGrades: JSON.stringify(form.targetGradesStr ? form.targetGradesStr.split(',').map((s: string) => s.trim()) : []),
  }
  delete data.targetGradesStr
  try {
    if (editingId.value) await roundApi.update(editingId.value, data)
    else await roundApi.create(data)
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchRounds()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 1 ? 2 : 1
  try { await roundApi.toggleStatus(row.id, newStatus); row.status = newStatus; ElMessage.success('操作成功') }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

async function deleteRound(row: any) {
  try { await ElMessageBox.confirm('确定删除此轮次？', '确认', { type: 'warning' }) } catch { return }
  try { await roundApi.delete(row.id); ElMessage.success('删除成功'); fetchRounds() }
  catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

onMounted(fetchRounds)
</script>

<style lang="scss" scoped>
</style>
