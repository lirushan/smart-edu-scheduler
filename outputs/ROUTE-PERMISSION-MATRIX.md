# 前端路由权限矩阵报告 (ROUTE-PERMISSION-MATRIX)

> **生成时间**: 2026-07-07
> **版本**: v1.0
> **分析范围**: 静态代码分析（路由配置 + 路由守卫 + 导航菜单）

---

## 一、角色定义

| 角色标识 | 中文名称 | userType | 说明 |
|:---|:---|:---:|:---|
| `student` | 学生 | 1 | 选课、看成绩、考试 |
| `teacher` | 教师 | 2 | 录成绩、管题库 |
| `academic` | 教务 | 3 | 排课、考试管理、成绩审核、培养方案 |
| `admin` | 管理员 | 4 | 用户管理、角色管理、课程审核 |
| `qb_admin` | 题库管理员 | 5 | 题库审核、题库管理 |

---

## 二、角色-路由权限矩阵

| # | 路由路径 | 路由名称 | 限制角色 (`meta.role`) | 学生 | 教师 | 教务 | 管理员 | 题库管理 |
|:---:|:---|:---|:---|:---:|:---:|:---:|:---:|:---:|
| 1 | `/login` | Login | 无 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 2 | `/dashboard` | Dashboard | 无 (角色自适应) | ✅ | ✅ | ✅ | ✅ | ✅ |
| 3 | `/courses` | CourseMarket | `student` | ✅ | ❌ | ❌ | ❌ | ❌ |
| 4 | `/schedule` | MySchedule | ⚠️ **无** | ✅ | ✅ | ✅ | ✅ | ✅ |
| 5 | `/enrollments` | MyEnrollments | `student` | ✅ | ❌ | ❌ | ❌ | ❌ |
| 6 | `/scores` | MyScores | `student` | ✅ | ❌ | ❌ | ❌ | ❌ |
| 7 | `/exams` | ExamCenter | `student` | ✅ | ❌ | ❌ | ❌ | ❌ |
| 8 | `/teacher` | TeacherDashboard | `teacher` | ❌ | ✅ | ❌ | ❌ | ❌ |
| 9 | `/teacher/scores` | ScoreEntry | `teacher` | ❌ | ✅ | ❌ | ❌ | ❌ |
| 10 | `/teacher/questions` | QuestionBank | `teacher,qb_admin` | ❌ | ✅ | ❌ | ❌ | ✅ |
| 11 | `/admin` | AdminDashboard | `admin` | ❌ | ❌ | ❌ | ✅ | ❌ |
| 12 | `/admin/users` | UserManagement | `admin` | ❌ | ❌ | ❌ | ✅ | ❌ |
| 13 | `/admin/roles` | RoleManagement | `admin` | ❌ | ❌ | ❌ | ✅ | ❌ |
| 14 | `/approvals` | CourseApproval | `admin,academic` | ❌ | ❌ | ✅ | ✅ | ❌ |
| 15 | `/academic` | AcademicDashboard | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 16 | `/academic/rounds` | RoundConfig | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 17 | `/academic/enroll-monitor` | EnrollmentMonitor | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 18 | `/academic/schedules` | ScheduleManagement | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 19 | `/academic/exams` | ExamManagement | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 20 | `/academic/scores` | ScoreApproval | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 21 | `/academic/evaluation` | TeachingEvaluation | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 22 | `/academic/training-plan` | TrainingPlan | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 23 | `/academic/new-student` | NewStudentImport | `academic` | ❌ | ❌ | ✅ | ❌ | ❌ |
| 24 | `/qb-admin` | QbAdminDashboard | `qb_admin` | ❌ | ❌ | ❌ | ❌ | ✅ |
| 25 | `/qb-admin/audit` | QuestionAudit | `qb_admin` | ❌ | ❌ | ❌ | ❌ | ✅ |
| 26 | `/:pathMatch(.*)*` | — | 无 (Catch-all) | → `/dashboard` | → `/dashboard` | → `/dashboard` | → `/dashboard` | → `/dashboard` |

