<template>
  <div class="page-card new-student-page">
    <PageHeader title="新生导入" />

    <div class="flow-strip">
      <div class="flow-step active">
        <span>1</span>
        <div>
          <strong>上传文件</strong>
          <p>选择 CSV / Excel 新生名单</p>
        </div>
      </div>
      <div class="flow-line" />
      <div class="flow-step" :class="{ active: previewData.length > 0 || importing || importResult }">
        <span>2</span>
        <div>
          <strong>预览校验</strong>
          <p>核对学号、姓名、专业和年级</p>
        </div>
      </div>
      <div class="flow-line" />
      <div class="flow-step" :class="{ active: importResult }">
        <span>3</span>
        <div>
          <strong>入库核对</strong>
          <p>在学生档案列表确认结果</p>
        </div>
      </div>
    </div>

    <!-- 上传区 -->
    <GlassCard title="上传文件" :gradient-top="true">
      <div class="upload-area">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".csv,.xls,.xlsx"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          drag
        >
          <el-icon :size="36" style="color:var(--color-text-muted)"><UploadFilled /></el-icon>
          <div class="upload-text">将 CSV / Excel 文件拖到此处，或点击上传</div>
          <div class="upload-hint">表头需含：姓名、学号、专业、年级</div>
        </el-upload>
        <el-button type="primary" :disabled="!fileReady" :loading="previewing" @click="previewFile" style="margin-top:12px">
          预览数据
        </el-button>
      </div>
    </GlassCard>

    <!-- 预览表格 -->
    <GlassCard v-if="previewData.length > 0" title="导入预览">
      <el-table :data="previewData" stripe max-height="360">
        <el-table-column prop="row" label="行号" width="70" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="140" />
        <el-table-column prop="major" label="专业" width="160" />
        <el-table-column prop="grade" label="年级" width="100" />
      </el-table>
      <div style="margin-top:16px;display:flex;justify-content:flex-end;gap:12px">
        <el-button @click="previewData = []">取消</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">
          确认导入 ({{ previewData.length }} 条)
        </el-button>
      </div>
    </GlassCard>

    <!-- 导入结果 -->
    <GlassCard v-if="importResult" title="导入结果">
      <div class="result-summary">
        <StatCard icon="Checked" label="成功" :value="importResult.success" color="green" />
        <StatCard icon="CircleClose" label="失败" :value="importResult.fail" color="red" />
        <StatCard icon="List" label="总计" :value="importResult.total" color="blue" />
      </div>
      <div class="result-actions">
        <span>导入成功的学生已写入下方“学生档案列表”，默认账号为学号，初始密码为 password123。</span>
        <el-button type="primary" @click="refreshStudents">刷新列表</el-button>
      </div>
      <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top:12px">
        <div v-for="(err, i) in importResult.errors" :key="i" class="error-item">{{ err }}</div>
      </div>
    </GlassCard>

    <GlassCard title="学生档案列表">
      <div class="student-toolbar">
        <el-input
          v-model="studentKeyword"
          placeholder="搜索学号 / 姓名 / 院系 / 专业"
          clearable
          class="search-input"
          @keyup.enter="searchStudents"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="studentMajor" placeholder="专业筛选" clearable class="filter-input" @keyup.enter="searchStudents" />
        <el-input v-model="studentGrade" placeholder="年级筛选" clearable class="filter-input" @keyup.enter="searchStudents" />
        <el-button type="primary" @click="searchStudents">查询</el-button>
        <el-button @click="resetStudents">重置</el-button>
      </div>

      <el-table :data="students" stripe v-loading="studentsLoading">
        <el-table-column type="index" label="#" width="56" />
        <el-table-column prop="username" label="学号/账号" min-width="130" />
        <el-table-column prop="realName" label="姓名" min-width="110" />
        <el-table-column prop="department" label="院系" min-width="150" show-overflow-tooltip />
        <el-table-column prop="major" label="专业" min-width="150" show-overflow-tooltip />
        <el-table-column prop="grade" label="年级" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : row.status === 2 ? '锁定' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="入库时间" min-width="170" />
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="studentPage"
          v-model:page-size="studentSize"
          :total="studentTotal"
          layout="total, prev, pager, next"
          @change="fetchStudents"
        />
      </div>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { academicStudentApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Checked, CircleClose, List, Search } from '@element-plus/icons-vue'
