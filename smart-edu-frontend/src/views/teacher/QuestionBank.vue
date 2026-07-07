<template>
  <div class="page-card question-bank">
    <h2 class="page-title">题库管理</h2>

    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索试题..." clearable class="search-input" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterType" placeholder="题型" clearable class="filter-select">
        <el-option label="单选题" :value="1" />
        <el-option label="多选题" :value="2" />
        <el-option label="判断题" :value="3" />
        <el-option label="填空题" :value="4" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="showCreateDialog">新增试题</el-button>
    </div>

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
      <el-table-column label="范围" width="80">
        <template #default="{ row }">
          <el-tag :type="row.scope === 1 ? 'success' : ''" size="small">{{ row.scope === 1 ? '全局' : '个人' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核" width="80">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 0 ? 'warning' : 'danger'" size="small">
            {{ row.auditStatus === 1 ? '通过' : row.auditStatus === 0 ? '待审' : '驳回' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140">
        <template #default="{ row }">
          <el-button size="small" link @click="editQuestion(row)">编辑</el-button>
          <el-button size="small" link type="danger" @click="deleteQuestion(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @change="fetchQuestions" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑试题' : '新增试题'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="题型">
          <el-radio-group v-model="form.questionType">
            <el-radio :value="1">单选</el-radio>
            <el-radio :value="2">多选</el-radio>
            <el-radio :value="3">判断</el-radio>
            <el-radio :value="4">填空</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目内容">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="[1,2].includes(form.questionType)" label="选项">
          <el-input v-model="form.optionsStr" type="textarea" :rows="3" placeholder="每行一个选项，如: A. 选项内容" />
        </el-form-item>
        <el-form-item label="正确答案">
          <el-input v-model="form.answer" placeholder="单选填字母如A，多选填如A,B，填空填完整答案" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoint" />
        </el-form-item>
        <el-form-item label="难度">
          <el-rate v-model="form.difficulty" :max="5" />
        </el-form-item>
        <el-form-item label="范围">
          <el-radio-group v-model="form.scope">
            <el-radio :value="2">个人</el-radio>
            <el-radio :value="1">全局（需审核）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { questionApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const keyword = ref('')
const filterType = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const questions = ref<any[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<any>({
  questionType: 1, content: '', optionsStr: '', answer: '',
  analysis: '', knowledgePoint: '', difficulty: 3, scope: 2,
})

function qTypeLabel(t: number) { return ['','单选题','多选题','判断题','填空题'][t] || '' }

async function fetchQuestions() {
  loading.value = true
  try {
    const data = await questionApi.list({
      page: page.value, size: size.value,
      keyword: keyword.value, questionType: filterType.value,
    })
    questions.value = data.records || []
    total.value = data.total || 0
  } catch { questions.value = [] } finally { loading.value = false }
}

function search() { page.value = 1; fetchQuestions() }

function showCreateDialog() {
  editingId.value = null
  Object.assign(form, { questionType: 1, content: '', optionsStr: '', answer: '', analysis: '', knowledgePoint: '', difficulty: 3, scope: 2 })
  dialogVisible.value = true
}

function editQuestion(row: any) {
  editingId.value = row.id
  form.questionType = row.questionType
  form.content = row.content
  form.optionsStr = row.options ? JSON.parse(row.options).join('\n') : ''
  form.answer = row.answer
  form.analysis = row.analysis || ''
  form.knowledgePoint = row.knowledgePoint || ''
  form.difficulty = row.difficulty || 3
  form.scope = row.scope || 2
  dialogVisible.value = true
}

async function saveQuestion() {
  const options = form.optionsStr ? JSON.stringify(form.optionsStr.split('\n').filter((l: string) => l.trim())) : '[]'
  const data = { ...form, options }
  delete data.optionsStr
  try {
    if (editingId.value) {
      await questionApi.update(editingId.value, data)
    } else {
      await questionApi.create(data)
    }
    ElMessage.success(editingId.value ? '编辑成功' : '新增成功')
    dialogVisible.value = false
    fetchQuestions()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}

async function deleteQuestion(row: any) {
  try { await ElMessageBox.confirm('确定删除此试题？', '确认', { type: 'warning' }) } catch { return }
  try { await questionApi.delete(row.id); ElMessage.success('删除成功'); fetchQuestions() }
  catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

onMounted(fetchQuestions)
</script>

<style lang="scss" scoped>
.page-title { font-size: 20px; font-weight: 700; margin: 0 0 8px; color: var(--color-text); }
.search-input { width: 240px; }
.filter-select { width: 140px; }
</style>
