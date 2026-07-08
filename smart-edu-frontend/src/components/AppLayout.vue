<template>
  <div class="app-layout" :class="{ 'dark-mode': isDark }">
    <!-- 玻璃态侧边栏 -->
    <aside class="sidebar-glass" :class="{ 'sidebar-open': sidebarOpen }">
      <div class="logo-area">
        <div class="logo-icon">
          <svg width="28" height="28" viewBox="0 0 24 24" fill="none">
            <path d="M12 2L2 7l10 5 10-5-10-5z" fill="url(#lg)" />
            <path d="M2 17l10 5 10-5" stroke="url(#lg)" stroke-width="2" fill="none" />
            <path d="M2 12l10 5 10-5" stroke="url(#lg)" stroke-width="2" fill="none" />
            <defs><linearGradient id="lg" x1="2" y1="2" x2="22" y2="22"><stop stop-color="#8b5cf6"/><stop offset="1" stop-color="#667eea"/></linearGradient></defs>
          </svg>
        </div>
        <span class="logo-text">智教通</span>
        <el-button class="sidebar-close-btn" :icon="Close" circle size="small" text @click="sidebarOpen = false" />
      </div>

      <nav class="sidebar-nav">
        <template v-for="item in menuItems" :key="item.section || item.path || item.label">
          <div v-if="item.section" class="nav-section">{{ item.section }}</div>
          <router-link
            v-else
            :to="item.path || '/'"
            class="nav-item"
            :class="{ active: isActive(item.path || '') }"
            @click="sidebarOpen = false"
          >
            <el-icon v-if="item.iconComp" :size="18">
              <component :is="item.iconComp" />
            </el-icon>
            <span class="nav-label">{{ item.label }}</span>
            <span v-if="isActive(item.path || '')" class="active-dot"></span>
          </router-link>
        </template>
      </nav>

      <!-- 用户信息 + 主题切换 -->
      <div class="sidebar-footer">
        <div class="user-info" @click="handleLogout">
          <el-avatar :size="32" class="user-avatar">
            {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-detail">
            <div class="user-name">{{ userStore.userInfo?.realName || '用户' }}</div>
            <div class="user-role">{{ roleText }}</div>
          </div>
          <el-icon :size="16" class="logout-icon"><SwitchButton /></el-icon>
        </div>
        <div class="theme-toggle" @click="toggleDark">
          <el-icon :size="16"><Sunny v-if="isDark" /><Moon v-else /></el-icon>
          <span>{{ isDark ? '浅色模式' : '深色模式' }}</span>
        </div>
      </div>
    </aside>

    <!-- 侧边栏遮罩（移动端） -->
    <div class="sidebar-overlay" :class="{ 'sidebar-overlay--visible': sidebarOpen }" @click="sidebarOpen = false"></div>

    <!-- 主内容 -->
    <main class="main-area">
      <header class="app-header glass">
        <!-- 移动端汉堡菜单 -->
        <el-button class="sidebar-hamburger" :icon="Expand" circle size="small" text @click="sidebarOpen = !sidebarOpen" />
        <el-breadcrumb separator="·">
          <el-breadcrumb-item>
            <span style="color: var(--color-text-secondary);">{{ roleText }}工作台</span>
          </el-breadcrumb-item>
          <el-breadcrumb-item v-if="route.meta.title && route.meta.title !== '工作台'">
            {{ route.meta.title }}
          </el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-actions">
          <el-popover
            trigger="click"
            placement="bottom-end"
            width="380"
            popper-class="notification-popover"
            :teleported="false"
          >
            <template #reference>
              <button class="notification-trigger" aria-label="打开通知中心">
                <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
                  <el-icon :size="20" class="header-icon"><Bell /></el-icon>
                </el-badge>
              </button>
            </template>
            <div class="notification-panel">
              <div class="notification-head">
                <div>
                  <div class="notification-title">通知中心</div>
                  <div class="notification-subtitle">{{ unreadCount > 0 ? `${unreadCount} 条未读` : '暂无未读通知' }}</div>
                </div>
                <el-button size="small" text :disabled="unreadCount === 0" @click="markAllNotificationsRead">
                  全部已读
                </el-button>
              </div>
              <div class="notification-list">
                <button
                  v-for="item in notifications"
                  :key="item.id"
                  class="notification-item"
                  :class="{ unread: !isNotificationRead(item.id) }"
                  @click="openNotification(item)"
                >
                  <span class="notification-dot"></span>
                  <span class="notification-content">
                    <span class="notification-name">{{ item.title }}</span>
                    <span class="notification-message">{{ item.content }}</span>
                    <span class="notification-meta">{{ item.time }}</span>
                  </span>
                </button>
              </div>
              <div class="notification-foot">
                当前为前端提醒面板，后续接入站内信后会同步真实已读状态。
              </div>
            </div>
          </el-popover>
          <span class="time-text">{{ currentTime }}</span>
        </div>
      </header>
      <div class="page-content">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell, SwitchButton, Reading, Calendar, List, DataLine, Notebook,
  UserFilled, Setting, Monitor, Sunny, Moon, Clock, View, Star,
  Document, Upload, Checked, Collection, Timer, Expand, Close
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { menuApi } from '@/api'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isDark = ref(false)
const sidebarOpen = ref(false)
const backendMenus = ref<MenuTreeNode[]>([])

const roleText = computed(() => {
  const map: Record<string, string> = { student: '学生', teacher: '教师', academic: '教务', admin: '管理员', qb_admin: '题库管理员' }
  return map[userStore.role] || '用户'
})

type MenuEntry = {
  section?: string
  path?: string
  label?: string
  iconComp?: any
}

type MenuTreeNode = {
  id: number
  menuName: string
  path?: string
  icon?: string
  menuType?: string
  children?: MenuTreeNode[]
}

type NotificationItem = {
  id: string
  title: string
  content: string
  time: string
  path?: string
}

const iconMap: Record<string, any> = {
  Monitor, Reading, Calendar, List, DataLine, Notebook, UserFilled, Setting,
  Clock, View, Star, Document, Upload, Checked, Collection, Timer
}

const menuItems = computed<MenuEntry[]>(() => {
  if (backendMenus.value.length > 0) {
    return flattenBackendMenus(backendMenus.value)
  }

  const role = userStore.role
  const makeItem = (path: string, label: string, icon: any): MenuEntry => ({
    path, label, iconComp: icon
  })
  const makeSection = (label: string): MenuEntry => ({ section: label })

  if (role === 'student') return [
    makeSection('主菜单'),
    makeItem('/dashboard', '工作台', Monitor),
    makeItem('/courses', '课程广场', Reading),
    makeItem('/schedule', '我的课表', Calendar),
    makeItem('/enrollments', '我的选课', List),
    makeSection('学习'),
    makeItem('/scores', '我的成绩', DataLine),
    makeItem('/exams', '考试中心', Timer),
  ]
  if (role === 'teacher') return [
    makeSection('主菜单'),
    makeItem('/teacher', '工作台', Monitor),
    makeItem('/schedule', '我的课表', Calendar),
    makeSection('教学管理'),
    makeItem('/teacher/scores', '成绩录入', DataLine),
    makeItem('/teacher/questions', '题库管理', Collection),
  ]
  if (role === 'academic') return [
    makeSection('主菜单'),
    makeItem('/academic', '工作台', Monitor),
    makeSection('选课管理'),
    makeItem('/academic/rounds', '选课轮次', Clock),
    makeItem('/academic/enroll-monitor', '选课监控', View),
    makeSection('教学管理'),
    makeItem('/academic/schedules', '排课管理', Calendar),
    makeItem('/academic/exams', '考试管理', Timer),
    makeItem('/academic/scores', '成绩审核', DataLine),
    makeItem('/academic/evaluation', '教学评价', Star),
    makeSection('培养管理'),
    makeItem('/academic/training-plan', '培养方案', Document),
    makeItem('/academic/new-student', '新生导入', Upload),
    makeSection('审核'),
    makeItem('/approvals', '课程审核', Checked),
  ]
  if (role === 'admin') return [
    makeSection('主菜单'),
    makeItem('/admin', '工作台', Monitor),
    makeSection('系统管理'),
    makeItem('/admin/users', '用户管理', UserFilled),
    makeItem('/admin/roles', '角色管理', Setting),
    makeSection('审核'),
    makeItem('/approvals', '课程审核', Checked),
  ]
  if (role === 'qb_admin') return [
    makeSection('主菜单'),
    makeItem('/qb-admin', '工作台', Monitor),
    makeItem('/teacher/questions', '题库管理', Collection),
    makeItem('/qb-admin/audit', '题库审核', Checked),
  ]
  return []
})

function flattenBackendMenus(menus: MenuTreeNode[]): MenuEntry[] {
  const result: MenuEntry[] = []

  menus.forEach((menu) => {
    const children = menu.children || []
    const hasPath = Boolean(menu.path)
    const iconComp = iconMap[menu.icon || ''] || Monitor

    if (!hasPath && children.length > 0) {
      result.push({ section: menu.menuName })
      children.forEach((child) => {
        if (child.path) {
          result.push({
            path: child.path,
            label: child.menuName,
            iconComp: iconMap[child.icon || ''] || iconComp,
          })
        }
      })
      return
    }

    if (hasPath) {
      result.push({
        path: menu.path,
        label: menu.menuName,
        iconComp,
      })
    }
  })

  return result
}

function isActive(path: string) {
  if (path === '/dashboard' || path === '/teacher' || path === '/admin' || path === '/academic' || path === '/qb-admin') {
    return route.path === path
  }
  return route.path.startsWith(path)
}

const currentTime = ref('')
const readNotificationIds = ref<string[]>([])
let timer: ReturnType<typeof setInterval>

const notifications = computed<NotificationItem[]>(() => {
  const role = userStore.role
  const common: NotificationItem[] = [
    {
      id: 'system-health',
      title: '系统提醒',
      content: '服务运行正常，今日教学业务可继续办理。',
      time: '刚刚',
      path: getRoleHomePath(role),
    },
  ]

  const roleNotifications: Record<string, NotificationItem[]> = {
    student: [
      { id: 'student-round', title: '选课提醒', content: '当前选课轮次开放中，请及时确认已选课程。', time: '10 分钟前', path: '/courses' },
      { id: 'student-exam', title: '考试通知', content: '考试中心已有成绩记录可查看。', time: '今天', path: '/exams' },
    ],
    teacher: [
      { id: 'teacher-score', title: '成绩录入', content: '请关注待录入或待提交的课程成绩。', time: '今天', path: '/teacher/scores' },
      { id: 'teacher-question', title: '题库提醒', content: '题目维护后可等待题库管理员审核。', time: '今天', path: '/teacher/questions' },
    ],
    academic: [
      { id: 'academic-round', title: '选课监控', content: '建议检查高热度课程容量与学生报名情况。', time: '5 分钟前', path: '/academic/enroll-monitor' },
      { id: 'academic-import', title: '新生导入', content: '导入后可在学生档案列表确认入库结果。', time: '今天', path: '/academic/new-student' },
      { id: 'academic-schedule', title: '排课提醒', content: '排课管理目前仍需补充编辑和冲突检查能力。', time: '今天', path: '/academic/schedules' },
    ],
    admin: [
      { id: 'admin-role', title: '权限管理', content: '角色菜单变更后建议补充审计记录。', time: '今天', path: '/admin/roles' },
      { id: 'admin-approval', title: '课程审核', content: '课程审核列表可处理教师开课申请。', time: '今天', path: '/approvals' },
    ],
    qb_admin: [
      { id: 'qb-audit', title: '题库审核', content: '请检查待审核题目并给出通过或驳回意见。', time: '今天', path: '/qb-admin/audit' },
    ],
  }

  return [...(roleNotifications[role] || []), ...common]
})

const unreadCount = computed(() => notifications.value.filter((item) => !isNotificationRead(item.id)).length)

function updateTime() {
  const now = new Date()
  currentTime.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric', month: 'long', day: 'numeric', weekday: 'long',
  }) + ' ' + now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function getRoleHomePath(role: string) {
  const map: Record<string, string> = {
    student: '/dashboard',
    teacher: '/teacher',
    academic: '/academic',
    admin: '/admin',
    qb_admin: '/qb-admin',
  }
  return map[role] || '/dashboard'
}

