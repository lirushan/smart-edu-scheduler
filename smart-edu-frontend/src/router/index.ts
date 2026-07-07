import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const AppLayout = () => import('@/components/AppLayout.vue')

// 学生端
const StudentDashboard = () => import('@/views/student/Dashboard.vue')
const CourseMarket = () => import('@/views/student/CourseMarket.vue')
const MySchedule = () => import('@/views/student/MySchedule.vue')
const MyEnrollments = () => import('@/views/student/MyEnrollments.vue')
const MyScores = () => import('@/views/student/MyScores.vue')
const ExamCenter = () => import('@/views/student/ExamCenter.vue')

// 教师端
const TeacherDashboard = () => import('@/views/teacher/Dashboard.vue')
const ScoreEntry = () => import('@/views/teacher/ScoreEntry.vue')
const QuestionBank = () => import('@/views/teacher/QuestionBank.vue')

// 管理端
const AdminDashboard = () => import('@/views/admin/Dashboard.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')
const CourseApproval = () => import('@/views/admin/CourseApproval.vue')
const RoleManagement = () => import('@/views/admin/RoleManagement.vue')

// 教务端
const AcademicDashboard = () => import('@/views/academic/Dashboard.vue')
const RoundConfig = () => import('@/views/academic/RoundConfig.vue')

// Skeleton pages
const SkeletonPage = () => import('@/components/SkeletonPage.vue')

// 题库管理端
const QbAdminDashboard = () => import('@/views/qb-admin/Dashboard.vue')
const QuestionAudit = () => import('@/views/qb-admin/QuestionAudit.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: AppLayout,
    redirect: '/dashboard',
    children: [
      // ===== 通用（角色自适应）=====
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => {
          const role = localStorage.getItem('userRole') || 'student'
          const map: Record<string, () => Promise<any>> = {
            student: () => import('@/views/student/Dashboard.vue'),
            teacher: () => import('@/views/teacher/Dashboard.vue'),
            academic: () => import('@/views/academic/Dashboard.vue'),
            admin: () => import('@/views/admin/Dashboard.vue'),
            qb_admin: () => import('@/views/qb-admin/Dashboard.vue'),
          }
          return (map[role] || map.student)()
        },
        meta: { title: '工作台' },
      },

      // ===== 学生端 =====
      { path: 'courses', name: 'CourseMarket', component: CourseMarket, meta: { title: '课程广场', role: 'student' } },
      { path: 'schedule', name: 'MySchedule', component: MySchedule, meta: { title: '我的课表' } },
      { path: 'enrollments', name: 'MyEnrollments', component: MyEnrollments, meta: { title: '我的选课', role: 'student' } },
      { path: 'scores', name: 'MyScores', component: MyScores, meta: { title: '我的成绩', role: 'student' } },
      { path: 'exams', name: 'ExamCenter', component: ExamCenter, meta: { title: '考试中心', role: 'student' } },

      // ===== 教师端 =====
      { path: 'teacher', name: 'TeacherDashboard', component: TeacherDashboard, meta: { title: '工作台', role: 'teacher' } },
      { path: 'teacher/scores', name: 'ScoreEntry', component: ScoreEntry, meta: { title: '成绩录入', role: 'teacher' } },
      { path: 'teacher/questions', name: 'QuestionBank', component: QuestionBank, meta: { title: '题库管理', role: 'teacher,qb_admin' } },

      // ===== 管理端 =====
      { path: 'admin', name: 'AdminDashboard', component: AdminDashboard, meta: { title: '工作台', role: 'admin' } },
      { path: 'admin/users', name: 'UserManagement', component: UserManagement, meta: { title: '用户管理', role: 'admin' } },
      { path: 'admin/roles', name: 'RoleManagement', component: RoleManagement, meta: { title: '角色管理', role: 'admin' } },
      { path: 'approvals', name: 'CourseApproval', component: CourseApproval, meta: { title: '课程审核', role: 'admin,academic' } },

      // ===== 教务端 =====
      { path: 'academic', name: 'AcademicDashboard', component: AcademicDashboard, meta: { title: '工作台', role: 'academic' } },
      { path: 'academic/rounds', name: 'RoundConfig', component: RoundConfig, meta: { title: '选课轮次', role: 'academic' } },
      { path: 'academic/enroll-monitor', name: 'EnrollmentMonitor', component: () => SkeletonPage, meta: { title: '选课监控', role: 'academic' }, props: { title: '选课监控' } },
      { path: 'academic/schedules', name: 'ScheduleManagement', component: () => SkeletonPage, meta: { title: '排课管理', role: 'academic' }, props: { title: '排课管理' } },
      { path: 'academic/exams', name: 'ExamManagement', component: () => SkeletonPage, meta: { title: '考试管理', role: 'academic' }, props: { title: '考试管理' } },
      { path: 'academic/scores', name: 'ScoreApproval', component: () => SkeletonPage, meta: { title: '成绩审核', role: 'academic' }, props: { title: '成绩审核' } },
      { path: 'academic/evaluation', name: 'TeachingEvaluation', component: () => SkeletonPage, meta: { title: '教学评价', role: 'academic' }, props: { title: '教学评价' } },
      { path: 'academic/training-plan', name: 'TrainingPlan', component: () => SkeletonPage, meta: { title: '培养方案', role: 'academic' }, props: { title: '培养方案' } },
      { path: 'academic/new-student', name: 'NewStudentImport', component: () => SkeletonPage, meta: { title: '新生导入', role: 'academic' }, props: { title: '新生导入' } },

      // ===== 题库管理端 =====
      { path: 'qb-admin', name: 'QbAdminDashboard', component: QbAdminDashboard, meta: { title: '工作台', role: 'qb_admin' } },
      { path: 'qb-admin/audit', name: 'QuestionAudit', component: QuestionAudit, meta: { title: '题库审核', role: 'qb_admin' } },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  document.title = `${to.meta.title || '智教通'} — 智教通`

  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 角色校验
  const requiredRoles = (to.meta.role as string)?.split(',') || []
  if (requiredRoles.length > 0) {
    const userRole = localStorage.getItem('userRole') || 'student'
    if (!requiredRoles.includes(userRole)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