> **图例**: ✅ = 可访问 | ❌ = 路由守卫拦截 | → = 自动重定向

---

## 三、路由守卫逻辑验证

### 3.1 守卫代码分析

**位置**: `smart-edu-frontend/src/router/index.ts` 第 116-136 行

```typescript
router.beforeEach((to, _from, next) => {
  // 1. 设置页面标题
  document.title = `${to.meta.title || '智教通'} — 智教通`

  // 2. 认证检查：未登录且非登录页 → 跳转 /login
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 3. 角色校验：有 role 限制且角色不匹配 → 跳转 /dashboard
  const requiredRoles = (to.meta.role as string)?.split(',') || []
  if (requiredRoles.length > 0) {
    const userRole = localStorage.getItem('userRole') || 'student'
    if (!requiredRoles.includes(userRole)) {
      next('/dashboard')
      return
    }
  }

  // 4. 正常通过
  next()
})
```

### 3.2 四条核心逻辑验证

| # | 场景 | 预期行为 | 实际行为 | 判定 |
|:---:|:---|:---|:---|:---:|
| A | 未登录 → 访问 `/courses` | 跳转 `/login` | `token` 为 null → `next('/login')` | ✅ PASS |
| B | 已登录(student) → 访问 `/admin/users` | 跳转 `/dashboard` | `requiredRoles=['admin']`, `userRole='student'` 不匹配 → `next('/dashboard')` | ✅ PASS |
| C | 已登录(teacher) → 访问 `/teacher/scores` | 正常通过 | `requiredRoles=['teacher']`, `userRole='teacher'` 匹配 → `next()` | ✅ PASS |
| D | 已登录 → 访问 `/nonexistent` | 跳转 `/dashboard` | Catch-all `/:pathMatch(.*)*` → redirect `/dashboard` | ✅ PASS |

### 3.3 多角色路由验证

| # | 场景 | 路由 | 预期 | 实际 | 判定 |
|:---:|:---|:---|:---:|:---:|:---:|
| E | teacher 访问 `/teacher/questions` | `teacher,qb_admin` | ✅ | `['teacher','qb_admin'].includes('teacher')` → pass | ✅ PASS |
| F | qb_admin 访问 `/teacher/questions` | `teacher,qb_admin` | ✅ | `['teacher','qb_admin'].includes('qb_admin')` → pass | ✅ PASS |
| G | academic 访问 `/approvals` | `admin,academic` | ✅ | `['admin','academic'].includes('academic')` → pass | ✅ PASS |
| H | admin 访问 `/approvals` | `admin,academic` | ✅ | `['admin','academic'].includes('admin')` → pass | ✅ PASS |
| I | student 访问 `/approvals` | `admin,academic` | ❌ | `['admin','academic'].includes('student')` → false → redirect | ✅ PASS |

---

## 四、发现的权限缺陷与风险

### 🔴 严重: ISSUE-01 — `/schedule` 路由缺少 `meta.role` 限制

| 项目 | 内容 |
|:---|:---|
| **位置** | `router/index.ts` 第 71 行 |
| **当前代码** | `{ path: 'schedule', name: 'MySchedule', component: MySchedule, meta: { title: '我的课表' } }` |
| **问题** | 该路由的组件是 `@/views/student/MySchedule.vue`（学生端组件），但 **没有设置 `meta.role`**。路由守卫会跳过角色校验（`requiredRoles.length === 0`），导致**所有已登录角色**（教师、教务、管理员、题库管理员）都能访问学生端的课表页面。 |
| **风险等级** | 🔴 高 |
| **影响** | 教师(teacher)也会在侧边栏菜单中看到此路由（AppLayout 第 149 行显式包含 `/schedule`），但加载的是学生端组件 `student/MySchedule.vue`，可能显示学生数据而非教师数据。 |
| **修复建议** | 根据业务需求二选一：<br>① 若课表为学生专属 → 添加 `meta: { role: 'student' }`，并从 AppLayout 教师菜单中移除 `/schedule`<br>② 若教师也需要课表 → 创建独立的教师课表组件 `teacher/MySchedule.vue`，或通过动态组件加载（类似 `/dashboard` 的做法） |

