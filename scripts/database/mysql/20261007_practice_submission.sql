-- 备份数据库后，在现有 MySQL 数据库执行一次。
ALTER TABLE edu_study_record MODIFY COLUMN knowledge_id BIGINT NULL;
ALTER TABLE edu_study_record ADD COLUMN submission_key VARCHAR(36) NULL;
ALTER TABLE edu_study_record ADD COLUMN selected_answer VARCHAR(32) NULL;
ALTER TABLE edu_study_record ADD COLUMN correct_answer VARCHAR(32) NULL;
ALTER TABLE edu_study_record ADD COLUMN answer_analysis LONGTEXT NULL;
ALTER TABLE edu_study_record ADD COLUMN ability_dimension VARCHAR(32) NULL;
ALTER TABLE edu_study_record ADD COLUMN analysis_applied BOOLEAN NOT NULL DEFAULT FALSE;
CREATE UNIQUE INDEX uk_sr_practice_submission
    ON edu_study_record (tenant_id, student_id, submission_key);
