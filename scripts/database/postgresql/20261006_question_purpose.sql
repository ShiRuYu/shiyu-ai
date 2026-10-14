-- Apply to a database using the preceding baseline before this revision.
BEGIN;
ALTER TABLE edu_question ADD COLUMN IF NOT EXISTS purpose VARCHAR(20) DEFAULT 'PRACTICE' NOT NULL;
UPDATE edu_question AS question SET purpose='EXAM_PRIVATE'
WHERE EXISTS (SELECT 1 FROM edu_exam_question AS link
              WHERE link.tenant_id=question.tenant_id AND link.question_id=question.id);
COMMIT;