function toggleDark() {
  isDark.value = !isDark.value
  document.documentElement.classList.toggle('dark', isDark.value)
  localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
}

async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

function getNotificationStorageKey() {
  const username = userStore.userInfo?.username || userStore.role || 'guest'
  return `smart-edu-read-notifications:${username}`
}

function loadReadNotifications() {
  try {
    const saved = localStorage.getItem(getNotificationStorageKey())
    readNotificationIds.value = saved ? JSON.parse(saved) : []
  } catch {
    readNotificationIds.value = []
  }
}

function saveReadNotifications() {
  localStorage.setItem(getNotificationStorageKey(), JSON.stringify(readNotificationIds.value))
}

function isNotificationRead(id: string) {
  return readNotificationIds.value.includes(id)
}

function markNotificationRead(id: string) {
  if (!readNotificationIds.value.includes(id)) {
    readNotificationIds.value = [...readNotificationIds.value, id]
    saveReadNotifications()
  }
}

function markAllNotificationsRead() {
  readNotificationIds.value = Array.from(new Set([
    ...readNotificationIds.value,
    ...notifications.value.map((item) => item.id),
  ]))
  saveReadNotifications()
}

function openNotification(item: NotificationItem) {
  markNotificationRead(item.id)
  if (item.path && route.path !== item.path) {
    router.push(item.path)
  }
}

