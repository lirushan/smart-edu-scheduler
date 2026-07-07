<template>
  <div class="page-card">
    <PageHeader title="培养方案" />

    <div class="toolbar">
      <el-input v-model="searchMajor" placeholder="搜索专业" clearable style="width:200px" @input="fetchPlans" />
      <el-button type="primary" @click="showDialog()">新增方案</el-button>
    </div>

    <el-table :data="plans" stripe v-loading="loading">
      <el-table-column prop="major" label="专业" min-width="160" />
      <el-table-column prop="grade" label="年级" width="100" />
      <el-table-column label="总学分" width="90">
        <template #default="{ row }">{{ row.totalCredits }} 学分</template>
      </el-table-column>
      <el-table-column label="必修学分" width="90">
        <template #default="{ row }">{{ row.requiredCredits }} 学分</template>
      </el-table-column>
      <el-table-column label="选修学分" width="90">
        <template #default="{ row }">{{ row.electiveCredits }} 学分</template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" link @click="showDialog(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="deletePlan(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-if="total > 0"
      style="margin-top:16px;justify-content:flex-end"
      layout="total, prev, pager, next"
      :total="total" :page-size="pageSize" v-model:current-page="currentPage"
      @current-change="fetchPlans"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑方案' : '新增方案'" width="500px" append-to-body>
      <el-form :model="form" label-width="100px">
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="form.grade" placeholder="如 2024级" /></el-form-item>
        <el-form-item label="总学分"><el-input-number v-model="form.totalCredits" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="必修学分"><el-input-number v-model="form.requiredCredits" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="选修学分"><el-input-number v-model="form.electiveCredits" :min="0" :precision="1" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { academicTrainingPlanApi } from '@/api'
import PageHeader from '@/components/PageHeader.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const plans = ref<any[]>([])
const loading = ref(false)
const searchMajor = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<any>({
  major: '', grade: '', totalCredits: 0, requiredCredits: 0,
  electiveCredits: 0, description: '', status: 1,
})

async function fetchPlans() {
  loading.value = true
  try {
    const res: any = await academicTrainingPlanApi.list({
      page: currentPage.value, size: pageSize.value, major: searchMajor.value || undefined,
    })
    plans.value = res.records || []
    total.value = res.total || 0
  } catch { plans.value = [] } finally { loading.value = false }
}

function showDialog(row?: any) {
  editingId.value = row ? row.id : null
  if (row) {
    form.major = row.major; form.grade = row.grade
    form.totalCredits = row.totalCredits; form.requiredCredits = row.requiredCredits
    form.electiveCredits = row.electiveCredits; form.description = row.description || ''
    form.status = row.status
  } else {
    form.major = ''; form.grade = ''; form.totalCredits = 0
    form.requiredCredits = 0; form.electiveCredits = 0
    form.description = ''; form.status = 1
  }
  dialogVisible.value = true
}

async function savePlan() {
  try {
    if (editingId.value) await academicTrainingPlanApi.update(editingId.value, form)
    else await academicTrainingPlanApi.create(form)
    ElMessage.success('保存成功'); dialogVisible.value = false; fetchPlans()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}

async function deletePlan(row: any) {
  try { await ElMessageBox.confirm('确定删除该培养方案？', '确认', { type: 'warning' }) } catch { return }
  try { await academicTrainingPlanApi.delete(row.id); ElMessage.success('删除成功'); fetchPlans() }
  catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

onMounted(fetchPlans)
</script>

<style lang="scss" scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
</style>
