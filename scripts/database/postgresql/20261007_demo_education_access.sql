UPDATE knowledge_space
SET access_mode = 'TENANT'
WHERE code = 'DEMO_EDU_MATH'
  AND domain_code = 'EDUCATION'
  AND access_mode = 'PRIVATE';