async function fetchMenus() {
  try {
    backendMenus.value = await menuApi.myMenus()
  } catch {
    backendMenus.value = []
  }
}

onMounted(async () => {
  updateTime()
  timer = setInterval(updateTime, 30000)
  const savedTheme = localStorage.getItem('theme')
  isDark.value = savedTheme === 'dark'
  document.documentElement.classList.toggle('dark', isDark.value)
  document.documentElement.classList.toggle('light', !isDark.value)
  loadReadNotifications()
  await fetchMenus()
})

onUnmounted(() => clearInterval(timer))
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
}

// 玻璃态侧边栏
.sidebar-glass {
  width: 252px;
  min-height: 100vh;
  position: fixed;
  left: 0; top: 0; bottom: 0;
  z-index: 100;
  display: flex;
  flex-direction: column;
  background: linear-gradient(
    180deg,
    rgba(30, 20, 60, 0.85) 0%,
    rgba(20, 15, 45, 0.9) 50%,
    rgba(15, 10, 35, 0.92) 100%
  );
  backdrop-filter: blur(24px) saturate(1.4);
  -webkit-backdrop-filter: blur(24px) saturate(1.4);
  border-right: 1px solid rgba(148, 163, 184, 0.14);
  box-shadow: 18px 0 56px rgba(15, 23, 42, 0.18);
}

