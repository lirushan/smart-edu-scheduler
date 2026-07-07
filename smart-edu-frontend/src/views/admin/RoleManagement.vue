<template>
  <div class="role-management">
    <section class="page-card role-panel">
      <div class="page-heading">
        <div>
          <h2 class="page-title">角色管理</h2>
          <p class="page-subtitle">维护系统角色状态，并查看角色可访问的菜单范围。</p>
        </div>
        <el-tag type="info" effect="plain">{{ total }} 个角色</el-tag>
      </div>

      <div class="toolbar role-toolbar">
        <el-input v-model="keyword" placeholder="搜索角色编码、名称或说明" clearable class="search-input" @keyup.enter="search">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="statusFilter" placeholder="状态" clearable class="status-select">
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="search">搜索</el-button>
        <div class="toolbar-spacer" />
        <el-button type="success" :icon="Plus" @click="openCreateDialog">新增角色</el-button>
      </div>

      <el-table :data="roles" stripe v-loading="loading" highlight-current-row @current-change="selectRole">
        <el-table-column prop="roleName" label="角色" min-width="120">
          <template #default="{ row }">
            <div class="role-name">
              <span>{{ row.roleName }}</span>
              <small>{{ row.roleCode }}</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :loading="statusLoadingId === row.id"
              @change="toggleStatus(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openEditDialog(row)">编辑</el-button>
            <el-button link type="primary" @click.stop="selectRole(row)">权限</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          layout="total, prev, pager, next"
          @change="fetchRoles"
        />
      </div>
    </section>

    <section class="page-card permission-panel">
      <div class="page-heading compact">
        <div>
          <h3 class="panel-title">{{ selectedRole?.roleName || '选择角色' }}</h3>
          <p class="page-subtitle">{{ selectedRole ? '勾选菜单后保存即可更新该角色权限。' : '从左侧选择一个角色查看菜单权限。' }}</p>
        </div>
        <el-tag v-if="selectedRole" type="success" effect="plain">{{ selectedRole.roleCode }}</el-tag>
      </div>

      <el-skeleton v-if="detailLoading" :rows="8" animated />
      <el-empty v-else-if="!selectedRole" description="暂无选中角色" />
      <div v-else class="permission-content">
        <el-tree
          ref="treeRef"
          :data="menuTree"
          node-key="id"
          show-checkbox
          default-expand-all
          :props="treeProps"
          :check-strictly="false"
          class="menu-tree"
        >
          <template #default="{ data }">
            <div class="tree-node">
              <el-icon><component :is="iconMap[data.icon] || MenuIcon" /></el-icon>
              <span>{{ data.menuName }}</span>
              <small v-if="data.path">{{ data.path }}</small>
            </div>
          </template>
        </el-tree>

        <div class="permission-actions">
          <el-button :icon="Refresh" @click="loadRoleDetail(selectedRole.id)">重置</el-button>
          <el-button type="primary" :icon="Check" :loading="saving" @click="saveMenus">保存权限</el-button>
        </div>
      </div>
    </section>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增角色' : '编辑角色'"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            placeholder="请输入角色编码"
            maxlength="50"
            :disabled="dialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述（可选）"
            maxlength="200"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import {
  Avatar,
  Calendar,
  Check,
  Checked,
  Clock,
  Collection,
  DataLine,
  Document,
  Files,
  List,
  Menu as MenuIcon,
  Monitor,
  Notebook,
  Plus,
  Reading,
  Refresh,
  Search,
  Setting,
  Timer,
  Upload,
  UserFilled,
  View,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { roleApi } from '@/api'

interface RoleItem {
  id: number
  roleCode: string
  roleName: string
  description: string
  status: number
}

interface MenuNode {
  id: number
  menuName: string
  path?: string
  icon?: string
  children?: MenuNode[]
}

const iconMap: Record<string, any> = {
  Avatar,
  Calendar,
  Checked,
  Clock,
  Collection,
  DataLine,
  Document,
  Files,
  List,
  Monitor,
  Notebook,
  Reading,
  Setting,
  Timer,
  Upload,
  UserFilled,
  View,
}

const treeProps = { label: 'menuName', children: 'children' }
const keyword = ref('')
const statusFilter = ref<number | null>(null)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const loading = ref(false)
const detailLoading = ref(false)
const saving = ref(false)
const statusLoadingId = ref<number | null>(null)
const roles = ref<RoleItem[]>([])
const selectedRole = ref<RoleItem | null>(null)
const menuTree = ref<MenuNode[]>([])
const treeRef = ref()

// ---- 新增/编辑角色弹窗 ----
const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingId = ref<number | null>(null)
const submitting = ref(false)
const formRef = ref()

const form = reactive({
  roleName: '',
  roleCode: '',
  description: '',
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

function resetForm() {
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  editingId.value = null
  formRef.value?.resetFields()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row: RoleItem | Record<string, any>) {
  const role = row as RoleItem
  dialogMode.value = 'edit'
  editingId.value = role.id
  form.roleName = role.roleName
  form.roleCode = role.roleCode
  form.description = role.description || ''
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (dialogMode.value === 'create') {
      await roleApi.create({
        roleName: form.roleName,
        roleCode: form.roleCode,
        description: form.description,
      })
      ElMessage.success('角色创建成功')
    } else {
      await roleApi.update(editingId.value!, {
        roleName: form.roleName,
        description: form.description,
      })
      ElMessage.success('角色信息已更新')
    }
    dialogVisible.value = false
    await fetchRoles()
  } catch {
    // 错误已由 http 拦截器统一提示
  } finally {
    submitting.value = false
  }
}

async function fetchRoles() {
  loading.value = true
  try {
    const data = await roleApi.list({
      page: page.value,
      size: size.value,
      keyword: keyword.value,
      status: statusFilter.value,
    })
    roles.value = data.records || []
    total.value = data.total || 0
    if (!selectedRole.value && roles.value.length > 0) {
      await selectRole(roles.value[0])
    }
  } catch {
    roles.value = []
  } finally {
    loading.value = false
  }
}

async function fetchMenuTree() {
  menuTree.value = await roleApi.menuTree()
}

function search() {
  page.value = 1
  fetchRoles()
}

async function selectRole(row?: RoleItem | Record<string, any> | null) {
  if (!row) return
  selectedRole.value = row as RoleItem
  await loadRoleDetail(Number(row.id))
}

async function loadRoleDetail(id: number) {
  detailLoading.value = true
  try {
    const data = await roleApi.detail(id)
    await nextTick()
    treeRef.value?.setCheckedKeys(data.menuIds || [], false)
  } finally {
    detailLoading.value = false
  }
}

async function toggleStatus(row: RoleItem | Record<string, any>) {
  const role = row as RoleItem
  const previous = role.status === 1 ? 0 : 1
  statusLoadingId.value = role.id
  try {
    await roleApi.toggleStatus(role.id, role.status)
    ElMessage.success('角色状态已更新')
  } catch {
    role.status = previous
  } finally {
    statusLoadingId.value = null
  }
}

async function saveMenus() {
  if (!selectedRole.value) return
  saving.value = true
  try {
    const checked = treeRef.value?.getCheckedKeys(false) || []
    const halfChecked = treeRef.value?.getHalfCheckedKeys() || []
    const menuIds = [...new Set([...checked, ...halfChecked])].map(Number)
    await roleApi.updateMenus(selectedRole.value.id, menuIds)
    ElMessage.success('角色权限已保存')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await fetchMenuTree()
  await fetchRoles()
})
</script>

<style lang="scss" scoped>
.role-management {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(360px, 0.75fr);
  gap: 18px;
}

.page-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;

  &.compact {
    margin-bottom: 18px;
  }
}

.page-title,
.panel-title {
  margin: 0;
  color: var(--color-text);
  font-weight: 800;
  letter-spacing: 0;
}

.page-title {
  font-size: 22px;
}

.panel-title {
  font-size: 18px;
}

.page-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 13px;
  line-height: 1.6;
}

