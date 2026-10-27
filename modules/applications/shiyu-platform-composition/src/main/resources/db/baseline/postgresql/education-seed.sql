-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 基线种子数据

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/01_auth.sql
-- 扩展模式：iam
INSERT INTO "auth_auth_code" VALUES(53, 'edu:subject:list', '查看学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(54, 'edu:subject:create', '创建学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(55, 'edu:subject:edit', '编辑学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(56, 'edu:subject:delete', '删除学科', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(57, 'edu:textbook:list', '查看教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(58, 'edu:textbook:create', '创建教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(59, 'edu:textbook:edit', '编辑教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(60, 'edu:textbook:delete', '删除教材', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(61, 'edu:chapter:list', '查看章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(62, 'edu:chapter:create', '创建章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(63, 'edu:chapter:edit', '编辑章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(64, 'edu:chapter:delete', '删除章节', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(65, 'edu:course:list', '查看课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(66, 'edu:course:create', '创建课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(67, 'edu:course:edit', '编辑课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(68, 'edu:course:delete', '删除课程', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(69, 'edu:question:list', '查看题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(70, 'edu:question:create', '创建题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(71, 'edu:question:edit', '编辑题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(72, 'edu:question:delete', '删除题目', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(73, 'edu:exam:list', '查看考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(74, 'edu:exam:create', '创建考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(75, 'edu:exam:edit', '编辑考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(76, 'edu:exam:delete', '删除考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(77, 'edu:exam:publish', '发布考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(214, 'edu:exam:take', '参加考试', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(78, 'edu:student:list', '查看学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(211, 'edu:student:create', '创建学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(212, 'edu:student:update', '编辑学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(213, 'edu:student:delete', '删除学生', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(79, 'edu:resource:list', '查看资源', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(80, 'edu:resource:upload', '上传资源', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(81, 'edu:resource:delete', '删除资源', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(82, 'edu:plan:list', '查看学习计划', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(83, 'edu:review:list', '查看复习任务', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(84, 'edu:analytics', '查看学情分析', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);
INSERT INTO "auth_auth_code" VALUES(85, 'edu:wrong-question', '查看错题管理', 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP);

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/02_navigation.sql
-- 扩展模式：iam
INSERT INTO "auth_menu" ("id","name","code","type","parent_id","tenant_id","path","redirect","icon","component","description","show","status","order","del_flag","create_by","create_time","update_by","update_time") VALUES
(2050,'教育中心','EducationCenter','CATALOG',NULL,1,'/education-center','/education-center/learning','lucide:graduation-cap','','学习、练习、复习与 AI 辅学',TRUE,1,6,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2051,'学习','EducationLearning','MENU',2050,1,'/education-center/learning',NULL,'lucide:book-open','feature:education.learning','课程与学习资源',TRUE,1,1,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2052,'练习','EducationPractice','MENU',2050,1,'/education-center/practice',NULL,'lucide:clipboard-check','feature:education.practice','题库、错题与考试',TRUE,1,2,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2053,'AI 辅学','EducationTutor','MENU',2050,1,'/education-center/ai-tutor',NULL,'lucide:sparkles','feature:education.tutor','讲解、出题、规划与报告',TRUE,1,3,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP),
(2054,'学习分析','EducationAnalytics','MENU',2050,1,'/education-center/analytics',NULL,'lucide:chart-no-axes-combined','feature:education.analytics','学习报告与趋势',TRUE,1,4,0,'system',CURRENT_TIMESTAMP,'system',CURRENT_TIMESTAMP);

INSERT INTO "auth_role_scope_menu" ("role_id","menu_id","tenant_id","status","del_flag","create_by","create_time","update_by","update_time")
SELECT R."id", M."id", 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "auth_role" R CROSS JOIN "auth_menu" M
WHERE R."id" IN (1,2,3) AND M."id" BETWEEN 2050 AND 2054;

INSERT INTO "auth_tenant_menu" ("tenant_id","menu_id","status","create_time","update_time")
SELECT 1, M."id", 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "auth_menu" M WHERE M."id" BETWEEN 2050 AND 2054;

INSERT INTO "auth_role_scope_auth_code" ("role_id","auth_code_id","tenant_id","status","del_flag","create_by","create_time","update_by","update_time")
SELECT R."id", C."id", 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "auth_role" R CROSS JOIN "auth_auth_code" C
WHERE R."id" IN (1,2) AND C."id" IN (53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,211,212,213);

INSERT INTO "auth_role_scope_auth_code" ("role_id","auth_code_id","tenant_id","status","del_flag","create_by","create_time","update_by","update_time")
SELECT R."id", 214, 1, 1, 0, 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP
FROM "auth_role" R WHERE R."id" IN (1,2,3);

INSERT INTO "auth_tenant_auth_code" ("tenant_id","auth_code_id","status","create_time","update_time")
SELECT 1, C."id", 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM "auth_auth_code" C
WHERE C."id" IN (53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,211,212,213,214);

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/07_education.sql

INSERT INTO "edu_subject"
    ("id", "code", "name", "grade_level", "icon", "sort_order", "status", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (1, 'MATH', '数学', 'K2', 'calculator', 10, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_teacher"
    ("id", "user_id", "teacher_no", "name", "subject", "school", "title", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 'T-2026-001', '王老师', '数学', '拾羽实验学校', '中学一级教师', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_student"
    ("id", "user_id", "student_no", "name", "gender", "birth_date", "grade", "grade_level", "school", "class_name", "learning_style", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 2, 'DEMO-2026-001', '示例学生', 1, DATE '2013-05-18', 7, 'K2', '拾羽实验学校', '七年级一班', 'visual', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_question"
    ("id", "code", "type", "purpose", "subject_code", "grade", "difficulty", "ability_dimension", "title", "options", "answer", "analysis", "source", "tags", "status", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (1, 'MATH-7-ALG-001', 'CHOICE', 'EXAM_PRIVATE', 'MATH', 7, 2, 'apply',
        '若 2x + 5 = 17，则 x 的值为？', '["A. 5","B. 6","C. 7","D. 8"]', 'B',
        '移项得 2x = 12，因此 x = 6。', '七年级数学阶段测验', '["一元一次方程","基础运算"]', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_question"
    ("id", "code", "type", "purpose", "subject_code", "grade", "difficulty", "ability_dimension", "title", "options", "answer", "analysis", "source", "tags", "status", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (2, 'MATH-7-PRACTICE-001', 'CHOICE', 'PRACTICE', 'MATH', 7, 1, 'apply',
        '3 + 4 的值为？', '["A. 6","B. 7","C. 8"]', 'B',
        '3 加 4 等于 7。', '公开练习', '["基础运算"]', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_exam"
    ("id", "name", "type", "subject_code", "grade", "duration_min", "total_score", "status", "teacher_id", "start_time", "end_time", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (1, '七年级数学阶段测验（示例）', 'UNIT_TEST', 'MATH', 7, 45, 100, 2, 1,
        TIMESTAMP '2026-09-01 09:00:00', TIMESTAMP '2026-09-01 09:45:00', 1, 1, 'system', 'system');

INSERT INTO "edu_exam_section"
    ("id", "exam_id", "name", "order_no", "score_per_q", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, '一、选择题', 1, 100.00, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_exam_question"
    ("id", "exam_id", "section_id", "question_id", "order_no", "score", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 1, 1, 1, 95.00, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_study_record"
    ("id", "student_id", "knowledge_id", "record_type", "question_id", "score", "accuracy", "duration_sec", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 1001, 'PRACTICE', 2, 92.00, 0.92, 420, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_study_record"
    ("id", "student_id", "knowledge_id", "record_type", "question_id", "score", "accuracy", "duration_sec", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (2, 1, 1001, 'EXAM', 1, 95.00, 0.95, 2700, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_ability"
    ("id", "student_id", "knowledge_id", "remember", "understand", "apply", "analyze", "evaluate", "create_score", "overall_mastery", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 1001, 98.0, 96.0, 95.0, 93.0, 92.0, 90.0, 94.0, 1, 1, 0, 'system', 'system');

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/08_learning_progress.sql
INSERT INTO "edu_review_task"
    ("id", "student_id", "knowledge_id", "review_date", "review_round", "status", "result_score", "completed_at", "tenant_id", "create_by", "update_by", "del_flag")
VALUES (1, 1, 1001, DATE '2026-09-10', 1, 0, NULL, NULL, 1, 'system', 'system', 0);

INSERT INTO "edu_study_plan"
    ("id", "student_id", "target_knowledge_id", "name", "start_date", "end_date", "status", "tenant_id", "create_by", "update_by", "del_flag")
VALUES (1, 1, 1001, '一元一次方程巩固计划', DATE '2026-09-08', DATE '2026-09-14', 0, 1, 'system', 'system', 0);

INSERT INTO "edu_study_plan_item"
    ("id", "plan_id", "knowledge_id", "plan_date", "order_no", "status", "completed_at", "tenant_id", "create_by", "update_by", "del_flag")
VALUES (1, 1, 1001, DATE '2026-09-09', 1, 2, TIMESTAMP '2026-09-09 19:30:00', 1, 'system', 'system', 0);

INSERT INTO "edu_wrong_question"
    ("id", "student_id", "question_id", "knowledge_id", "student_answer", "correct_times", "created_at", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 1, 1001, 'C', 0, TIMESTAMP '2026-09-08 18:20:00', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_learning_state"
    ("id", "student_id", "knowledge_id", "state", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 1001, 'PROFICIENT', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_achievement"
    ("id", "student_id", "code", "name", "description", "icon", "earned_at", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, 'ALGEBRA_FOUNDATION', '代数基础达标', '完成一元一次方程阶段测验并达到 90 分以上。', 'achievement-algebra', TIMESTAMP '2026-09-08 20:00:00', 1, 1, 0, 'system', 'system');

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/09_curriculum.sql
INSERT INTO "edu_textbook"
    ("id", "name", "subject_code", "grade", "publisher", "isbn", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, '义务教育教科书 数学 七年级上册', 'MATH', 7, '人民教育出版社', '9787107335661', 1, 1, 0, 'system', 'system');

INSERT INTO "edu_chapter"
    ("id", "textbook_id", "parent_id", "name", "chapter_order", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, NULL, '第三章 一元一次方程', 3, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_knowledge_textbook"
    ("id", "knowledge_id", "textbook_id", "chapter_id", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1001, 1, 1, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_course"
    ("id", "name", "description", "subject_code", "grade", "textbook_id", "teacher_id", "total_hours", "status", "view_count", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (1, '七年级数学同步辅导', '围绕教材章节进行概念讲解、例题练习与错题复习。', 'MATH', 7, 1, 1, 16, 1, 128, 1, 0, 'system', 'system');

INSERT INTO "edu_course_chapter"
    ("id", "course_id", "name", "order_no", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, '一元一次方程', 1, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_course_section"
    ("id", "chapter_id", "name", "order_no", "content_url", "video_url", "duration_min", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1, '解方程的基本步骤', 1, '/education/courses/1/sections/1', NULL, 25, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_course_knowledge"
    ("course_id", "knowledge_id", "section_id", "sort_order", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1001, 1, 1, 1, 1, 0, 'system', 'system');

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/10_resources.sql
INSERT INTO "edu_resource"
    ("id", "name", "type", "url", "size_bytes", "duration_sec", "subject_code", "grade", "difficulty", "description", "status", "view_count", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (1, '一元一次方程例题讲解', 'VIDEO', '/education/resources/math-linear-equation-intro.mp4', 52428800, 780, 'MATH', 7, 2, '用生活化例子讲解列方程与解方程的完整步骤。', 1, 86, 1, 0, 'system', 'system');

INSERT INTO "edu_resource_knowledge"
    ("resource_id", "knowledge_id", "sort_order", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1001, 1, 1, 1, 0, 'system', 'system');

INSERT INTO "edu_question_knowledge"
    ("question_id", "knowledge_id", "weight", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (1, 1001, 1.0, 1, 1, 0, 'system', 'system'),
       (2, 1001, 1.0, 1, 1, 0, 'system', 'system');

-- 来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/baseline/h2/seed/education/11_resource_variants.sql
INSERT INTO "edu_resource"
    ("id", "name", "type", "url", "size_bytes", "subject_code", "grade", "difficulty", "description", "status", "view_count", "tenant_id", "del_flag", "create_by", "update_by")
VALUES (2, '一元一次方程课后练习册', 'PDF', '/education/resources/math-linear-equation-practice.pdf', 1048576, 'MATH', 7, 2, '包含 12 道基础到中等难度练习题，适合课后巩固。', 1, 42, 1, 0, 'system', 'system');

INSERT INTO "edu_resource_knowledge"
    ("resource_id", "knowledge_id", "sort_order", "tenant_id", "status", "del_flag", "create_by", "update_by")
VALUES (2, 1001, 2, 1, 1, 0, 'system', 'system');
