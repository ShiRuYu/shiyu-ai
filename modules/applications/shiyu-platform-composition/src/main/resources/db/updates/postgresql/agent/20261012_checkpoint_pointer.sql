-- PostgreSQL resources generated from the H2 resources.
-- Do not edit this generated file directly; update the source and regenerate.
-- Converted from modules/domains/agent/shiyu-agent-implementation/src/main/resources/db/updates/agent/20261012_checkpoint_pointer.sql

ALTER TABLE AGENT_EXECUTION ADD COLUMN IF NOT EXISTS LAST_CHECKPOINT_ID VARCHAR(64);
