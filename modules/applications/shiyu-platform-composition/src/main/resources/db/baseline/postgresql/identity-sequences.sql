-- 从 H2 源资源生成的 PostgreSQL 资源。
-- 不要直接编辑此生成文件；请修改源资源后重新生成。
-- 写入显式种子标识后推进自增序列

-- 显式种子标识写入后需要同步 PostgreSQL 自增序列。
DO $$
DECLARE identity_column record; identity_value bigint;
BEGIN
  FOR identity_column IN
    SELECT table_name, column_name FROM information_schema.columns
    WHERE table_schema = 'public' AND is_identity = 'YES' AND table_name IN ('agent_checkpoint', 'agent_def', 'agent_eval_case', 'agent_eval_dataset', 'agent_eval_run', 'agent_execution', 'agent_intent_def', 'agent_node_execution', 'agent_version', 'ai_app', 'ai_app_version', 'ai_run', 'ai_run_event', 'ai_tool_approval', 'auth_auth_code', 'auth_menu', 'auth_role', 'auth_role_scope_auth_code', 'auth_role_scope_menu', 'auth_tenant', 'auth_tenant_auth_code', 'auth_tenant_menu', 'auth_user', 'auth_user_scope_role', 'chat_character_asset', 'chat_conversation', 'chat_generation_active', 'chat_generation_postprocessing', 'chat_generation_run', 'chat_group_chat', 'chat_idempotency_key', 'chat_lorebook_asset', 'chat_message', 'chat_persona_asset', 'chat_prompt_template', 'common_dict', 'common_schema_baseline', 'edu_ability', 'edu_achievement', 'edu_chapter', 'edu_course', 'edu_course_chapter', 'edu_course_knowledge', 'edu_course_section', 'edu_exam', 'edu_exam_attempt', 'edu_exam_attempt_question', 'edu_exam_question', 'edu_exam_section', 'edu_knowledge_textbook', 'edu_learning_state', 'edu_question', 'edu_question_knowledge', 'edu_resource', 'edu_resource_knowledge', 'edu_review_task', 'edu_student', 'edu_study_plan', 'edu_study_plan_item', 'edu_study_record', 'edu_subject', 'edu_teacher', 'edu_textbook', 'edu_wrong_question', 'governance_quota_reservation', 'governance_quota_tenant', 'governance_usage_record', 'knowledge_audit_log', 'knowledge_base', 'knowledge_difficulty_scale', 'knowledge_difficulty_scale_level', 'knowledge_doc_relation', 'knowledge_document', 'knowledge_document_relation', 'knowledge_document_version', 'knowledge_evaluation_case', 'knowledge_ingestion_job', 'knowledge_relation', 'knowledge_review_record', 'knowledge_space', 'knowledge_space_member', 'memory_consolidation_job', 'memory_edge', 'memory_entity', 'memory_event', 'memory_retrieval_trace', 'model_ai_model', 'model_ai_platform', 'observation_audit_log', 'observation_execution_timeline', 'plugin_market_entry', 'storage_object', 'storage_upload_chunk', 'storage_upload_session', 'vector_knowledge_chunk')
  LOOP
    EXECUTE format('LOCK TABLE %I.%I IN ACCESS EXCLUSIVE MODE',
      'public', identity_column.table_name);
    EXECUTE format('SELECT COALESCE(MAX(%I), 0) + 1 FROM %I.%I',
      identity_column.column_name, 'public', identity_column.table_name) INTO identity_value;
    EXECUTE format('ALTER TABLE %I.%I ALTER COLUMN %I RESTART WITH %s',
      'public', identity_column.table_name, identity_column.column_name, identity_value);
  END LOOP;
END;
$$;