.logo-area {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 22px 20px;
  border-bottom: 1px solid rgba(139, 92, 246, 0.12);
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    background: linear-gradient(135deg, #ffffff, #67e8f9);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    letter-spacing: 0;
  }
  .sidebar-close-btn {
    display: none;
    margin-left: auto;
    color: rgba(255,255,255,0.5);
  }
}

// 菜单导航
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 12px;
}

.nav-section {
  padding: 12px 12px 4px;
  font-size: 10px;
  font-weight: 600;
  color: rgba(148, 163, 184, 0.62);
  text-transform: uppercase;
  letter-spacing: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  margin: 1px 0;
  border-radius: 8px;
  color: rgba(226, 232, 240, 0.66);
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  // 悬浮紫光从左滑到右
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, transparent, rgba(34, 211, 238, 0.08), transparent);
    transform: translateX(-100%);
    transition: transform 0.5s ease;
    pointer-events: none;
  }

  &:hover {
    color: rgba(255, 255, 255, 0.9);
    background: rgba(148, 163, 184, 0.09);
    &::after { transform: translateX(0); }
  }

  &.active {
    color: #fff;
    background: linear-gradient(135deg, rgba(79, 70, 229, 0.32), rgba(6, 182, 212, 0.18));
    animation: sidebarGlow 3s ease-in-out infinite;
    box-shadow: 0 10px 24px rgba(6, 182, 212, 0.12);
    font-weight: 600;

    .nav-icon { opacity: 1; }
  }

  .nav-label { flex: 1; }
}

