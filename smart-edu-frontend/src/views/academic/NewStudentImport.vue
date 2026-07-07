<template>
  <div class="page-card">
    <PageHeader title="新生导入" />

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
      <div v-if="importResult.errors && importResult.errors.length > 0" style="margin-top:12px">
        <div v-for="(err, i) in importResult.errors" :key="i" class="error-item">{{ err }}</div>
      </div>
    </GlassCard>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { academicStudentApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Checked, CircleClose, List } from '@element-plus/icons-vue'
import GlassCard from '@/components/GlassCard.vue'
import StatCard from '@/components/StatCard.vue'

const fileReady = ref(false)
const previewing = ref(false)
const importing = ref(false)
const selectedFile = ref<File | null>(null)
const previewData = ref<any[]>([])
const importResult = ref<any>(null)

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
  } catch (e: any) { ElMessage.error(e?.message || '导入失败') }
  finally { importing.value = false }
}
</script>

<style lang="scss" scoped>
.upload-area { display: flex; flex-direction: column; align-items: center; }
.upload-text { font-size: 14px; color: var(--color-text-secondary); margin-top: 8px; }
.upload-hint { font-size: 12px; color: var(--color-text-muted); margin-top: 4px; }
.result-summary { display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; }
.error-item { font-size: 12px; color: var(--color-danger, #f87171); padding: 4px 0; border-bottom: 1px solid rgba(248,113,113,0.1); }
</style>
