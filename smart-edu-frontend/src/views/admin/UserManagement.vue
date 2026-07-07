<template>
  <div class="page-card user-management">
    <PageHeader title="用户管理" />
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名/姓名/院系..." clearable class="search-input" @keyup.enter="search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-select v-model="filterType" placeholder="角色筛选" clearable>
        <el-option label="学生" :value="1" /><el-option label="教师" :value="2" />
        <el-option label="教务" :value="3" /><el-option label="管理员" :value="4" />
        <el-option label="题库管理员" :value="5" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
      <el-button @click="showCreateDialog">新增用户</el-button>
    </div>

    <el-table :data="users" stripe v-loading="loading">
      <el-table-column type="index" label="#" width="50" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">{{ roleLabel(row.userType) }}</template>
      </el-table-column>
      <el-table-column prop="department" label="院系" width="140" />
      <el-table-column prop="major" label="专业" width="140" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 2 ? 'warning' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : row.status === 2 ? '锁定' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" link @click="editUser(row)">编辑</el-button>
          <el-button size="small" link @click="toggleStatus(row)">
            {{ row.status === 0 ? '启用' : '禁用' }}
          </el-button>
          <el-button size="small" link type="danger" @click="deleteUser(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-bar">
      <el-pagination v-model:current-page="page" v-model:page-size="size" :total="total"
        layout="total, prev, pager, next" @change="fetchUsers" />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" placeholder="留空则不修改" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.userType">
            <el-option label="学生" :value="1" /><el-option label="教师" :value="2" />
            <el-option label="教务" :value="3" /><el-option label="管理员" :value="4" />
            <el-option label="题库管理员" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="院系"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="专业"><el-input v-model="form.major" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import { userApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const keyword = ref('')
const filterType = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const users = ref<any[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({ username: '', password: '', realName: '', userType: 1, department: '', major: '' })

function roleLabel(t: number) {
  const map: Record<number, string> = { 1: '学生', 2: '教师', 3: '教务', 4: '管理员', 5: '题库管理员' }
  return map[t] || ''
}

async function fetchUsers() {
  loading.value = true
  try {
    const data = await userApi.list({ page: page.value, size: size.value, keyword: keyword.value, userType: filterType.value })
    users.value = data.records || []
    total.value = data.total || 0
  } catch { users.value = [] } finally { loading.value = false }
}

function search() { page.value = 1; fetchUsers() }

function showCreateDialog() {
  editingId.value = null
  Object.assign(form, { username: '', password: '', realName: '', userType: 1, department: '', major: '' })
  dialogVisible.value = true
}

function editUser(row: any) {
  editingId.value = row.id
  form.username = row.username; form.password = ''
  form.realName = row.realName; form.userType = row.userType
  form.department = row.department || ''; form.major = row.major || ''
  dialogVisible.value = true
}

async function saveUser() {
  try {
    if (editingId.value) await userApi.update(editingId.value, form)
    else await userApi.create(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false; fetchUsers()
  } catch (e: any) { ElMessage.error(e?.message || '保存失败') }
}

async function toggleStatus(row: any) {
  const newStatus = row.status === 0 ? 1 : 0
  try { await userApi.toggleStatus(row.id, newStatus); row.status = newStatus; ElMessage.success('操作成功') }
  catch (e: any) { ElMessage.error(e?.message || '操作失败') }
}

async function deleteUser(row: any) {
  try { await ElMessageBox.confirm('确定删除此用户？', '确认', { type: 'warning' }) } catch { return }
  try { await userApi.delete(row.id); ElMessage.success('删除成功'); fetchUsers() }
  catch (e: any) { ElMessage.error(e?.message || '删除失败') }
}

onMounted(fetchUsers)
</script>

<style lang="scss" scoped>
.search-input { width: 240px; }
</style>
