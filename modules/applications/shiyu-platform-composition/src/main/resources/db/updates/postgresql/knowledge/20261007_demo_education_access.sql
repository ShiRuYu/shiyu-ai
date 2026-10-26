-- PostgreSQL resources generated from the H2 resources.
-- Do not edit this generated file directly; update the source and regenerate.
-- Converted from modules/domains/knowledge/shiyu-knowledge-implementation/src/main/resources/db/updates/knowledge/20261007_demo_education_access.sql

UPDATE "knowledge_space"
SET "access_mode" = 'TENANT'
WHERE "code" = 'DEMO_EDU_MATH'
  AND "domain_code" = 'EDUCATION'
  AND "access_mode" = 'PRIVATE';
