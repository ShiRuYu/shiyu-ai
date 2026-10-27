-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/updates/knowledge/20261007_demo_education_access.sql

UPDATE "knowledge_space"
SET "access_mode" = 'TENANT'
WHERE "code" = 'DEMO_EDU_MATH'
  AND "domain_code" = 'EDUCATION'
  AND "access_mode" = 'PRIVATE';
