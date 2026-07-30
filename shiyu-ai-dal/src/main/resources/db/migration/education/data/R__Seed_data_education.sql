-- ============================================
-- Data: education — 教育模块种子数据
-- ============================================

-- 学科
INSERT IGNORE INTO `edu_subject` (`id`, `code`, `name`, `grade_level`, `icon`, `sort_order`, `status`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 'MATH', '数学', 'K3', 'i-mdi:math-compass', 1, 1, 1, 'system', 'system'),
(2, 'PHYSICS', '物理', 'K3', 'i-mdi:physics', 2, 1, 1, 'system', 'system'),
(3, 'ENGLISH', '英语', 'K3', 'i-mdi:alphabetical', 3, 1, 1, 'system', 'system'),
(4, 'CHINESE', '语文', 'K3', 'i-mdi:book-open-page-variant', 4, 1, 1, 'system', 'system'),
(5, 'CHEMISTRY', '化学', 'K3', 'i-mdi:flask', 5, 1, 1, 'system', 'system');

-- 教材
INSERT IGNORE INTO `edu_textbook` (tenant_id, `id`, `name`, `subject_code`, `grade`, `publisher`, `isbn`, `create_by`, `update_by`) VALUES
(1, 1, '人教版数学七年级上册', 'MATH', 7, '人民教育出版社', '978-7-107-12345-6', 'system', 'system'),
(1, 2, '人教版数学七年级下册', 'MATH', 7, '人民教育出版社', '978-7-107-12346-3', 'system', 'system'),
(1, 3, '北师大版数学七年级上册', 'MATH', 7, '北京师范大学出版社', '978-7-303-12345-7', 'system', 'system'),
(1, 4, '人教版物理八年级上册', 'PHYSICS', 8, '人民教育出版社', '978-7-107-23456-7', 'system', 'system'),
(1, 5, '人教版英语七年级上册', 'ENGLISH', 7, '人民教育出版社', '978-7-107-34567-8', 'system', 'system'),
(1, 6, '人教版化学九年级上册', 'CHEMISTRY', 9, '人民教育出版社', '978-7-107-45678-9', 'system', 'system');

-- 章节（教材1：人教版数学七年级上册）
INSERT IGNORE INTO `edu_chapter` (tenant_id, `id`, `textbook_id`, `parent_id`, `name`, `chapter_order`, `create_by`, `update_by`) VALUES
(1, 1, 1, NULL, '第一章 有理数', 1, 'system', 'system'),
(1, 2, 1, 1, '1.1 正数和负数', 1, 'system', 'system'),
(1, 3, 1, 1, '1.2 有理数', 2, 'system', 'system'),
(1, 4, 1, 1, '1.3 有理数的加减法', 3, 'system', 'system'),
(1, 5, 1, NULL, '第二章 整式的加减', 2, 'system', 'system'),
(1, 6, 1, 5, '2.1 整式', 1, 'system', 'system'),
(1, 7, 1, 5, '2.2 整式的加减', 2, 'system', 'system');

-- 知识点-教材关联
INSERT IGNORE INTO `edu_knowledge_textbook` (`knowledge_id`, `textbook_id`, `chapter_id`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 3, 1, 'system', 'system'),
(2, 1, 3, 1, 'system', 'system'),
(3, 1, 3, 1, 'system', 'system'),
(4, 1, 3, 1, 'system', 'system'),
(5, 1, 3, 1, 'system', 'system'),
(6, 1, 3, 1, 'system', 'system');

-- 学生
INSERT IGNORE INTO `edu_student` (`id`, `user_id`, `student_no`, `name`, `gender`, `grade`, `grade_level`, `school`, `class_name`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 4, 'STU2024001', '王小明', 1, 7, 'K3', '阳光中学', '七年级一班', 1, 'system', 'system');

-- 教师
INSERT IGNORE INTO `edu_teacher` (`id`, `user_id`, `teacher_no`, `name`, `subject`, `school`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 3, 'TCH2024001', '张老师', 'MATH', '阳光中学', 1, 'system', 'system');

-- 题目
INSERT IGNORE INTO `edu_question` (`id`, `code`, `type`, `subject_code`, `grade`, `difficulty`, `ability_dimension`, `title`, `options`, `answer`, `analysis`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 'MATH-7-001', 'CHOICE', 'MATH', 7, 1, 'understand', '|-5| 等于多少？', '["A. -5", "B. 0", "C. 5", "D. 10"]', 'C', '绝对值表示一个数到原点的距离，|-5| = 5，故选 C', 1, 'system', 'system'),
(2, 'MATH-7-002', 'CHOICE', 'MATH', 7, 2, 'apply', '若 |x| = 3，则 x 的值为？', '["A. 3", "B. -3", "C. ±3", "D. 0"]', 'C', '绝对值等于 3 的数有两个：3 和 -3，故选 C', 1, 'system', 'system'),
(3, 'MATH-7-003', 'SOLVE', 'MATH', 7, 3, 'apply', '计算 |5-8| + |-3+1|', NULL, '5', '|5-8| = |-3| = 3，|-3+1| = |-2| = 2，3+2=5', 1, 'system', 'system');

-- 题目-知识点关联
INSERT IGNORE INTO `edu_question_knowledge` (`question_id`, `knowledge_id`, `weight`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 5, 1.0, 1, 'system', 'system'),
(2, 5, 1.0, 1, 'system', 'system'),
(3, 5, 1.0, 1, 'system', 'system');

