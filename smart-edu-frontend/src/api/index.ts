import http from './http'

// ===== 认证 =====
export const authApi = {
  login: (params: { username: string; password: string }) =>
    http.post('/auth/login', params),
  logout: () => {
    const refreshToken = localStorage.getItem('refreshToken') || ''
    return http.post('/auth/logout', { refreshToken })
  },
  refreshToken: (refreshToken: string) =>
    http.post('/auth/refresh', { refreshToken }),
  me: () => http.get('/auth/me'),
}

// ===== 菜单 =====
export const menuApi = {
  myMenus: () => http.get('/menus/my'),
}

// ===== 课程 =====
export const courseApi = {
  list: (params?: Record<string, any>) =>
    http.get('/courses/offerings', { params }),
  detail: (id: number) => http.get(`/courses/offerings/${id}`),
}

// ===== 选课 =====
export const enrollmentApi = {
  myList: () => http.get('/enrollments/my'),
  enroll: (offeringId: number) => http.post('/enrollments', { offeringId }),
  drop: (id: number) => http.delete(`/enrollments/${id}`),
  log: () => http.get('/enrollments/log'),
}

// ===== 课表 =====
export const scheduleApi = {
  mySchedule: () => http.get('/schedules/my'),
  studentSchedule: (studentId: number) => http.get(`/schedules/student/${studentId}`),
}

// ===== 成绩 =====
export const scoreApi = {
  myScores: () => http.get('/scores/my'),
  byOffering: (offeringId: number) => http.get(`/scores/offering/${offeringId}`),
  update: (id: number, data: any) => http.put(`/scores/${id}`, data),
  batch: (data: any[]) => http.post('/scores/batch', data),
  publish: (id: number) => http.put(`/scores/${id}/publish`),
}

// ===== 考试 =====
export const examApi = {
  list: () => http.get('/exams'),
  detail: (id: number) => http.get(`/exams/${id}`),
  questions: (id: number) => http.get(`/exams/${id}/questions`),
  start: (id: number) => http.post(`/exams/${id}/start`),
  submit: (id: number, answers: any[]) => http.post(`/exams/${id}/submit`, { answers }),
  myResult: (id: number) => http.get(`/exams/${id}/results/my`),
}

// ===== 题库 =====
export const questionApi = {
  list: (params?: Record<string, any>) => http.get('/questions', { params }),
  detail: (id: number) => http.get(`/questions/${id}`),
  create: (data: any) => http.post('/questions', data),
  update: (id: number, data: any) => http.put(`/questions/${id}`, data),
  delete: (id: number) => http.delete(`/questions/${id}`),
  auditList: (params?: Record<string, any>) => http.get('/questions/audit/list', { params }),
  audit: (id: number, data: any) => http.put(`/questions/${id}/audit`, data),
}

// ===== 用户管理 =====
export const userApi = {
  list: (params?: Record<string, any>) => http.get('/admin/users', { params }),
  create: (data: any) => http.post('/admin/users', data),
  update: (id: number, data: any) => http.put(`/admin/users/${id}`, data),
  toggleStatus: (id: number, status: number) => http.put(`/admin/users/${id}/status`, { status }),
  delete: (id: number) => http.delete(`/admin/users/${id}`),
}

// ===== 角色管理 =====
export const roleApi = {
  list: (params?: Record<string, any>) => http.get('/admin/roles', { params }),
  detail: (id: number) => http.get(`/admin/roles/${id}`),
  menuTree: () => http.get('/admin/roles/menus/tree'),
  create: (data: { roleName: string; roleCode: string; description?: string }) =>
    http.post('/admin/roles', data),
  update: (id: number, data: { roleName: string; description?: string }) =>
    http.put(`/admin/roles/${id}`, data),
  toggleStatus: (id: number, status: number) => http.put(`/admin/roles/${id}/status`, { status }),
  updateMenus: (id: number, menuIds: number[]) => http.put(`/admin/roles/${id}/menus`, { menuIds }),
}

// ===== 课程审核 =====
export const approvalApi = {
  list: (params?: Record<string, any>) => http.get('/approvals/offerings', { params }),
  detail: (id: number) => http.get(`/approvals/offerings/${id}`),
  approve: (id: number, comment?: string) => http.put(`/approvals/offerings/${id}/approve`, { comment: comment || '' }),
  reject: (id: number, comment: string) => http.put(`/approvals/offerings/${id}/reject`, { comment }),
}

// ===== 选课轮次 =====
export const roundApi = {
  list: () => http.get('/academic/rounds'),
  create: (data: any) => http.post('/academic/rounds', data),
  update: (id: number, data: any) => http.put(`/academic/rounds/${id}`, data),
  delete: (id: number) => http.delete(`/academic/rounds/${id}`),
  toggleStatus: (id: number, status: number) => http.put(`/academic/rounds/${id}/status`, { status }),
}

// ===== 教务排课管理 =====
export const academicScheduleApi = {
  semesters: () => http.get('/academic/schedules/semesters'),
  offerings: () => http.get('/academic/schedules/offerings'),
  schedules: (params?: Record<string, any>) => http.get('/academic/schedules', { params }),
}

// ===== 教务考试管理 =====
export const academicExamApi = {
  list: (params?: Record<string, any>) => http.get('/academic/exams', { params }),
  detail: (id: number) => http.get(`/academic/exams/${id}`),
  create: (data: any) => http.post('/academic/exams', data),
  update: (id: number, data: any) => http.put(`/academic/exams/${id}`, data),
  delete: (id: number) => http.delete(`/academic/exams/${id}`),
}

// ===== 教务成绩审核 =====
export const academicScoreApi = {
  reviewList: (params?: Record<string, any>) => http.get('/academic/scores/review', { params }),
  approve: (id: number) => http.put(`/academic/scores/${id}/approve`),
  batchApprove: (ids: number[]) => http.put('/academic/scores/batch-approve', { ids }),
  reject: (id: number) => http.put(`/academic/scores/${id}/reject`),
}

// ===== 教务教学评价 =====
export const academicEvaluationApi = {
  stats: () => http.get('/academic/evaluations'),
  teacherDetails: (teacherId: number) => http.get(`/academic/evaluations/${teacherId}`),
  allDetails: () => http.get('/academic/evaluations/all'),
}

// ===== 教务选课监控 =====
export const academicEnrollmentApi = {
  stats: () => http.get('/academic/enrollments/stats'),
  details: (offeringId?: number) => http.get('/academic/enrollments/details', { params: offeringId ? { offeringId } : {} }),
}

// ===== 教务培养方案 =====
export const academicTrainingPlanApi = {
  list: (params?: Record<string, any>) => http.get('/academic/training-plans', { params }),
  detail: (id: number) => http.get(`/academic/training-plans/${id}`),
  create: (data: any) => http.post('/academic/training-plans', data),
  update: (id: number, data: any) => http.put(`/academic/training-plans/${id}`, data),
  delete: (id: number) => http.delete(`/academic/training-plans/${id}`),
}

// ===== 教务学生导入 =====
export const academicStudentApi = {
  preview: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post('/academic/students/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  import: (students: any[]) => http.post('/academic/students/import', students),
}
