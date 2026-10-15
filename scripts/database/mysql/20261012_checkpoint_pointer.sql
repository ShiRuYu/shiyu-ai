-- 启动更新后的应用前，在现有 MySQL 数据库执行一次。
ALTER TABLE agent_execution ADD COLUMN last_checkpoint_id VARCHAR(64) NULL;
