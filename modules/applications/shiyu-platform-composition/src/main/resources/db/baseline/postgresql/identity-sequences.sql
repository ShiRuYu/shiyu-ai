-- PostgreSQL resources generated from the H2 resources.
-- Do not edit this generated file directly; update the source and regenerate.
-- Advance identity sequences after explicit seed IDs

-- Explicit seed IDs require synchronizing PostgreSQL identity sequences.
DO $$
DECLARE identity_column record; identity_value bigint; has_rows boolean;
BEGIN
  FOR identity_column IN
    SELECT table_name, column_name FROM information_schema.columns
    WHERE table_schema = 'public' AND is_identity = 'YES' AND table_name IN ('agent_checkpoint', 'agent_def', 'agent_eval_case', 'agent_eval_dataset', 'agent_eval_run', 'agent_execution', 'agent_intent_def', 'agent_node_execution', 'agent_version', 'auth_auth_code', 'auth_menu', 'auth_role', 'auth_role_scope_auth_code', 'auth_role_scope_menu', 'auth_tenant', 'auth_tenant_auth_code', 'auth_tenant_menu', 'auth_user', 'auth_user_scope_role', 'common_dict', 'common_schema_baseline', 'edu_ability', 'edu_achievement', 'edu_chapter', 'edu_course', 'edu_course_chapter', 'edu_course_knowledge', 'edu_course_section', 'edu_exam', 'edu_exam_attempt', 'edu_exam_attempt_question', 'edu_exam_question', 'edu_exam_section', 'edu_knowledge_textbook', 'edu_learning_state', 'edu_question', 'edu_question_knowledge', 'edu_resource', 'edu_resource_knowledge', 'edu_review_task', 'edu_student', 'edu_study_plan', 'edu_study_plan_item', 'edu_study_record', 'edu_subject', 'edu_teacher', 'edu_textbook', 'edu_wrong_question', 'governance_quota_reservation', 'governance_quota_tenant', 'governance_usage_record', 'knowledge_audit_log', 'knowledge_base', 'knowledge_difficulty_scale', 'knowledge_difficulty_scale_level', 'knowledge_doc_relation', 'knowledge_document', 'knowledge_document_relation', 'knowledge_document_version', 'knowledge_evaluation_case', 'knowledge_ingestion_job', 'knowledge_relation', 'knowledge_review_record', 'knowledge_space', 'knowledge_space_member', 'model_ai_model', 'model_ai_platform', 'observation_audit_log', 'observation_execution_timeline', 'storage_object', 'storage_upload_chunk', 'storage_upload_session', 'vector_knowledge_chunk')
  LOOP
    EXECUTE format('SELECT COALESCE(MAX(%I), 1) FROM %I.%I',
      identity_column.column_name, 'public', identity_column.table_name) INTO identity_value;
    EXECUTE format('SELECT EXISTS (SELECT 1 FROM %I.%I)',
      'public', identity_column.table_name) INTO has_rows;
    PERFORM setval(
      pg_get_serial_sequence(format('%I.%I', 'public', identity_column.table_name), identity_column.column_name),
      identity_value, has_rows);
  END LOOP;
END;
$$;
