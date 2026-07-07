-- ============================================================
-- 智教通 V1.0 角色菜单绑定脚本
-- 适用数据库：smart_edu
--
-- 用途：
-- 1. 确保 5 个内置角色存在。
-- 2. 确保 5 个角色对应的菜单记录存在。
-- 3. 幂等补齐 sys_role_menu 绑定关系。
--
-- 默认脚本是非破坏式的：
-- - 不删除角色
-- - 不删除菜单
-- - 不清空角色菜单绑定
-- - 已存在的数据用 INSERT IGNORE / ON DUPLICATE KEY UPDATE 处理
--
-- 如需“精确重置”5 个内置角色的菜单绑定，请人工确认后再执行
-- 文件底部的 OPTIONAL 精确重置段。
-- ============================================================

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
USE smart_edu;

START TRANSACTION;

-- ===== 1. 内置角色 =====
INSERT INTO sys_role (id, role_code, role_name, description, status) VALUES
(1, 'STUDENT',  '学生',       '学生角色',       1),
(2, 'TEACHER',  '教师',       '教师角色',       1),
(3, 'ACADEMIC', '教务',       '教务管理人员',   1),
(4, 'ADMIN',    '管理员',     '系统管理员',     1),
(5, 'QB_ADMIN', '题库管理员', '题库审核与管理', 1)
ON DUPLICATE KEY UPDATE
  role_code = VALUES(role_code),
  role_name = VALUES(role_name),
  description = VALUES(description),
  status = VALUES(status);

-- ===== 2. 菜单表 =====
INSERT INTO sys_menu
  (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort_order, visible, status)
VALUES
-- 学生菜单
(1,  0, '工作台',   '/dashboard',        'student/Dashboard',     'Monitor',    'M', '', 1, 1, 1),
(2,  0, '课程广场', '/courses',          'student/CourseMarket',  'Reading',    'M', '', 2, 1, 1),
(3,  0, '我的课表', '/schedule',         'student/MySchedule',    'Calendar',   'M', '', 3, 1, 1),
(4,  0, '我的选课', '/enrollments',      'student/MyEnrollments', 'List',       'M', '', 4, 1, 1),
(5,  0, '学习中心', '',                  '',                      'Notebook',   'C', '', 5, 1, 1),
(6,  5, '我的成绩', '/scores',           'student/MyScores',      'DataLine',   'M', '', 1, 1, 1),
(7,  5, '考试中心', '/exams',            'student/ExamCenter',    'Timer',      'M', '', 2, 1, 1),

-- 教师菜单
(10, 0,  '工作台',   '/teacher',           'teacher/Dashboard',    'Monitor',    'M', '', 1, 1, 1),
(11, 0,  '教学管理', '',                   '',                     'Notebook',   'C', '', 2, 1, 1),
(12, 11, '成绩录入', '/teacher/scores',    'teacher/ScoreEntry',   'DataLine',   'M', '', 1, 1, 1),
(13, 11, '题库管理', '/teacher/questions', 'teacher/QuestionBank', 'Collection', 'M', '', 2, 1, 1),

-- 教务菜单
(20, 0,  '工作台',   '/academic',                'academic/Dashboard',          'Monitor',  'M', '', 1, 1, 1),
(21, 0,  '选课管理', '',                         '',                            'List',     'C', '', 2, 1, 1),
(22, 21, '选课轮次', '/academic/rounds',         'academic/RoundConfig',        'Clock',    'M', '', 1, 1, 1),
(23, 21, '选课监控', '/academic/enroll-monitor', 'academic/EnrollmentMonitor',  'View',     'M', '', 2, 1, 1),
(24, 0,  '教学管理', '',                         '',                            'Notebook', 'C', '', 3, 1, 1),
(25, 24, '排课管理', '/academic/schedules',      'academic/ScheduleManagement', 'Calendar', 'M', '', 1, 1, 1),
(26, 24, '考试管理', '/academic/exams',          'academic/ExamManagement',     'Timer',    'M', '', 2, 1, 1),
(27, 24, '成绩审核', '/academic/scores',         'academic/ScoreApproval',      'DataLine', 'M', '', 3, 1, 1),
(28, 24, '教学评价', '/academic/evaluation',     'academic/TeachingEvaluation', 'Star',     'M', '', 4, 1, 1),
(29, 0,  '培养管理', '',                         '',                            'Files',    'C', '', 4, 1, 1),
(30, 29, '培养方案', '/academic/training-plan',  'academic/TrainingPlan',       'Document', 'M', '', 1, 1, 1),
(31, 29, '新生导入', '/academic/new-student',    'academic/NewStudentImport',   'Upload',   'M', '', 2, 1, 1),
(32, 0,  '课程审核', '/approvals',               'admin/CourseApproval',        'Checked',  'M', '', 5, 1, 1),