import GlassCard from '@/components/GlassCard.vue'
import StatCard from '@/components/StatCard.vue'

const fileReady = ref(false)
const previewing = ref(false)
const importing = ref(false)
const selectedFile = ref<File | null>(null)
const previewData = ref<any[]>([])
const importResult = ref<any>(null)
const studentsLoading = ref(false)
const students = ref<any[]>([])
const studentPage = ref(1)
const studentSize = ref(10)
const studentTotal = ref(0)
const studentKeyword = ref('')
const studentMajor = ref('')
const studentGrade = ref('')

function handleFileChange(file: any) {
  selectedFile.value = file.raw
  fileReady.value = true
}

function handleFileRemove() {
  selectedFile.value = null
  fileReady.value = false
  previewData.value = []
  importResult.value = null
}

async function previewFile() {
  if (!selectedFile.value) return
  previewing.value = true
  importResult.value = null
  try { previewData.value = await academicStudentApi.preview(selectedFile.value) }
  catch (e: any) { ElMessage.error(e?.message || '预览失败'); previewData.value = [] }
  finally { previewing.value = false }
}

async function confirmImport() {
  importing.value = true
  try {
    importResult.value = await academicStudentApi.import(previewData.value)
    ElMessage.success(`导入完成：成功 ${importResult.value.success} 条，失败 ${importResult.value.fail} 条`)
    previewData.value = []
    selectedFile.value = null
    fileReady.value = false
    studentPage.value = 1
    await fetchStudents()
  } catch (e: any) { ElMessage.error(e?.message || '导入失败') }
  finally { importing.value = false }
}

async function fetchStudents() {
  studentsLoading.value = true
  try {
    const data = await academicStudentApi.list({
      page: studentPage.value,
      size: studentSize.value,
      keyword: studentKeyword.value,
      major: studentMajor.value,
      grade: studentGrade.value,
    })
    students.value = data.records || []
    studentTotal.value = data.total || 0
  } catch (e: any) {
    students.value = []
    studentTotal.value = 0
    ElMessage.error(e?.message || '学生列表加载失败')
  } finally {
    studentsLoading.value = false
  }
}

function searchStudents() {
  studentPage.value = 1
  fetchStudents()
}

function resetStudents() {
  studentKeyword.value = ''
  studentMajor.value = ''
  studentGrade.value = ''
  searchStudents()
}

function refreshStudents() {
  fetchStudents()
}

onMounted(fetchStudents)
</script>

<style lang="scss" scoped>
.new-student-page {
  display: grid;
  gap: 18px;
}

.flow-strip {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 64px minmax(0, 1fr) 64px minmax(0, 1fr);
  align-items: center;
  padding: 18px 20px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(139, 92, 246, 0.12), rgba(6, 182, 212, 0.08)), var(--color-surface);
  box-shadow: var(--shadow-soft);
}

.flow-step {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--color-text-muted);

  span {
    display: grid;
    place-items: center;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: 1px solid var(--color-border);
    background: var(--color-surface-hover);
    font-weight: 700;
  }

  strong {
    display: block;
    color: var(--color-text-secondary);
    font-size: 14px;
  }

  p {
    margin: 2px 0 0;
    font-size: 12px;
  }

  &.active {
    color: var(--color-text-secondary);

    span {
      color: #fff;
      border-color: transparent;
      background: linear-gradient(135deg, var(--color-brand), #06b6d4);
      box-shadow: 0 10px 26px rgba(139, 92, 246, 0.3);
    }

    strong {
      color: var(--color-text);
    }
  }
}

.flow-line {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.45), transparent);
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.upload-text {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-top: 8px;
}

.upload-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.result-summary {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.result-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-top: 14px;
  padding: 12px 14px;
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 12px;
  color: var(--color-text-secondary);
  background: rgba(16, 185, 129, 0.08);
}

.student-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 280px;
}

.filter-input {
  width: 160px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.error-item {
  font-size: 12px;
  color: var(--color-danger, #f87171);
  padding: 4px 0;
  border-bottom: 1px solid rgba(248,113,113,0.1);
}

@media (max-width: 900px) {
  .flow-strip {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .flow-line {
    display: none;
  }

  .search-input,
  .filter-input {
    width: 100%;
  }

  .result-summary {
    grid-template-columns: 1fr;
  }

  .result-actions {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
