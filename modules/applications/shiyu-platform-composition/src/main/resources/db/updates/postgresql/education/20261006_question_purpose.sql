-- PostgreSQL resources generated from the H2 resources.
-- Do not edit this generated file directly; update the source and regenerate.
-- Converted from modules/business/education/shiyu-education-implementation/src/main/resources/db/updates/education/20261006_question_purpose.sql

ALTER TABLE EDU_QUESTION ADD COLUMN IF NOT EXISTS PURPOSE VARCHAR(20) DEFAULT 'PRACTICE' NOT NULL;
UPDATE EDU_QUESTION SET PURPOSE='EXAM_PRIVATE'
WHERE ID IN (SELECT QUESTION_ID FROM EDU_EXAM_QUESTION WHERE TENANT_ID=EDU_QUESTION.TENANT_ID);
