-- 备份数据库后，在现有 MySQL 数据库执行一次。
UPDATE knowledge_space
SET access_mode = 'TENANT'
WHERE code = 'DEMO_EDU_MATH'
  AND domain_code = 'EDUCATION'
  AND access_mode = 'PRIVATE';
