<template>
  <div class="page-card score-entry">
    <h2 class="page-title">成绩录入</h2>

    <div class="toolbar">
      <span class="label">选择课程：</span>
      <el-select v-model="selectedOfferingId" placeholder="请选择授课课程" @change="fetchStudents">
        <el-option v-for="o in offerings" :key="o.id" :label="o.courseName" :value="o.id" />
      </el-select>
    </div>

    <div v-if="!selectedOfferingId" class="empty-hint">请先选择一门课程</div>
    <template v-else>
      <div class="toolbar">
        <el-button type="primary" @click="batchSave">批量保存</el-button>
        <el-upload :show-file-list="false" accept=".xlsx,.xls" class="upload-btn">
          <el-button>导入 Excel</el-button>
        </el-upload>
      </div>

      <el-table :data="scoreList" stripe>
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column label="百分制分数" width="180">
          <template #default="{ row }">
            <el-input-number v-model="row.rawScore" :min="0" :max="100" :precision="1" size="small" controls-position="right" />
          </template>
        </el-table-column>
        <el-table-column label="五级制" width="100">
          <template #default="{ row }">
            <el-tag :type="gradeTag(row.rawScore)" size="small">{{ convertGrade(row.rawScore) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="GPA" width="70">
          <template #default="{ row }">{{ calcGpa(row.rawScore) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="saveSingle(row)">保存</el-button>
            <el-button v-if="row.status !== 1" size="small" link type="success" @click="publish(row)">发布</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { scoreApi } from '@/api'
import { ElMessage } from 'element-plus'

const offerings = ref<any[]>([])
const selectedOfferingId = ref<number | null>(null)
const scoreList = ref<any[]>([])

function convertGrade(s: number | null | undefined) {
  if (s == null) return 'N/A'
  if (s >= 90) return '优秀'
  if (s >= 80) return '良好'
  if (s >= 70) return '中等'
  if (s >= 60) return '及格'
  return '不及格'
}
function calcGpa(s: number | null | undefined) {
  if (s == null) return 'N/A'
  if (s >= 90) return '4.0'
  if (s >= 80) return '3.0'
  if (s >= 70) return '2.0'
  if (s >= 60) return '1.0'
  return '0.0'
}
function gradeTag(s: number | null | undefined) {
  if (s == null) return 'info' as any
  if (s >= 90) return 'success'
  if (s >= 80) return ''
  if (s >= 70) return 'warning'
  if (s >= 60) return 'warning'
  return 'danger' as any
}

async function fetchStudents() {
  if (!selectedOfferingId.value) return
  try {
    scoreList.value = await scoreApi.byOffering(selectedOfferingId.value)
  } catch {
    scoreList.value = []
    ElMessage.info('暂无成绩数据，请先确认选课名单')
  }
}

async function saveSingle(row: any) {
  try {
    await scoreApi.update(row.id, {
      studentId: row.studentId,
      offeringId: row.offeringId || selectedOfferingId.value,
      rawScore: row.rawScore,
    })
    ElMessage.success('保存成功')
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}

async function batchSave() {
  try {
    await scoreApi.batch(scoreList.value.map(r => ({
      studentId: r.studentId,
      offeringId: selectedOfferingId.value,
      rawScore: r.rawScore,
    })))
    ElMessage.success('批量保存成功')
  } catch (e: any) { ElMessage.error(e?.message || '批量保存失败') }
}

async function publish(row: any) {
  try {
    await scoreApi.publish(row.id)
    row.status = 1
    ElMessage.success('成绩已发布')
  } catch (e: any) { ElMessage.error(e?.message || '发布失败') }
}
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0 0 12px; color: var(--color-text); }
.label { font-size: 13px; color: var(--color-text-secondary); }
.upload-btn { display: inline-block; margin-left: 8px; }
.empty-hint { text-align: center; color: var(--color-text-muted); padding: 60px 0; font-size: 14px; }
</style>
