-- Realistic curriculum structure for the sample mathematics learner.
INSERT INTO "PUBLIC"."EDU_TEXTBOOK"
    ("ID", "NAME", "SUBJECT_CODE", "GRADE", "PUBLISHER", "ISBN", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, '义务教育教科书 数学 七年级上册', 'MATH', 7, '人民教育出版社', '9787107335661', 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_CHAPTER"
    ("ID", "TEXTBOOK_ID", "PARENT_ID", "NAME", "CHAPTER_ORDER", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, NULL, '第三章 一元一次方程', 3, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_KNOWLEDGE_TEXTBOOK"
    ("ID", "KNOWLEDGE_ID", "TEXTBOOK_ID", "CHAPTER_ID", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1001, 1, 1, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_COURSE"
    ("ID", "NAME", "DESCRIPTION", "SUBJECT_CODE", "GRADE", "TEXTBOOK_ID", "TEACHER_ID", "TOTAL_HOURS", "STATUS", "VIEW_COUNT", "TENANT_ID", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, '七年级数学同步辅导', '围绕教材章节进行概念讲解、例题练习与错题复习。', 'MATH', 7, 1, 1, 16, 1, 128, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_COURSE_CHAPTER"
    ("ID", "COURSE_ID", "NAME", "ORDER_NO", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, '一元一次方程', 1, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_COURSE_SECTION"
    ("ID", "CHAPTER_ID", "NAME", "ORDER_NO", "CONTENT_URL", "VIDEO_URL", "DURATION_MIN", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1, '解方程的基本步骤', 1, '/education/courses/1/sections/1', NULL, 25, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_COURSE_KNOWLEDGE"
    ("COURSE_ID", "KNOWLEDGE_ID", "SECTION_ID", "SORT_ORDER", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1001, 1, 1, 1, 1, 0, 'system', 'system');