.active-dot {
  width: 4px; height: 4px;
  border-radius: 50%;
  background: #22d3ee;
  animation: breathe 2s ease-in-out infinite;
}

// 侧边栏底部
.sidebar-footer {
  border-top: 1px solid rgba(139, 92, 246, 0.12);
  padding: 12px 14px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  &:hover { background: rgba(148, 163, 184, 0.09); }
  .user-detail { flex: 1; min-width: 0; }
  .user-name { font-size: 13px; color: rgba(255,255,255,0.85); font-weight: 500; }
  .user-role { font-size: 11px; color: rgba(255,255,255,0.4); }
  .logout-icon { color: rgba(255,255,255,0.3); }
}

.theme-toggle {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  margin-top: 6px;
  border-radius: 8px;
  font-size: 11px;
  color: rgba(255,255,255,0.35);
  cursor: pointer;
  transition: all 0.2s;
  &:hover { background: rgba(148, 163, 184, 0.09); color: rgba(255,255,255,0.6); }
}

// 主内容
.main-area {
  margin-left: 252px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.app-header {
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 28px;
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.72) !important;
  border-bottom: 1px solid rgba(79, 70, 229, 0.08);

  .sidebar-hamburger {
    display: none;
    color: var(--color-text-secondary);
    margin-right: 4px;
  }

  html.dark & {
    background: rgba(11, 16, 32, 0.72) !important;
    border-bottom-color: rgba(139, 92, 246, 0.1);
  }

  .header-actions {
    margin-left: auto;
    display: flex;
    align-items: center;
    gap: 18px;
  }
  .header-icon { color: var(--color-text-secondary); }
  .notification-trigger {
    width: 36px;
    height: 36px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border: 1px solid rgba(148, 163, 184, 0.22);
    border-radius: 8px;
    background: rgba(255, 255, 255, 0.66);
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover {
      border-color: rgba(79, 70, 229, 0.28);
      background: rgba(255, 255, 255, 0.92);
      box-shadow: 0 10px 24px rgba(79, 70, 229, 0.12);

      .header-icon {
        color: #4f46e5;
      }
    }
  }
  .time-text { font-size: 12px; color: var(--color-text-muted); }
}

.notification-panel {
  padding: 4px;
}

.notification-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 4px 12px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.16);
}

.notification-title {
  font-size: 15px;
  line-height: 22px;
  font-weight: 700;
  color: #111827;
}

.notification-subtitle {
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 320px;
  overflow-y: auto;
  padding: 10px 0;
}

.notification-item {
  width: 100%;
  display: grid;
  grid-template-columns: 8px 1fr;
  gap: 10px;
  padding: 11px 10px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover {
    border-color: rgba(79, 70, 229, 0.14);
    background: linear-gradient(135deg, rgba(79, 70, 229, 0.07), rgba(6, 182, 212, 0.05));
  }

  &.unread {
    background: rgba(79, 70, 229, 0.05);

    .notification-dot {
      background: #4f46e5;
      box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.12);
    }

    .notification-name {
      color: #1e1b4b;
    }
  }
}

.notification-dot {
  width: 7px;
  height: 7px;
  margin-top: 7px;
  border-radius: 999px;
  background: #cbd5e1;
}

.notification-content {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 3px;
}

.notification-name {
  font-size: 13px;
  line-height: 18px;
  font-weight: 700;
  color: #334155;
}

.notification-message {
  font-size: 12px;
  line-height: 18px;
  color: #64748b;
}

.notification-meta {
  font-size: 11px;
  line-height: 16px;
  color: #94a3b8;
}

.notification-foot {
  margin-top: 2px;
  padding: 10px 4px 2px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  font-size: 11px;
  line-height: 18px;
  color: #94a3b8;
}

.page-content {
  padding: 24px 28px;
  flex: 1;
  position: relative;
}

// 移动端：侧边栏内关闭按钮可见
@media (max-width: 768px) {
  .sidebar-close-btn {
    display: flex !important;
  }
}
</style>
