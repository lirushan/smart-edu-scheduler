-- 修复 V1.0 种子数据中文乱码
-- 请使用 UTF-8 连接执行；推荐命令：
-- docker exec -i smart-edu-mysql mysql --default-character-set=utf8mb4 -uroot -pSmartEdu2024! smart_edu < outputs/repair-seed-data-utf8.sql

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

-- 用户
UPDATE sys_user SET real_name='系统管理员', department='信息化中心', major='', grade='' WHERE username='admin';
UPDATE sys_user SET real_name='李教务', department='教务处', major='', grade='' WHERE username='academic01';
UPDATE sys_user SET real_name='赵教授', department='计算机学院', major='', grade='' WHERE username='teacher01';
UPDATE sys_user SET real_name='钱副教授', department='数学学院', major='', grade='' WHERE username='teacher02';
UPDATE sys_user SET real_name='孙讲师', department='外语学院', major='', grade='' WHERE username='teacher03';
UPDATE sys_user SET real_name='张明远', department='计算机学院', major='计算机科学与技术', grade='2024级' WHERE username='student01';
UPDATE sys_user SET real_name='王丽华', department='计算机学院', major='软件工程', grade='2024级' WHERE username='student02';
UPDATE sys_user SET real_name='陈小刚', department='数学学院', major='应用数学', grade='2023级' WHERE username='student03';
UPDATE sys_user SET real_name='刘思雨', department='外语学院', major='英语', grade='2024级' WHERE username='student04';
UPDATE sys_user SET real_name='周题库', department='信息化中心', major='', grade='' WHERE username='qbadmin01';

-- 角色
UPDATE sys_role SET role_name='学生', description='学生角色' WHERE role_code='STUDENT';
UPDATE sys_role SET role_name='教师', description='教师角色' WHERE role_code='TEACHER';
UPDATE sys_role SET role_name='教务', description='教务管理人员' WHERE role_code='ACADEMIC';
UPDATE sys_role SET role_name='管理员', description='系统管理员' WHERE role_code='ADMIN';
UPDATE sys_role SET role_name='题库管理员', description='题库审核与管理' WHERE role_code='QB_ADMIN';

-- 菜单
UPDATE sys_menu SET menu_name='工作台' WHERE id IN (1,10,20,40,50);
UPDATE sys_menu SET menu_name='课程广场' WHERE id=2;
UPDATE sys_menu SET menu_name='我的课表' WHERE id=3;
UPDATE sys_menu SET menu_name='我的选课' WHERE id=4;
UPDATE sys_menu SET menu_name='学习中心' WHERE id=5;
UPDATE sys_menu SET menu_name='我的成绩' WHERE id=6;
UPDATE sys_menu SET menu_name='考试中心' WHERE id=7;
UPDATE sys_menu SET menu_name='教学管理' WHERE id IN (11,24);
UPDATE sys_menu SET menu_name='成绩录入' WHERE id=12;
UPDATE sys_menu SET menu_name='题库管理' WHERE id IN (13,51);
UPDATE sys_menu SET menu_name='选课管理' WHERE id=21;
UPDATE sys_menu SET menu_name='选课轮次' WHERE id=22;
UPDATE sys_menu SET menu_name='选课监控' WHERE id=23;
UPDATE sys_menu SET menu_name='排课管理' WHERE id=25;
UPDATE sys_menu SET menu_name='考试管理' WHERE id=26;
UPDATE sys_menu SET menu_name='成绩审核' WHERE id=27;
UPDATE sys_menu SET menu_name='教学评价' WHERE id=28;
UPDATE sys_menu SET menu_name='培养管理' WHERE id=29;
UPDATE sys_menu SET menu_name='培养方案' WHERE id=30;
UPDATE sys_menu SET menu_name='新生导入' WHERE id=31;
UPDATE sys_menu SET menu_name='课程审核' WHERE id IN (32,44);
UPDATE sys_menu SET menu_name='系统管理' WHERE id=41;
UPDATE sys_menu SET menu_name='用户管理' WHERE id=42;
UPDATE sys_menu SET menu_name='角色管理' WHERE id=43;
UPDATE sys_menu SET menu_name='题库审核' WHERE id=52;

