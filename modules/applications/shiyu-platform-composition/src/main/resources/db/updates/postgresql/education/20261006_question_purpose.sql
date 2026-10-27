-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/updates/education/20261006_question_purpose.sql

ALTER TABLE EDU_QUESTION ADD COLUMN IF NOT EXISTS PURPOSE VARCHAR(20) DEFAULT 'PRACTICE' NOT NULL;
UPDATE EDU_QUESTION SET PURPOSE='EXAM_PRIVATE'
WHERE ID IN (SELECT QUESTION_ID FROM EDU_EXAM_QUESTION WHERE TENANT_ID=EDU_QUESTION.TENANT_ID);