-- 管理员菜单
(40, 0,  '工作台',   '/admin',       'admin/Dashboard',      'Monitor',    'M', '', 1, 1, 1),
(41, 0,  '系统管理', '',             '',                     'Setting',    'C', '', 2, 1, 1),
(42, 41, '用户管理', '/admin/users', 'admin/UserManagement', 'UserFilled', 'M', '', 1, 1, 1),
(43, 41, '角色管理', '/admin/roles', 'admin/RoleManagement', 'Avatar',     'M', '', 2, 1, 1),
(44, 0,  '课程审核', '/approvals',   'admin/CourseApproval', 'Checked',    'M', '', 3, 1, 1),

-- 题库管理员菜单
(50, 0, '工作台',   '/qb-admin',          'qb-admin/Dashboard',     'Monitor',    'M', '', 1, 1, 1),
(51, 0, '题库管理', '/teacher/questions', 'teacher/QuestionBank',   'Collection', 'M', '', 2, 1, 1),
(52, 0, '题库审核', '/qb-admin/audit',    'qb-admin/QuestionAudit', 'Checked',    'M', '', 3, 1, 1)
ON DUPLICATE KEY UPDATE
  parent_id = VALUES(parent_id),
  menu_name = VALUES(menu_name),
  path = VALUES(path),
  component = VALUES(component),
  icon = VALUES(icon),
  menu_type = VALUES(menu_type),
  permission = VALUES(permission),
  sort_order = VALUES(sort_order),
  visible = VALUES(visible),
  status = VALUES(status);

-- ===== 3. 角色菜单绑定：非破坏式补齐 =====
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES
-- 学生
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),
-- 教师
(2,10),(2,11),(2,12),(2,13),
-- 教务
(3,20),(3,21),(3,22),(3,23),(3,24),(3,25),(3,26),(3,27),(3,28),(3,29),(3,30),(3,31),(3,32),
-- 管理员
(4,40),(4,41),(4,42),(4,43),(4,44),
-- 题库管理员
(5,50),(5,51),(5,52);

COMMIT;

-- ===== 4. 执行后校验 =====
SELECT
  r.id,
  r.role_code,
  r.role_name,
  COUNT(rm.menu_id) AS menu_count,
  GROUP_CONCAT(CONCAT(m.id, ':', m.menu_name, '(', IFNULL(NULLIF(m.path, ''), '目录'), ')') ORDER BY m.id SEPARATOR ' | ') AS menus
FROM sys_role r
LEFT JOIN sys_role_menu rm ON rm.role_id = r.id
LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE r.id IN (1, 2, 3, 4, 5)
GROUP BY r.id, r.role_code, r.role_name
ORDER BY r.id;

-- 检查是否存在指向不存在菜单/角色的绑定，两个结果都应为 0。
SELECT COUNT(*) AS missing_menu_bindings
FROM sys_role_menu rm
LEFT JOIN sys_menu m ON m.id = rm.menu_id
WHERE m.id IS NULL;

SELECT COUNT(*) AS missing_role_bindings
FROM sys_role_menu rm
LEFT JOIN sys_role r ON r.id = rm.role_id
WHERE r.id IS NULL;

-- ============================================================
-- OPTIONAL：精确重置 5 个内置角色的菜单绑定
--
-- 默认不建议执行。仅当你确认要把 STUDENT/TEACHER/ACADEMIC/
-- ADMIN/QB_ADMIN 的菜单绑定重置为本文定义的标准集合时使用。
--
-- 执行方式：
-- 1. 先备份 sys_role_menu。
-- 2. 删除下面代码块的注释。
-- 3. 执行后跑上面的校验查询。
--
-- START TRANSACTION;
-- DELETE FROM sys_role_menu WHERE role_id IN (1, 2, 3, 4, 5);
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES
-- (1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7),
-- (2,10),(2,11),(2,12),(2,13),
-- (3,20),(3,21),(3,22),(3,23),(3,24),(3,25),(3,26),(3,27),(3,28),(3,29),(3,30),(3,31),(3,32),
-- (4,40),(4,41),(4,42),(4,43),(4,44),
-- (5,50),(5,51),(5,52);
-- COMMIT;
-- ============================================================
