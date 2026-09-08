-- A second, distinct resource variant for recommendation and content API flows.
INSERT INTO "PUBLIC"."EDU_RESOURCE"
    ("ID", "NAME", "TYPE", "URL", "SIZE_BYTES", "SUBJECT_CODE", "GRADE", "DIFFICULTY", "DESCRIPTION", "STATUS", "VIEW_COUNT", "TENANT_ID", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (2, '一元一次方程课后练习册', 'PDF', '/education/resources/math-linear-equation-practice.pdf', 1048576, 'MATH', 7, 2, '包含 12 道基础到中等难度练习题，适合课后巩固。', 1, 42, 1, 0, 'system', 'system');

INSERT INTO "PUBLIC"."EDU_RESOURCE_KNOWLEDGE"
    ("RESOURCE_ID", "KNOWLEDGE_ID", "SORT_ORDER", "TENANT_ID", "STATUS", "DEL_FLAG", "CREATE_BY", "UPDATE_BY")
VALUES (2, 1001, 2, 1, 1, 0, 'system', 'system');