.role-toolbar {
  flex-wrap: wrap;
}

.search-input {
  width: min(360px, 100%);
}

.status-select {
  width: 116px;
}

.toolbar-spacer {
  flex: 1;
}

.role-name {
  display: flex;
  flex-direction: column;
  gap: 3px;

  span {
    color: var(--color-text);
    font-weight: 700;
  }

  small {
    color: var(--color-text-muted);
    font-size: 12px;
  }
}

.permission-panel {
  min-height: 520px;
}

.permission-content {
  display: flex;
  flex-direction: column;
  min-height: 420px;
}

.menu-tree {
  flex: 1;
  padding: 10px;
  background: rgba(255, 255, 255, 0.34);
  border: 1px solid var(--color-border);
  border-radius: 8px;

  :deep(.el-tree-node__content) {
    min-height: 36px;
    border-radius: 8px;
  }

  :deep(.el-tree-node__content:hover) {
    background: rgba(139, 92, 246, 0.08);
  }
}

html.dark .menu-tree {
  background: rgba(255, 255, 255, 0.03);
}

.tree-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;

  span {
    color: var(--color-text);
    font-weight: 650;
  }

  small {
    color: var(--color-text-muted);
    font-size: 12px;
  }
}

.permission-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 16px;
}

@media (max-width: 1120px) {
  .role-management {
    grid-template-columns: 1fr;
  }
}
</style>
