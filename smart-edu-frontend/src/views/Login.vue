<template>
  <div class="login-page">
    <!-- 光球背景 -->
    <div class="light-orb light-orb-1"></div>
    <div class="light-orb light-orb-2"></div>
    <div class="light-orb light-orb-3"></div>

    <div class="login-card page-card gradient-border-top">
      <div class="login-logo">
        <div class="logo-icon icon-3d-clay icon-3d-purple icon-3d-lg">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="#a78bfa"/>
            <path d="M2 17l10 5 10-5" stroke="#7c3aed" stroke-width="2" fill="none"/>
            <path d="M2 12l10 5 10-5" stroke="#8b5cf6" stroke-width="2" fill="none"/>
          </svg>
        </div>
        <h1>智 教 通</h1>
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
          <el-radio-group v-model="form.role" class="role-group">
            <el-radio-button value="student">🎓 学生</el-radio-button>
            <el-radio-button value="teacher">👨‍🏫 教师</el-radio-button>
            <el-radio-button value="academic">📋 教务</el-radio-button>
            <el-radio-button value="admin">⚙️ 管理员</el-radio-button>
            <el-radio-button value="qb_admin">📝 题库管理</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p class="demo-hint">种子账号: admin / teacher01 / student01 / academic01 / qbadmin01</p>
        <p class="demo-hint">密码: password123</p>
      </div>
    </div>

    <!-- 脉冲光圈装饰 -->
    <div class="pulse-rings">
      <div class="pulse-ring"></div>
      <div class="pulse-ring"></div>
      <div class="pulse-ring"></div>
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

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    try {
      // 尝试调用真实 API
      await userStore.login(roleUserMap[form.role] || form.username, form.password)
    } catch {
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
    router.push('/dashboard')
  } catch (e: any) {
    ElMessage.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 30%, #16213e 60%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.login-card {
  width: 440px;
  position: relative;
  z-index: 10;
  padding: 44px 40px;
}

.login-logo {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    margin: 0 auto 12px;
  }

  h1 {
    font-size: 24px;
    font-weight: 700;
    background: linear-gradient(135deg, #a78bfa, #667eea);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 4px;
    margin: 0 0 4px;
  }
  p {
    color: var(--color-text-muted);
    font-size: 13px;
    margin: 0;
  }
}

.role-group {
  width: 100%;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  :deep(.el-radio-button__inner) {
    min-width: 78px;
    text-align: center;
    font-size: 12px;
    border-radius: 8px !important;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
}

.login-footer {
  text-align: center;
  margin-top: 16px;
}

.demo-hint {
  font-size: 11px;
  color: var(--color-text-muted);
  margin: 2px 0;
}
</style>
