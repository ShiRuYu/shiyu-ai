ALTER TABLE edu_study_record ALTER COLUMN knowledge_id DROP NOT NULL;
ALTER TABLE edu_study_record ADD COLUMN IF NOT EXISTS submission_key VARCHAR(36);
ALTER TABLE edu_study_record ADD COLUMN IF NOT EXISTS selected_answer VARCHAR(32);
ALTER TABLE edu_study_record ADD COLUMN IF NOT EXISTS correct_answer VARCHAR(32);
ALTER TABLE edu_study_record ADD COLUMN IF NOT EXISTS answer_analysis TEXT;
ALTER TABLE edu_study_record ADD COLUMN IF NOT EXISTS ability_dimension VARCHAR(32);
ALTER TABLE edu_study_record ADD COLUMN IF NOT EXISTS analysis_applied BOOLEAN NOT NULL DEFAULT FALSE;
CREATE UNIQUE INDEX IF NOT EXISTS uk_sr_practice_submission
    ON edu_study_record(tenant_id, student_id, submission_key);
