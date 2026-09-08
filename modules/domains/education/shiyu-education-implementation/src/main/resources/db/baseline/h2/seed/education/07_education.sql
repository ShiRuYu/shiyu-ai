-- Realistic education demo seed for the default tenant.
-- Scores are deliberately high (92-98) to exercise high-performing learner views.

INSERT INTO "PUBLIC"."EDU_SUBJECT"
    ("ID", "CODE", "NAME", "GRADE_LEVEL", "ICON", "SORT_ORDER", "STATUS", "TENANT_ID", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 'MATH', '数学', 'K2', 'calculator', 10, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_TEACHER"
    ("ID", "USER_ID", "TEACHER_NO", "NAME", "SUBJECT", "SCHOOL", "TITLE", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, 'T-2026-001', '王老师', '数学', '拾羽实验学校', '中学一级教师', 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_STUDENT"
    ("ID", "USER_ID", "STUDENT_NO", "NAME", "GENDER", "BIRTH_DATE", "GRADE", "GRADE_LEVEL", "SCHOOL", "CLASS_NAME", "LEARNING_STYLE", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 2, 'DEMO-2026-001', '示例学生', 1, DATE '2013-05-18', 7, 'K2', '拾羽实验学校', '七年级一班', 'visual', 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_QUESTION"
    ("ID", "CODE", "TYPE", "SUBJECT_CODE", "GRADE", "DIFFICULTY", "ABILITY_DIMENSION", "TITLE", "OPTIONS", "ANSWER", "ANALYSIS", "SOURCE", "TAGS", "STATUS", "TENANT_ID", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 'MATH-7-ALG-001', 'CHOICE', 'MATH', 7, 2, 'apply',
        '若 2x + 5 = 17，则 x 的值为？', '["A. 5","B. 6","C. 7","D. 8"]', 'B',
        '移项得 2x = 12，因此 x = 6。', '七年级数学阶段测验', '["一元一次方程","基础运算"]', 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_EXAM"
    ("ID", "NAME", "TYPE", "SUBJECT_CODE", "GRADE", "DURATION_MIN", "TOTAL_SCORE", "STATUS", "TEACHER_ID", "START_TIME", "END_TIME", "TENANT_ID", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, '七年级数学阶段测验（示例）', 'UNIT_TEST', 'MATH', 7, 45, 100, 2, 1,
        TIMESTAMP '2026-09-01 09:00:00', TIMESTAMP '2026-09-01 09:45:00', 1, 1, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_EXAM_SECTION"
    ("ID", "EXAM_ID", "NAME", "ORDER_NO", "SCORE_PER_Q", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, '一、选择题', 1, 100.00, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_EXAM_QUESTION"
    ("ID", "EXAM_ID", "SECTION_ID", "QUESTION_ID", "ORDER_NO", "SCORE", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, 1, 1, 1, 95.00, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_STUDY_RECORD"
    ("ID", "STUDENT_ID", "KNOWLEDGE_ID", "RECORD_TYPE", "QUESTION_ID", "SCORE", "ACCURACY", "DURATION_SEC", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, 1001, 'PRACTICE', 1, 92.00, 0.92, 420, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_STUDY_RECORD"
    ("ID", "STUDENT_ID", "KNOWLEDGE_ID", "RECORD_TYPE", "QUESTION_ID", "SCORE", "ACCURACY", "DURATION_SEC", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (2, 1, 1001, 'EXAM', 1, 95.00, 0.95, 2700, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_ABILITY"
    ("ID", "STUDENT_ID", "KNOWLEDGE_ID", "REMEMBER", "UNDERSTAND", "APPLY", "ANALYZE", "EVALUATE", "CREATE_SCORE", "OVERALL_MASTERY", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, 1001, 98.0, 96.0, 95.0, 93.0, 92.0, 90.0, 94.0, 1, 1, 0, 'system', 'system');