### 🟡 中等: ISSUE-02 — 路由守卫绕过了 Pinia Store，直接读取 localStorage

| 项目 | 内容 |
|:---|:---|
| **位置** | `router/index.ts` 第 119、128 行 |
| **问题** | 路由守卫使用 `localStorage.getItem('token')` 和 `localStorage.getItem('userRole')` 做认证/鉴权判断，而不是使用 Pinia Store 的响应式状态 (`useUserStore().isLoggedIn` / `useUserStore().role`)。 |
| **风险等级** | 🟡 中 |
| **影响** | 虽然 store 会在 login 和 fetchUserInfo 时同步写入 localStorage，但如果在 `fetchUserInfo()` 异步调用完成前路由切换，或 localStorage 写入失败，守卫会使用过期/不正确的角色信息。此外，**token 过期/失效后若未清除 localStorage**，守卫仍认为用户已登录，可能导致请求 API 时 401 但路由层面未拦截。 |
| **修复建议** | 考虑在守卫中引入 `useUserStore()` 并结合 token 有效性验证（如检查 token 是否过期）。当前实现可接受，但建议在后期迭代中增强。 |

### 🟡 中等: ISSUE-03 — 教师菜单中包含学生端路由组件

| 项目 | 内容 |
|:---|:---|
| **位置** | `AppLayout.vue` 第 146-153 行，教师菜单 `menuItems` |
| **问题** | 教师菜单中第 149 行包含 `makeItem('/schedule', '我的课表', Calendar)`，然而 `/schedule` 路由加载的是 `@/views/student/MySchedule.vue`。这暗示"教师也看学生课表"——但这在语义上是可疑的。教师应该看到他们所教课程的课表，而非学生视角的课表。 |
| **风险等级** | 🟡 中 |
| **影响** | 教师通过菜单进入 `/schedule` 会看到学生端 UI，可能功能不完全或数据不对。若后端 API 有行级权限控制，则影响降为 UI 混乱；若没有，则可能暴露不该看到的数据。 |
| **修复建议** | 与 ISSUE-01 联动修复。明确 `/schedule` 的设计意图后统一定义角色策略。 |

### 🟢 低: ISSUE-04 — Dashboard 动态组件加载也绕过 Store

| 项目 | 内容 |
|:---|:---|
| **位置** | `router/index.ts` 第 56 行 |
| **问题** | Dashboard 的 `component` 字段使用 `localStorage.getItem('userRole')` 决定加载哪个组件，而非 Pinia Store。 |
| **风险等级** | 🟢 低 |
| **影响** | 与 ISSUE-02 同类问题，但影响较小（Dashboard 没有角色限制，只是 UI 展示差异）。最坏情况：显示错误角色的 Dashboard 布局。 |
| **修复建议** | 可考虑改为在 Dashboard 组件内部根据 store 状态动态渲染，而非在路由配置中判断。 |

---

## 五、侧边栏导航菜单权限对比

### 5.1 菜单-路由对照

