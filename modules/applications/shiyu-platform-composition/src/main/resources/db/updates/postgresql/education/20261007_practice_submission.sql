-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 转换来源：modules/business/education/shiyu-education-implementation/src/main/resources/db/updates/education/20261007_practice_submission.sql

ALTER TABLE "edu_study_record" ALTER COLUMN "knowledge_id" DROP NOT NULL;
ALTER TABLE "edu_study_record" ADD COLUMN IF NOT EXISTS "submission_key" CHARACTER VARYING(36);
ALTER TABLE "edu_study_record" ADD COLUMN IF NOT EXISTS "selected_answer" CHARACTER VARYING(32);
ALTER TABLE "edu_study_record" ADD COLUMN IF NOT EXISTS "correct_answer" CHARACTER VARYING(32);
ALTER TABLE "edu_study_record" ADD COLUMN IF NOT EXISTS "answer_analysis" CHARACTER VARYING;
ALTER TABLE "edu_study_record" ADD COLUMN IF NOT EXISTS "ability_dimension" CHARACTER VARYING(32);
ALTER TABLE "edu_study_record" ADD COLUMN IF NOT EXISTS "analysis_applied" BOOLEAN DEFAULT FALSE NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS "uk_sr_practice_submission"
    ON "edu_study_record"("tenant_id", "student_id", "submission_key");
