<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-logo">
        <div class="logo-icon icon-3d-clay icon-3d-purple icon-3d-lg">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#a78bfa"/>
            <path d="M2 17l10 5 10-5" stroke="#7c3aed" stroke-width="2" fill="none"/>
            <path d="M2 12l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <h1>智教通</h1>
        <p>学生课程报名与排课管理系统</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" size="large" @keyup.enter="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-radio-group v-model="form.role" class="role-group" @change="applyRoleAccount">
            <el-radio-button value="student">学生</el-radio-button>
            <el-radio-button value="teacher">教师</el-radio-button>
            <el-radio-button value="academic">教务</el-radio-button>
            <el-radio-button value="admin">管理员</el-radio-button>
            <el-radio-button value="qb_admin">题库管理</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p class="demo-hint">选择角色会自动填入对应种子账号，也可以手动输入。</p>
        <p class="demo-hint">账号: admin / teacher01 / student01 / academic01 / qbadmin01</p>
        <p class="demo-hint">密码: password123</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { FormInstance } from 'element-plus'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'student01',
  password: 'password123',
  role: 'student',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const roleUserMap: Record<string, string> = {
  student: 'student01',
  teacher: 'teacher01',
  academic: 'academic01',
  admin: 'admin',
  qb_admin: 'qbadmin01',
}

const roleHomeMap: Record<string, string> = {
  student: '/dashboard',
  teacher: '/teacher',
  academic: '/academic',
  admin: '/admin',
  qb_admin: '/qb-admin',
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    try {
      // 尝试调用真实 API
      await userStore.login(form.username, form.password)
    } catch {
      if (import.meta.env.VITE_ENABLE_MOCK_LOGIN !== 'true') {
        throw new Error('登录失败，请检查账号、密码或后端服务状态')
      }
      // 降级到模拟登录
      const userMap: Record<string, { id: number; realName: string; userType: number; department: string; major: string }> = {
        student: { id: 6, realName: '张明远', userType: 1, department: '计算机学院', major: '计算机科学与技术' },
        teacher: { id: 3, realName: '赵教授', userType: 2, department: '计算机学院', major: '' },
        academic: { id: 2, realName: '李教务', userType: 3, department: '教务处', major: '' },
        admin: { id: 1, realName: '系统管理员', userType: 4, department: '信息化中心', major: '' },
        qb_admin: { id: 10, realName: '周题库', userType: 5, department: '信息化中心', major: '' },
      }
      const u = userMap[form.role] || userMap.student
      userStore.token = 'mock-token'
      userStore.userInfo = {
        id: u.id, username: roleUserMap[form.role] || form.username,
        realName: u.realName, userType: u.userType,
        department: u.department, major: u.major,
      }
      localStorage.setItem('token', 'mock-token')
      localStorage.setItem('userRole', form.role)
    }

    ElMessage.success('登录成功')
    router.push(roleHomeMap[userStore.role] || '/dashboard')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

function applyRoleAccount(role: string | number | boolean | undefined) {
  const nextUsername = roleUserMap[String(role)]
  if (nextUsername) {
    form.username = nextUsername
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background:
    linear-gradient(135deg, rgba(248, 250, 252, 0.94) 0%, rgba(241, 245, 249, 0.96) 48%, rgba(236, 254, 255, 0.92) 100%),
    repeating-linear-gradient(135deg, rgba(79, 70, 229, 0.035) 0 1px, transparent 1px 16px);
  position: relative;
  overflow: hidden;
}

.login-card {
  width: 460px;
  position: relative;
  z-index: 10;
  padding: 42px 42px 34px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(79, 70, 229, 0.12);
  border-radius: 14px;
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.14), 0 2px 10px rgba(79, 70, 229, 0.08);
  backdrop-filter: blur(18px);
  -webkit-backdrop-filter: blur(18px);
}

.login-logo {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    margin: 0 auto 12px;
  }

  h1 {
    font-size: 30px;
    font-weight: 700;
    background: linear-gradient(135deg, #1f2937, #4f46e5);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 0;
    margin: 0 0 4px;
  }
  p {
    color: #64748b;
    font-size: 14px;
    margin: 0;
  }
}

.role-group {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  :deep(.el-radio-button__inner) {
    min-width: 76px;
    text-align: center;
    font-size: 12px;
    border-radius: 8px !important;
    border: 1px solid rgba(148, 163, 184, 0.28) !important;
    background: rgba(255, 255, 255, 0.74) !important;
    color: #475569;
    box-shadow: none !important;
  }

  :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    color: #fff;
    background: linear-gradient(135deg, #4f46e5, #06b6d4) !important;
    border-color: transparent !important;
  }
}

.login-btn {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.demo-hint {
  font-size: 12px;
  color: #64748b;
  margin: 2px 0;
}

@media (max-width: 520px) {
  .login-page {
    padding: 24px 16px;
    overflow: auto;
  }

  .login-card {
    width: 100%;
    padding: 32px 22px 26px;
  }

  .login-logo {
    margin-bottom: 24px;

    h1 {
      font-size: 26px;
    }
  }

  .role-group {
    :deep(.el-radio-button__inner) {
      min-width: 68px;
      padding: 10px 12px;
    }
  }
}
</style>

