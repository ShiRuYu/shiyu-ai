-- Learning resources connected to the curriculum and knowledge point.
INSERT INTO "PUBLIC"."EDU_RESOURCE"
    ("ID", "NAME", "TYPE", "URL", "SIZE_BYTES", "DURATION_SEC", "SUBJECT_CODE", "GRADE", "DIFFICULTY", "DESCRIPTION", "STATUS", "VIEW_COUNT", "TENANT_ID", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, '一元一次方程例题讲解', 'VIDEO', '/education/resources/math-linear-equation-intro.mp4', 52428800, 780, 'MATH', 7, 2, '用生活化例子讲解列方程与解方程的完整步骤。', 1, 86, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_RESOURCE_KNOWLEDGE"
    ("RESOURCE_ID", "KNOWLEDGE_ID", "SORT_ORDER", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1001, 1, 1, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_QUESTION_KNOWLEDGE"
    ("QUESTION_ID", "KNOWLEDGE_ID", "WEIGHT", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (1, 1001, 1.0, 1, 1, 0, 'system', 'system');
