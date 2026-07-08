-- ============================================
-- Data: education — 教育模块种子数据
-- ============================================

-- 学科
INSERT IGNORE INTO `subject` (`id`, `code`, `name`, `grade_level`, `icon`, `sort_order`, `status`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 'MATH',     '数学',   'K3', 'i-mdi:math-compass',        1, 1, 1, 0, 'system', 'system'),
(2, 'PHYSICS',  '物理',   'K3', 'i-mdi:physics',            2, 1, 1, 0, 'system', 'system'),
(3, 'ENGLISH',  '英语',   'K3', 'i-mdi:alphabetical',       3, 1, 1, 0, 'system', 'system'),
(4, 'CHINESE',  '语文',   'K3', 'i-mdi:book-open-page-variant', 4, 1, 1, 0, 'system', 'system'),
(5, 'CHEMISTRY','化学',   'K3', 'i-mdi:flask',              5, 1, 1, 0, 'system', 'system');

-- 教材
INSERT IGNORE INTO `textbook` (tenant_id, workspace_id, `id`, `name`, `subject_code`, `grade`, `publisher`, `isbn`, `create_by`, `update_by`) VALUES
(1, 0, 1, '人教版数学七年级上册',   'MATH',    7, '人民教育出版社', '978-7-107-12345-6', 'system', 'system'),
(1, 0, 2, '人教版数学七年级下册',   'MATH',    7, '人民教育出版社', '978-7-107-12346-3', 'system', 'system'),
(1, 0, 3, '北师大版数学七年级上册', 'MATH',    7, '北京师范大学出版社', '978-7-303-12345-7', 'system', 'system'),
(1, 0, 4, '人教版物理八年级上册',   'PHYSICS', 8, '人民教育出版社', '978-7-107-23456-7', 'system', 'system'),
(1, 0, 5, '人教版英语七年级上册',   'ENGLISH', 7, '人民教育出版社', '978-7-107-34567-8', 'system', 'system'),
(1, 0, 6, '人教版化学九年级上册',   'CHEMISTRY',9, '人民教育出版社', '978-7-107-45678-9', 'system', 'system');

-- 章节（教材1：人教版数学七年级上册）
INSERT IGNORE INTO `chapter` (tenant_id, workspace_id, `id`, `textbook_id`, `parent_id`, `name`, `chapter_order`, `create_by`, `update_by`) VALUES
(1, 0, 1, 1, NULL, '第一章 有理数', 1, 'system', 'system'),
(1, 0, 2, 1, 1, '1.1 正数和负数', 1, 'system', 'system'),
(1, 0, 3, 1, 1, '1.2 有理数',   2, 'system', 'system'),
(1, 0, 4, 1, 1, '1.3 有理数的加减法', 3, 'system', 'system'),
(1, 0, 5, 1, NULL, '第二章 整式的加减', 2, 'system', 'system'),
(1, 0, 6, 1, 5, '2.1 整式', 1, 'system', 'system'),
(1, 0, 7, 1, 5, '2.2 整式的加减', 2, 'system', 'system');

-- 知识点-教材关联
INSERT IGNORE INTO `knowledge_textbook` (`knowledge_id`, `textbook_id`, `chapter_id`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 1, 3, 1, 0, 'system', 'system'),
(2, 1, 3, 1, 0, 'system', 'system'),
(3, 1, 3, 1, 0, 'system', 'system'),
(4, 1, 3, 1, 0, 'system', 'system'),
(5, 1, 3, 1, 0, 'system', 'system'),
(6, 1, 3, 1, 0, 'system', 'system');

-- 学生
INSERT IGNORE INTO `student` (`id`, `user_id`, `student_no`, `name`, `gender`, `grade`, `grade_level`, `school`, `class_name`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 4, 'STU2024001', '王小明', 1, 7, 'K3', '阳光中学', '七年级一班', 1, 0, 'system', 'system');

-- 教师
INSERT IGNORE INTO `teacher` (`id`, `user_id`, `teacher_no`, `name`, `subject`, `school`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 3, 'TCH2024001', '张老师', 'MATH', '阳光中学', 1, 0, 'system', 'system');

-- 题目
INSERT IGNORE INTO `edu_question` (`id`, `code`, `type`, `subject_code`, `grade`, `difficulty`, `ability_dimension`, `title`, `options`, `answer`, `analysis`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 'MATH-7-001', 'CHOICE', 'MATH', 7, 1, 'understand', '|-5| 等于多少？',
 '["A. -5", "B. 0", "C. 5", "D. 10"]', 'C', '绝对值表示一个数到原点的距离，|-5| = 5，故选 C', 1, 0, 'system', 'system'),
(2, 'MATH-7-002', 'CHOICE', 'MATH', 7, 2, 'apply', '若 |x| = 3，则 x 的值为？',
 '["A. 3", "B. -3", "C. ±3", "D. 0"]', 'C', '绝对值等于 3 的数有两个：3 和 -3，故选 C', 1, 0, 'system', 'system'),
(3, 'MATH-7-003', 'SOLVE', 'MATH', 7, 3, 'apply', '计算 |5-8| + |-3+1|', NULL, '5', '|5-8| = |-3| = 3，|-3+1| = |-2| = 2，3+2=5', 1, 0, 'system', 'system');

-- 题目-知识点关联
INSERT IGNORE INTO `edu_question_knowledge` (`question_id`, `knowledge_id`, `weight`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 5, 1.0, 1, 0, 'system', 'system'),
(2, 5, 1.0, 1, 0, 'system', 'system'),
(3, 5, 1.0, 1, 0, 'system', 'system');

-- 课程
INSERT IGNORE INTO `course` (`id`, `name`, `description`, `subject_code`, `grade`, `textbook_id`, `teacher_id`, `status`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, '七年级数学上册', '人教版七年级数学上册基础课程', 'MATH', 7, 1, 1, 1, 1, 0, 'system', 'system');

-- 课程章节
INSERT IGNORE INTO `course_chapter` (`id`, `course_id`, `name`, `order_no`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 1, '有理数', 1, 1, 0, 'system', 'system'),
(2, 1, '整式的加减', 2, 1, 0, 'system', 'system');

-- 课程小节
INSERT IGNORE INTO `course_section` (`id`, `chapter_id`, `name`, `order_no`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 1, '正数和负数', 1, 1, 0, 'system', 'system'),
(2, 1, '有理数', 2, 1, 0, 'system', 'system'),
(3, 1, '有理数的加减法', 3, 1, 0, 'system', 'system'),
(4, 2, '整式', 1, 1, 0, 'system', 'system');

-- 课程-知识点关联
INSERT IGNORE INTO `course_knowledge` (`course_id`, `knowledge_id`, `section_id`, `sort_order`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 1, 2, 1, 1, 0, 'system', 'system'),
(1, 2, 2, 2, 1, 0, 'system', 'system'),
(1, 3, 2, 3, 1, 0, 'system', 'system'),
(1, 5, 2, 4, 1, 0, 'system', 'system');

-- 能力值
INSERT IGNORE INTO `ability` (`student_id`, `knowledge_id`, `remember`, `understand`, `apply`, `overall_mastery`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 5, 0.85, 0.70, 0.60, 0.72, 1, 0, 'system', 'system');

-- 学习状态
INSERT IGNORE INTO `edu_learning_state` (`student_id`, `knowledge_id`, `state`, `tenant_id`, `workspace_id`, `create_by`, `update_by`) VALUES
(1, 5, 'LEARNING', 1, 0, 'system', 'system');
