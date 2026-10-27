-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261012_checkpoint_pointer.sql

ALTER TABLE AGENT_EXECUTION ADD COLUMN IF NOT EXISTS LAST_CHECKPOINT_ID VARCHAR(64);
