-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261027_version_numeric_delete_flag.sql

-- 删除标志与 BaseEntity 的整数属性对齐，保留已有 0、1 和 NULL。
ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG DROP DEFAULT;
ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG TYPE SMALLINT USING CAST(DEL_FLAG AS SMALLINT);
ALTER TABLE AGENT_VERSION ALTER COLUMN DEL_FLAG SET DEFAULT 0;
