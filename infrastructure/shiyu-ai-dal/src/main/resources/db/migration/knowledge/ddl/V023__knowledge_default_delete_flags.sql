-- Normalize rows created before knowledge services explicitly populated the
-- shared soft-delete field.  NULL means active in legacy records, so retain
-- those records as active while making future queries deterministic.
UPDATE `knowledge_space` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_space_member` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_document` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_document_version` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_ingestion_job` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_review_record` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_audit_log` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_evaluation_case` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `knowledge_doc_relation` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
UPDATE `vector_knowledge_chunk` SET `del_flag` = 0 WHERE `del_flag` IS NULL;