-- 课程
UPDATE crs_course SET course_name='计算机组成原理', description='计算机硬件系统的基本组成和工作原理', category='计算机科学' WHERE course_code='CS101';
UPDATE crs_course SET course_name='数据结构与算法', description='常用数据结构与基本算法的设计与分析', category='计算机科学' WHERE course_code='CS102';
UPDATE crs_course SET course_name='操作系统', description='操作系统的基本原理与实现技术', category='计算机科学' WHERE course_code='CS103';
UPDATE crs_course SET course_name='高等数学A(下)', description='多元函数微积分、级数与常微分方程', category='数学' WHERE course_code='MATH201';
UPDATE crs_course SET course_name='线性代数', description='向量空间、矩阵理论与线性变换', category='数学' WHERE course_code='MATH202';
UPDATE crs_course SET course_name='大学英语(三)', description='学术英语阅读与写作', category='外语' WHERE course_code='ENG301';
UPDATE crs_course SET course_name='英语口语', description='英语口语表达与交际训练', category='外语' WHERE course_code='ENG302';
UPDATE crs_course SET course_name='大学体育(一)', description='体能训练与基础运动技能', category='体育' WHERE course_code='PE101';

-- 开课地点
UPDATE crs_offering SET location='教三楼301' WHERE id IN (1,9);
UPDATE crs_offering SET location='教三楼302' WHERE id IN (2,10);
UPDATE crs_offering SET location='教三楼303' WHERE id=3;
UPDATE crs_offering SET location='教四楼201' WHERE id=4;
UPDATE crs_offering SET location='教四楼202' WHERE id=5;
UPDATE crs_offering SET location='教五楼101' WHERE id=6;
UPDATE crs_offering SET location='教五楼102' WHERE id=7;
UPDATE crs_offering SET location='体育馆' WHERE id=8;

-- 轮次、成绩
UPDATE reg_round SET round_name='2024-2025-1 第一轮选课', target_grades='["2024级","2023级","2022级"]' WHERE id=1;
UPDATE reg_score SET grade_level='优秀' WHERE id=1;
UPDATE reg_score SET grade_level='良好' WHERE id=2;
UPDATE reg_score SET grade_level='中等' WHERE id=3;

-- 试题与考试
UPDATE exam_question SET content='CPU的中文全称是什么？', options='["A. 中央处理器","B. 图形处理器","C. 数字信号处理器","D. 网络处理器"]', answer='A', analysis='CPU即Central Processing Unit', knowledge_point='计算机组成' WHERE id=1;
UPDATE exam_question SET content='操作系统的主要功能不包括？', options='["A. 进程管理","B. 内存管理","C. 数据压缩","D. 文件管理"]', answer='C', analysis='OS核心功能包括进程/内存/文件/设备管理', knowledge_point='操作系统' WHERE id=2;
UPDATE exam_question SET content='栈是一种先进先出的数据结构。', options=NULL, answer='B', analysis='栈是后进先出(LIFO)，队列才是先进先出(FIFO)', knowledge_point='数据结构' WHERE id=3;
UPDATE exam_question SET content='冯·诺依曼计算机体系结构的五大部件是：_____、_____、_____、_____、_____。', options=NULL, answer='运算器,控制器,存储器,输入设备,输出设备', analysis='冯·诺依曼体系结构核心组成', knowledge_point='计算机组成' WHERE id=4;
UPDATE exam_exam SET exam_name='计算机组成原理 期中考试' WHERE id=1;
UPDATE exam_paper SET paper_title='计算机组成原理 期中试卷A' WHERE id=1;