| 菜单可见角色 | 菜单项 | 路由路径 | 路由守卫允许 | 一致性 |
|:---|:---|:---|:---|:---:|
| student | 工作台 | `/dashboard` | ✅ (无限制) | ✅ |
| student | 课程广场 | `/courses` | ✅ (student) | ✅ |
| student | 我的课表 | `/schedule` | ✅ (⚠️ 无限制) | ⚠️ |
| student | 我的选课 | `/enrollments` | ✅ (student) | ✅ |
| student | 我的成绩 | `/scores` | ✅ (student) | ✅ |
| student | 考试中心 | `/exams` | ✅ (student) | ✅ |
| teacher | 工作台 | `/teacher` | ✅ (teacher) | ✅ |
| teacher | 我的课表 | `/schedule` | ✅ (⚠️ 无限制) | ⚠️ |
| teacher | 成绩录入 | `/teacher/scores` | ✅ (teacher) | ✅ |
| teacher | 题库管理 | `/teacher/questions` | ✅ (teacher,qb_admin) | ✅ |
| academic | 工作台 | `/academic` | ✅ (academic) | ✅ |
| academic | 选课轮次 | `/academic/rounds` | ✅ (academic) | ✅ |
| academic | 选课监控 | `/academic/enroll-monitor` | ✅ (academic) | ✅ |
| academic | 排课管理 | `/academic/schedules` | ✅ (academic) | ✅ |
| academic | 考试管理 | `/academic/exams` | ✅ (academic) | ✅ |
| academic | 成绩审核 | `/academic/scores` | ✅ (academic) | ✅ |
| academic | 教学评价 | `/academic/evaluation` | ✅ (academic) | ✅ |
| academic | 培养方案 | `/academic/training-plan` | ✅ (academic) | ✅ |
| academic | 新生导入 | `/academic/new-student` | ✅ (academic) | ✅ |
| academic | 课程审核 | `/approvals` | ✅ (admin,academic) | ✅ |
| admin | 工作台 | `/admin` | ✅ (admin) | ✅ |
| admin | 用户管理 | `/admin/users` | ✅ (admin) | ✅ |
| admin | 角色管理 | `/admin/roles` | ✅ (admin) | ✅ |
| admin | 课程审核 | `/approvals` | ✅ (admin,academic) | ✅ |
| qb_admin | 工作台 | `/qb-admin` | ✅ (qb_admin) | ✅ |
| qb_admin | 题库管理 | `/teacher/questions` | ✅ (teacher,qb_admin) | ✅ |
| qb_admin | 题库审核 | `/qb-admin/audit` | ✅ (qb_admin) | ✅ |

> ⚠️ = `/schedule` 在菜单层面虽然是按角色显示的，但路由守卫层面无限制，存在菜单-路由守卫不一致。

---

## 六、总结

### 6.1 整体评估

| 维度 | 状态 | 说明 |
|:---|:---:|:---|
| 路由元数据完整性 | ⚠️ 基本完整 | 25 条路由中 22 条有正确 `meta.role`，1 条缺失 (`/schedule`)，2 条无需限制 (`/login`, `/dashboard`) |
| 路由守卫正确性 | ✅ 正确 | 4 个核心场景全部通过 |
| 多角色支持 | ✅ 正确 | `teacher,qb_admin` 和 `admin,academic` 逗号分隔模式工作正常 |
| Catch-all 404 | ✅ 正确 | `/:pathMatch(.*)*` 重定向到 `/dashboard` |
| 菜单-路由一致性 | ⚠️ 基本一致 | 除 `/schedule` 外所有菜单项与路由守卫对齐 |
| 越权风险 | 🔴 存在 | `/schedule` 无角色限制，任何登录用户可访问学生端课表 |

### 6.2 修复优先级

| 优先级 | Issue | 修复工作量 |
|:---:|:---|:---:|
| 🔴 P0 | ISSUE-01: `/schedule` 添加 `meta.role` | 5 分钟 (1 行改动) |
| 🟡 P1 | ISSUE-03: 明确教师课表需求，清理菜单/路由不一致 | 30 分钟 |
| 🟡 P2 | ISSUE-02: 路由守卫接入 Store + token 过期检查 | 1 小时 |
| 🟢 P3 | ISSUE-04: Dashboard 组件加载逻辑重构 | 30 分钟 |

### 6.3 未发现的问题

以下场景经检查确认**不存在**风险：
- ❌ 不存在无 token 可绕过角色检查的路径（`/login` 外所有路径都在 AppLayout 的 children 中，进入前经过守卫）
- ❌ 不存在角色元数据拼写错误（所有 role 值与 userStore 中定义的五种角色一致）
- ❌ 不存在管理员可见的隐藏路由被遗漏在侧边栏外
- ❌ 不存在循环重定向风险（`/login` 不检查 token，`/dashboard` 不检查 role）
