-- ============================================================
-- 智教通 V1.0 — 全量建表 DDL + 种子数据
-- 数据库：smart_edu
-- 编码：utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS smart_edu
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE smart_edu;

-- ============================================================
-- 系统模块 (sys_)
-- ============================================================

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(256) NOT NULL,
    real_name VARCHAR(64) DEFAULT '',
    user_type TINYINT NOT NULL COMMENT '1=学生 2=教师 3=教务 4=管理员 5=题库管理员',
    department VARCHAR(128) DEFAULT '',
    major VARCHAR(128) DEFAULT '',
    grade VARCHAR(32) DEFAULT '' COMMENT '年级，如2024级',
    phone VARCHAR(32) DEFAULT '',
    email VARCHAR(128) DEFAULT '',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=禁用 1=正常 2=锁定',
    login_fail_count INT DEFAULT 0,
    lock_until DATETIME NULL,
    last_login_time DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0=正常 1=删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    INDEX idx_user_type_status (user_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(64) NOT NULL,
    description VARCHAR(256) DEFAULT '',
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0,
    menu_name VARCHAR(64) NOT NULL,
    path VARCHAR(128) DEFAULT '',
    component VARCHAR(128) DEFAULT '',
    icon VARCHAR(64) DEFAULT '',
    menu_type CHAR(1) NOT NULL DEFAULT 'M' COMMENT 'M=目录 C=菜单 B=按钮',
    permission VARCHAR(128) DEFAULT '' COMMENT '权限标识，如 student:enroll',
    sort_order INT DEFAULT 0,
    visible TINYINT NOT NULL DEFAULT 1,
    status TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联';

-- ============================================================
-- 课程模块 (crs_)
-- ============================================================

-- 课程库
DROP TABLE IF EXISTS crs_course;
CREATE TABLE crs_course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(32) NOT NULL,
    course_name VARCHAR(128) NOT NULL,
    credit DECIMAL(3,1) NOT NULL DEFAULT 0.0,
    description TEXT,
    category VARCHAR(64) DEFAULT '' COMMENT '课程分类',
    syllabus TEXT COMMENT '教学大纲',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_code (course_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程库';

-- 开课实例
DROP TABLE IF EXISTS crs_offering;
CREATE TABLE crs_offering (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    semester VARCHAR(32) NOT NULL COMMENT '学期，如2024-2025-1',
    weekday TINYINT NOT NULL COMMENT '1-7 周一至周日',
    period_start TINYINT NOT NULL COMMENT '开始节次 1-8',
    period_end TINYINT NOT NULL COMMENT '结束节次 1-8',
    location VARCHAR(128) DEFAULT '',
    capacity INT NOT NULL DEFAULT 0,
    enrolled_count INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=待审 1=通过 2=驳回',
    audit_comment VARCHAR(512) DEFAULT '',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status_semester (status, semester),
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开课实例';

-- ============================================================
-- 选课模块 (reg_)
-- ============================================================

-- 选课轮次
DROP TABLE IF EXISTS reg_round;
CREATE TABLE reg_round (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    round_name VARCHAR(128) NOT NULL,
    semester VARCHAR(32) NOT NULL DEFAULT '',
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_credits INT DEFAULT 30 COMMENT '最大可选学分',
    max_courses INT DEFAULT 10 COMMENT '最大可选门数',
    target_grades JSON COMMENT '目标年级列表，如["2024级","2023级"]',
    age_min INT DEFAULT 0,
    age_max INT DEFAULT 99,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已结束',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课轮次';

-- 选课记录
DROP TABLE IF EXISTS reg_enrollment;
CREATE TABLE reg_enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    round_id BIGINT DEFAULT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常 1=已退 2=待审核',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dropped_at DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_student_offering (student_id, offering_id),
    INDEX idx_student_status (student_id, status),
    INDEX idx_offering_id (offering_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课记录';

-- 成绩记录
DROP TABLE IF EXISTS reg_score;
CREATE TABLE reg_score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    raw_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '原始百分制分数',
    grade_level VARCHAR(16) COMMENT '五级制：优秀/良好/中等/及格/不及格',
    gpa DECIMAL(3,1) DEFAULT 0.0,
    rank_in_class INT DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布',
    entered_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_offering_score (student_id, offering_id),
    INDEX idx_offering_id (offering_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩记录';

-- ============================================================
-- 考试模块 (exam_)
-- ============================================================

-- 考试定义
DROP TABLE IF EXISTS exam_exam;
CREATE TABLE exam_exam (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    offering_id BIGINT NOT NULL,
    exam_name VARCHAR(128) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    duration_minutes INT NOT NULL DEFAULT 120,
    total_score INT NOT NULL DEFAULT 100,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已结束',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_offering_id (offering_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试定义';

-- 试卷结构
DROP TABLE IF EXISTS exam_paper;
CREATE TABLE exam_paper (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    paper_title VARCHAR(128) NOT NULL,
    question_count INT DEFAULT 0,
    total_score INT NOT NULL DEFAULT 100,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exam_id (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷结构';

-- 试题
DROP TABLE IF EXISTS exam_question;
CREATE TABLE exam_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_type TINYINT NOT NULL COMMENT '1=单选 2=多选 3=判断 4=填空',
    content TEXT NOT NULL,
    options JSON COMMENT '选项，如["A.xxx","B.xxx"]',
    answer TEXT NOT NULL COMMENT '正确答案',
    analysis TEXT COMMENT '解析',
    difficulty TINYINT NOT NULL DEFAULT 3 COMMENT '难度 1-5',
    knowledge_point VARCHAR(128) DEFAULT '',
    created_by BIGINT NOT NULL,
    scope TINYINT NOT NULL DEFAULT 2 COMMENT '1=全局 2=个人',
    audit_status TINYINT NOT NULL DEFAULT 1 COMMENT '0=待审 1=通过 2=驳回',
    auditor_id BIGINT DEFAULT NULL,
    audit_time DATETIME NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_qtype_scope_audit (question_type, scope, audit_status),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题库';

-- 试卷-试题关联
DROP TABLE IF EXISTS exam_paper_question;
CREATE TABLE exam_paper_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    paper_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    question_order INT DEFAULT 0,
    score INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_paper_question (paper_id, question_id),
    INDEX idx_paper_id (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷试题关联';

-- 考试记录
DROP TABLE IF EXISTS exam_record;
CREATE TABLE exam_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    start_time DATETIME NULL,
    submit_time DATETIME NULL,
    answers JSON COMMENT '学生答案',
    objective_score DECIMAL(5,2) DEFAULT 0.00,
    total_score DECIMAL(5,2) DEFAULT 0.00,
    ai_feedback JSON COMMENT 'AI评分反馈',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=未开始 1=进行中 2=已交卷',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_student (exam_id, student_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试记录';

-- ============================================================
-- 种子数据
-- ============================================================

-- 密码均为 BCrypt("password123") → $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh
-- 简化种子数据用占位哈希
SET @bcrypt_pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh';

-- ===== 系统用户 =====
INSERT INTO sys_user (id, username, password, real_name, user_type, department, major, grade, status) VALUES
(1,  'admin',         @bcrypt_pwd, '系统管理员',   4, '信息化中心',     '',        '',      1),
(2,  'academic01',    @bcrypt_pwd, '李教务',       3, '教务处',         '',        '',      1),
(3,  'teacher01',     @bcrypt_pwd, '赵教授',       2, '计算机学院',     '',        '',      1),
(4,  'teacher02',     @bcrypt_pwd, '钱副教授',     2, '数学学院',       '',        '',      1),
(5,  'teacher03',     @bcrypt_pwd, '孙讲师',       2, '外语学院',       '',        '',      1),
(6,  'student01',     @bcrypt_pwd, '张明远',       1, '计算机学院',     '计算机科学与技术', '2024级', 1),
(7,  'student02',     @bcrypt_pwd, '王丽华',       1, '计算机学院',     '软件工程',         '2024级', 1),
(8,  'student03',     @bcrypt_pwd, '陈小刚',       1, '数学学院',       '应用数学',         '2023级', 1),
(9,  'student04',     @bcrypt_pwd, '刘思雨',       1, '外语学院',       '英语',             '2024级', 1),
(10, 'qbadmin01',     @bcrypt_pwd, '周题库',       5, '信息化中心',     '',        '',      1);

-- ===== 角色 =====
INSERT INTO sys_role (id, role_code, role_name, description) VALUES
(1, 'STUDENT',        '学生',        '学生角色'),
(2, 'TEACHER',        '教师',        '教师角色'),
(3, 'ACADEMIC',       '教务',        '教务管理人员'),
(4, 'ADMIN',          '管理员',      '系统管理员'),
(5, 'QB_ADMIN',       '题库管理员',  '题库审核与管理');

-- ===== 用户-角色关联 =====
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1,4), (2,3), (3,2), (4,2), (5,2),
(6,1), (7,1), (8,1), (9,1), (10,5);

-- ===== 菜单 =====
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort_order) VALUES
-- 学生菜单
(1,  0, '工作台',     '/dashboard',         'student/Dashboard',    'Monitor',    'M', '', 1),
(2,  0, '课程广场',   '/courses',           'student/CourseMarket', 'Reading',    'M', '', 2),
(3,  0, '我的课表',   '/schedule',          'student/MySchedule',   'Calendar',   'M', '', 3),
(4,  0, '我的选课',   '/enrollments',       'student/MyEnrollments','List',       'M', '', 4),
(5,  0, '学习中心',   '',                   '',                     'Notebook',   'C', '', 5),
(6,  5, '我的成绩',   '/scores',            'student/MyScores',     'DataLine',   'M', '', 1),
(7,  5, '考试中心',   '/exams',             'student/ExamCenter',   'Timer',      'M', '', 2),

-- 教师菜单
(10, 0, '工作台',     '/teacher',           'teacher/Dashboard',    'Monitor',    'M', '', 1),
(11, 0, '教学管理',   '',                   '',                     'Notebook',   'C', '', 2),
(12, 11,'成绩录入',   '/teacher/scores',    'teacher/ScoreEntry',   'DataLine',   'M', '', 1),
(13, 11,'题库管理',   '/teacher/questions', 'teacher/QuestionBank', 'Collection', 'M', '', 2),

-- 教务菜单
(20, 0, '工作台',     '/academic',          'academic/Dashboard',   'Monitor',    'M', '', 1),
(21, 0, '选课管理',   '',                   '',                     'List',       'C', '', 2),
(22, 21,'选课轮次',   '/academic/rounds',   'academic/RoundConfig', 'Clock',      'M', '', 1),
(23, 21,'选课监控',   '/academic/enroll-monitor', 'academic/EnrollmentMonitor', 'View', 'M', '', 2),
(24, 0, '教学管理',   '',                   '',                     'Notebook',   'C', '', 3),
(25, 24,'排课管理',   '/academic/schedules',  'academic/ScheduleManagement', 'Calendar', 'M', '', 1),
(26, 24,'考试管理',   '/academic/exams',      'academic/ExamManagement',      'Timer',    'M', '', 2),
(27, 24,'成绩审核',   '/academic/scores',     'academic/ScoreApproval',       'DataLine', 'M', '', 3),
(28, 24,'教学评价',   '/academic/evaluation', 'academic/TeachingEvaluation',  'Star',     'M', '', 4),
(29, 0, '培养管理',   '',                   '',                     'Files',      'C', '', 4),
(30, 29,'培养方案',   '/academic/training-plan', 'academic/TrainingPlan',      'Document', 'M', '', 1),
(31, 29,'新生导入',   '/academic/new-student',   'academic/NewStudentImport',  'Upload',   'M', '', 2),
(32, 0, '课程审核',   '/approvals',           'admin/CourseApproval', 'Checked',    'M', '', 5),

-- 管理员菜单
(40, 0, '工作台',     '/admin',             'admin/Dashboard',        'Monitor',  'M', '', 1),
(41, 0, '系统管理',   '',                   '',                       'Setting',  'C', '', 2),
(42, 41,'用户管理',   '/admin/users',       'admin/UserManagement',   'UserFilled','M', '', 1),
(43, 41,'角色管理',   '/admin/roles',       'admin/RoleManagement',   'Avatar',   'M', '', 2),
(44, 0, '课程审核',   '/approvals',         'admin/CourseApproval',   'Checked',  'M', '', 3),

-- 题库管理员菜单
(50, 0, '工作台',     '/qb-admin',          'qb-admin/Dashboard',     'Monitor',  'M', '', 1),
(51, 0, '题库管理',   '/teacher/questions', 'teacher/QuestionBank',   'Collection','M', '', 2),
(52, 0, '题库审核',   '/qb-admin/audit',    'qb-admin/QuestionAudit', 'Checked',  'M', '', 3);

-- ===== 角色-菜单关联 =====
-- 学生 (role_id=1): 菜单 1-7
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1,1),(1,2),(1,3),(1,4),(1,5),(1,6),(1,7);

-- 教师 (role_id=2): 菜单 10-13
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2,10),(2,11),(2,12),(2,13);

-- 教务 (role_id=3): 菜单 20-32
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3,20),(3,21),(3,22),(3,23),(3,24),(3,25),(3,26),(3,27),(3,28),(3,29),(3,30),(3,31),(3,32);

-- 管理员 (role_id=4): 菜单 40-44
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4,40),(4,41),(4,42),(4,43),(4,44);

-- 题库管理员 (role_id=5): 菜单 50-52
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(5,50),(5,51),(5,52);

-- ===== 课程库 =====
INSERT INTO crs_course (id, course_code, course_name, credit, description, category) VALUES
(1, 'CS101', '计算机组成原理',   4.0, '计算机硬件系统的基本组成和工作原理',    '计算机科学'),
(2, 'CS102', '数据结构与算法',   4.0, '常用数据结构与基本算法的设计与分析',    '计算机科学'),
(3, 'CS103', '操作系统',         3.5, '操作系统的基本原理与实现技术',          '计算机科学'),
(4, 'MATH201', '高等数学A(下)',  5.0, '多元函数微积分、级数与常微分方程',      '数学'),
(5, 'MATH202', '线性代数',       3.5, '向量空间、矩阵理论与线性变换',          '数学'),
(6, 'ENG301', '大学英语(三)',    3.0, '学术英语阅读与写作',                    '外语'),
(7, 'ENG302', '英语口语',        2.0, '英语口语表达与交际训练',                '外语'),
(8, 'PE101',  '大学体育(一)',    1.5, '体能训练与基础运动技能',                '体育');

-- ===== 开课实例（已审核通过） =====
INSERT INTO crs_offering (id, course_id, teacher_id, semester, weekday, period_start, period_end, location, capacity, enrolled_count, status) VALUES
(1, 1, 3, '2024-2025-1', 1, 1, 2, '教三楼301', 60, 12, 1),
(2, 2, 3, '2024-2025-1', 2, 3, 4, '教三楼302', 50, 8,  1),
(3, 3, 3, '2024-2025-1', 3, 1, 2, '教三楼303', 50, 5,  1),
(4, 4, 4, '2024-2025-1', 1, 3, 4, '教四楼201', 80, 20, 1),
(5, 5, 4, '2024-2025-1', 2, 1, 2, '教四楼202', 70, 15, 1),
(6, 6, 5, '2024-2025-1', 3, 3, 4, '教五楼101', 40, 10, 1),
(7, 7, 5, '2024-2025-1', 4, 5, 6, '教五楼102', 30, 6,  1),
(8, 8, 5, '2024-2025-1', 4, 7, 8, '体育馆',    40, 18, 1),
-- 待审课程
(9, 1, 3, '2024-2025-2', 1, 1, 2, '教三楼301', 60, 0, 0),
(10, 2, 3,'2024-2025-2', 2, 3, 4, '教三楼302', 50, 0, 0);

-- ===== 选课轮次 =====
INSERT INTO reg_round (id, round_name, semester, start_time, end_time, max_credits, max_courses, target_grades, age_min, age_max, status) VALUES
(1, '2024-2025-1 第一轮选课', '2024-2025-1', '2024-09-01 08:00:00', '2024-09-15 23:59:59', 30, 8,
    '["2024级","2023级","2022级"]', 16, 30, 1);

-- ===== 选课记录 =====
INSERT INTO reg_enrollment (id, student_id, offering_id, round_id, status) VALUES
(1, 6, 1, 1, 0),
(2, 6, 4, 1, 0),
(3, 7, 2, 1, 0),
(4, 7, 5, 1, 0),
(5, 8, 4, 1, 0),
(6, 9, 6, 1, 0);

-- ===== 成绩记录 =====
INSERT INTO reg_score (id, student_id, offering_id, raw_score, grade_level, gpa, status, entered_by) VALUES
(1, 6, 1, 92.0, '优秀', 4.0, 1, 3),
(2, 6, 4, 85.5, '良好', 3.0, 1, 4),
(3, 7, 2, 78.0, '中等', 2.0, 1, 3);

-- ===== 试题 =====
INSERT INTO exam_question (id, question_type, content, options, answer, analysis, difficulty, knowledge_point, created_by, scope, audit_status) VALUES
(1, 1, 'CPU的中文全称是什么？', '["A. 中央处理器","B. 图形处理器","C. 数字信号处理器","D. 网络处理器"]', 'A', 'CPU即Central Processing Unit', 1, '计算机组成', 3, 2, 1),
(2, 1, '操作系统的主要功能不包括？', '["A. 进程管理","B. 内存管理","C. 数据压缩","D. 文件管理"]', 'C', 'OS核心功能包括进程/内存/文件/设备管理', 2, '操作系统', 3, 2, 1),
(3, 3, '栈是一种先进先出的数据结构。', NULL, 'B', '栈是后进先出(LIFO)，队列才是先进先出(FIFO)', 2, '数据结构', 3, 2, 1),
(4, 4, '冯·诺依曼计算机体系结构的五大部件是：_____、_____、_____、_____、_____。', NULL, '运算器,控制器,存储器,输入设备,输出设备', '冯·诺依曼体系结构核心组成', 3, '计算机组成', 3, 1, 1);

-- ===== 考试 =====
INSERT INTO exam_exam (id, offering_id, exam_name, start_time, end_time, duration_minutes, total_score, status) VALUES
(1, 1, '计算机组成原理 期中考试', '2024-11-15 09:00:00', '2024-11-15 11:00:00', 120, 100, 0);

-- ===== 试卷 =====
INSERT INTO exam_paper (id, exam_id, paper_title, question_count, total_score) VALUES
(1, 1, '计算机组成原理 期中试卷A', 4, 100);

-- ===== 试卷-试题关联 =====
INSERT INTO exam_paper_question (paper_id, question_id, question_order, score) VALUES
(1, 1, 1, 25),
(1, 2, 2, 25),
(1, 3, 3, 20),
(1, 4, 4, 30);
