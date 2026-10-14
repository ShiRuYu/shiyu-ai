-- Run once on an existing MySQL installation before starting the updated application.
ALTER TABLE agent_execution ADD COLUMN last_checkpoint_id VARCHAR(64) NULL;
