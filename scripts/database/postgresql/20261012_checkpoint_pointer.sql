ALTER TABLE agent_execution ADD COLUMN IF NOT EXISTS last_checkpoint_id VARCHAR(64);
