-- ============================================================
-- 智教通 V1.1 — 教务端新表
-- ============================================================

USE smart_edu;

-- 教学评价表
DROP TABLE IF EXISTS sys_evaluation;
CREATE TABLE sys_evaluation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    offering_id BIGINT NOT NULL,
    score_1 TINYINT NOT NULL DEFAULT 5 COMMENT '教学质量 1-5',
    score_2 TINYINT NOT NULL DEFAULT 5 COMMENT '课程内容 1-5',
    score_3 TINYINT NOT NULL DEFAULT 5 COMMENT '课堂氛围 1-5',
    score_4 TINYINT NOT NULL DEFAULT 5 COMMENT '师生互动 1-5',
    score_5 TINYINT NOT NULL DEFAULT 5 COMMENT '综合评价 1-5',
    comment TEXT COMMENT '文字评价',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已提交',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher_id (teacher_id),
    INDEX idx_offering_id (offering_id),
    INDEX idx_student_id (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学评价';

-- 培养方案表
DROP TABLE IF EXISTS sys_training_plan;
CREATE TABLE sys_training_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    major VARCHAR(128) NOT NULL COMMENT '专业名称',
    grade VARCHAR(32) NOT NULL COMMENT '年级，如2024级',
    total_credits DECIMAL(5,1) NOT NULL DEFAULT 0.0 COMMENT '总学分要求',
    required_credits DECIMAL(5,1) NOT NULL DEFAULT 0.0 COMMENT '必修学分',
    elective_credits DECIMAL(5,1) NOT NULL DEFAULT 0.0 COMMENT '选修学分',
    description TEXT COMMENT '方案描述',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '0=停用 1=启用',
    deleted TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_grade (major, grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='培养方案';

-- 种子数据：教学评价
INSERT INTO sys_evaluation (id, student_id, teacher_id, offering_id, score_1, score_2, score_3, score_4, score_5, comment, status) VALUES
(1, 6, 3, 1, 5, 4, 5, 4, 5, '赵教授讲课非常清晰，受益匪浅', 1),
(2, 7, 3, 2, 4, 5, 4, 5, 4, '课程内容有深度，作业量适中', 1),
(3, 6, 4, 4, 5, 5, 4, 4, 5, '钱老师的高等数学深入浅出', 1),
(4, 8, 4, 4, 3, 4, 3, 4, 4, '希望能增加互动环节', 1),
(5, 9, 5, 6, 4, 4, 5, 4, 4, '英语课堂氛围很好', 1);

-- 种子数据：培养方案
INSERT INTO sys_training_plan (id, major, grade, total_credits, required_credits, elective_credits, description, status) VALUES
(1, '计算机科学与技术', '2024级', 170.0, 120.0, 50.0, '计算机科学与技术专业2024级培养方案，涵盖计算机硬件、软件、网络等核心课程', 1),
(2, '软件工程', '2024级', 168.0, 118.0, 50.0, '软件工程专业2024级培养方案，注重软件开发实践能力', 1),
(3, '应用数学', '2023级', 160.0, 110.0, 50.0, '应用数学专业2023级培养方案', 1),
(4, '英语', '2024级', 155.0, 105.0, 50.0, '英语专业2024级培养方案，注重语言应用与跨文化交际能力', 1);
