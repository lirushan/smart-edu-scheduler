import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export interface UserInfo {
  id: number
  username: string
  realName: string
  userType: number       // 1=学生 2=教师 3=教务 4=管理员 5=题库管理员
  department?: string
  major?: string
  avatar?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => {
    const map: Record<number, string> = { 1: 'student', 2: 'teacher', 3: 'academic', 4: 'admin', 5: 'qb_admin' }
    return userInfo.value ? map[userInfo.value.userType] || 'student' : 'student'
  })

  async function login(username: string, password: string) {
    const data = await authApi.login({ username, password })
    token.value = data.accessToken
    refreshToken.value = data.refreshToken
    userInfo.value = data.userInfo
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userRole', role.value)
    return data
  }

  async function fetchUserInfo() {
    try {
      const data = await authApi.me()
      userInfo.value = data
      localStorage.setItem('userRole', role.value)
    } catch {
      // ignore
    }
  }

  async function logout() {
    try {
      await authApi.logout()
    } catch { /* ignore */ }
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userRole')
  }

  return { token, refreshToken, userInfo, isLoggedIn, role, login, logout, fetchUserInfo }
})