-- 课程
INSERT IGNORE INTO `edu_course` (`id`, `name`, `description`, `subject_code`, `grade`, `textbook_id`, `teacher_id`, `status`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, '七年级数学上册', '人教版七年级数学上册基础课程', 'MATH', 7, 1, 1, 1, 1, 'system', 'system');

-- 课程章节
INSERT IGNORE INTO `edu_course_chapter` (`id`, `course_id`, `name`, `order_no`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, '有理数', 1, 1, 'system', 'system'),
(2, 1, '整式的加减', 2, 1, 'system', 'system');

-- 课程小节
INSERT IGNORE INTO `edu_course_section` (`id`, `chapter_id`, `name`, `order_no`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, '正数和负数', 1, 1, 'system', 'system'),
(2, 1, '有理数', 2, 1, 'system', 'system'),
(3, 1, '有理数的加减法', 3, 1, 'system', 'system'),
(4, 2, '整式', 1, 1, 'system', 'system');

-- 课程-知识点关联
INSERT IGNORE INTO `edu_course_knowledge` (`course_id`, `knowledge_id`, `section_id`, `sort_order`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 2, 1, 1, 'system', 'system'),
(1, 2, 2, 2, 1, 'system', 'system'),
(1, 3, 2, 3, 1, 'system', 'system'),
(1, 5, 2, 4, 1, 'system', 'system');

-- 能力值
INSERT IGNORE INTO `edu_ability` (`student_id`, `knowledge_id`, `remember`, `understand`, `apply`, `overall_mastery`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 5, 0.85, 0.70, 0.60, 0.72, 1, 'system', 'system');

-- 学习状态
INSERT IGNORE INTO `edu_learning_state` (`student_id`, `knowledge_id`, `state`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 5, 'LEARNING', 1, 'system', 'system');

-- 学习资源
INSERT IGNORE INTO `edu_resource` (`id`, `name`, `type`, `url`, `subject_code`, `grade`, `difficulty`, `cover_url`, `description`, `view_count`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, '有理数入门视频', 'VIDEO', 'https://example.com/video1.mp4', 'MATH', 7, 1, 'https://example.com/cover1.jpg', '有理数基础教学视频', 150, 1, 'system', 'system'),
(2, '绝对值练习题集', 'EXERCISE', 'https://example.com/exercise1.pdf', 'MATH', 7, 2, 'https://example.com/cover2.jpg', '绝对值相关练习题', 89, 1, 'system', 'system'),
(3, '数轴互动演示', 'INTERACTIVE', 'https://example.com/interactive1.html', 'MATH', 7, 1, 'https://example.com/cover3.jpg', '数轴概念互动学习', 67, 1, 'system', 'system');

-- 考试
INSERT IGNORE INTO `edu_exam` (`id`, `name`, `type`, `subject_code`, `grade`, `duration_min`, `total_score`, `status`, `teacher_id`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, '七年级数学第一章测验', 'UNIT_TEST', 'MATH', 7, 45, 100, 1, 1, 1, 'system', 'system'),
(2, '七年级数学期中考试', 'MIDTERM', 'MATH', 7, 90, 150, 1, 1, 1, 'system', 'system');

-- 考试分区
INSERT IGNORE INTO `edu_exam_section` (`id`, `exam_id`, `name`, `order_no`, `score_per_q`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, '选择题', 1, 0, 1, 'system', 'system'),
(2, 1, '解答题', 2, 0, 1, 'system', 'system');

-- 考试-题目关联
INSERT IGNORE INTO `edu_exam_question` (`exam_id`, `section_id`, `question_id`, `order_no`, `score`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 1, 1, 10, 1, 'system', 'system'),
(1, 1, 2, 2, 10, 1, 'system', 'system'),
(1, 2, 3, 1, 20, 1, 'system', 'system');

-- 学习计划
INSERT IGNORE INTO `edu_study_plan` (`id`, `student_id`, `target_knowledge_id`, `name`, `start_date`, `end_date`, `status`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 1, '七年级数学上册学习计划', '2024-09-01', '2024-12-31', '0', 1, 'system', 'system');

-- 学习计划项
INSERT IGNORE INTO `edu_study_plan_item` (`id`, `plan_id`, `knowledge_id`, `plan_date`, `order_no`, `status`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 1, '2024-09-05', 1, '2', 1, 'system', 'system'),
(2, 1, 2, '2024-09-10', 2, '2', 1, 'system', 'system'),
(3, 1, 5, '2024-09-15', 3, '1', 1, 'system', 'system');

-- 复习任务
INSERT IGNORE INTO `edu_review_task` (`id`, `student_id`, `knowledge_id`, `review_round`, `review_date`, `status`, `result_score`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 1, 1, '2024-09-06', '2', 85, 1, 'system', 'system'),
(2, 1, 2, 1, '2024-09-11', '2', 78, 1, 'system', 'system'),
(3, 1, 5, 1, '2024-09-20', '0', NULL, 1, 'system', 'system');

-- 错题记录
INSERT IGNORE INTO `edu_wrong_question` (`id`, `student_id`, `question_id`, `knowledge_id`, `student_answer`, `correct_times`, `tenant_id`, `create_by`, `update_by`) VALUES
(1, 1, 2, 1, 'A', 2, 1, 'system', 'system');
