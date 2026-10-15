-- 备份数据库后，在现有 MySQL 数据库执行一次。
ALTER TABLE edu_question ADD COLUMN purpose VARCHAR(20) NOT NULL DEFAULT 'PRACTICE';
UPDATE edu_question q
JOIN edu_exam_question link
  ON link.tenant_id = q.tenant_id AND link.question_id = q.id
SET q.purpose = 'EXAM_PRIVATE';
