package com.shiyu.ai.composition.database;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInitializerTest {

    private static final Set<String> SEEDED_TABLES = Set.of(
            "COMMON_DICT",
            "AUTH_AUTH_CODE", "AUTH_MENU", "AUTH_ROLE", "AUTH_ROLE_SCOPE_AUTH_CODE",
            "AUTH_ROLE_SCOPE_MENU", "AUTH_TENANT", "AUTH_TENANT_AUTH_CODE",
            "AUTH_TENANT_MENU", "AUTH_USER", "AUTH_USER_SCOPE_ROLE",
            "MODEL_AI_MODEL", "MODEL_AI_PLATFORM", "AGENT_DEF", "AGENT_INTENT_DEF",
            "AI_APP", "AI_APP_VERSION",
            "AGENT_VERSION",
            "KNOWLEDGE_DIFFICULTY_SCALE", "KNOWLEDGE_DIFFICULTY_SCALE_LEVEL", "KNOWLEDGE_SPACE",
            "KNOWLEDGE_BASE", "KNOWLEDGE_DOCUMENT", "KNOWLEDGE_DOCUMENT_VERSION", "KNOWLEDGE_DOC_RELATION",
            "VECTOR_KNOWLEDGE_CHUNK",
            "EDU_SUBJECT", "EDU_STUDENT", "EDU_QUESTION", "EDU_EXAM",
            "EDU_EXAM_SECTION", "EDU_EXAM_QUESTION", "EDU_STUDY_RECORD", "EDU_ABILITY", "EDU_REVIEW_TASK",
            "EDU_STUDY_PLAN", "EDU_STUDY_PLAN_ITEM", "EDU_WRONG_QUESTION", "EDU_LEARNING_STATE", "EDU_ACHIEVEMENT",
            "EDU_TEXTBOOK", "EDU_CHAPTER", "EDU_KNOWLEDGE_TEXTBOOK", "EDU_TEACHER", "EDU_COURSE", "EDU_COURSE_CHAPTER",
            "EDU_COURSE_SECTION", "EDU_COURSE_KNOWLEDGE",
            "EDU_RESOURCE", "EDU_RESOURCE_KNOWLEDGE", "EDU_QUESTION_KNOWLEDGE",
            "KNOWLEDGE_INGESTION_JOB",
            "KNOWLEDGE_AUDIT_LOG",
            "KNOWLEDGE_EVALUATION_CASE",
            "CHAT_CONVERSATION", "CHAT_MESSAGE", "CHAT_GENERATION_RUN", "GOVERNANCE_USAGE_RECORD",
            "MEMORY_ENTITY", "MEMORY_EVENT", "MEMORY_EDGE", "MEMORY_CONSOLIDATION_JOB", "PLUGIN_MARKET_ENTRY"
    );

    @Test
    void installsCompleteBaselineAndSkipsSecondRun() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);

        initializer.initialize();

        assertEquals(90, scalar(dataSource,
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                        + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_TYPE='BASE TABLE'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM COMMON_SCHEMA_BASELINE "
                        + "WHERE BASELINE_VERSION='4' AND SEED_PROFILE='system-ai'"));

        assertEquals(18, scalar(dataSource, "SELECT COUNT(*) FROM COMMON_DICT"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM COMMON_DICT WHERE REMARK='美国纽约时区'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_TENANT"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_TENANT WHERE NAME='默认租户'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_USER WHERE USERNAME='admin'"));
        assertEquals(3, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_ROLE"));
        assertEquals(38, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU"));
        assertEquals(111, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_AUTH_CODE"));
        assertEquals(38, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_TENANT_MENU"));
        assertEquals(111, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_TENANT_AUTH_CODE"));
        assertTrue(scalar(dataSource, "SELECT COUNT(*) FROM AUTH_ROLE_SCOPE_MENU") >= 99);
        assertEquals(222, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_ROLE_SCOPE_AUTH_CODE"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_USER_SCOPE_ROLE usr "
                        + "JOIN AUTH_USER u ON u.ID=usr.USER_ID "
                        + "JOIN AUTH_ROLE r ON r.ID=usr.ROLE_ID "
                        + "WHERE u.USERNAME='admin' AND r.CODE='super'"));

        assertEquals(4, scalar(dataSource, "SELECT COUNT(*) FROM MODEL_AI_PLATFORM"));
        assertEquals(4, scalar(dataSource, "SELECT COUNT(*) FROM MODEL_AI_PLATFORM WHERE ADAPTER_TYPE='OPENAI_COMPATIBLE'"));
        assertEquals(9, scalar(dataSource, "SELECT COUNT(*) FROM MODEL_AI_MODEL"));
        assertEquals(0, scalar(dataSource, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME IN ('AGENT_AI_" + "PLATFORM','AGENT_AI_" + "MODEL')"));
        assertEquals(0, scalar(dataSource, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME LIKE 'RECORD_%'"));
        assertEquals(11, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_DEF"));
        assertEquals(11, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_VERSION"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM KNOWLEDGE_DIFFICULTY_SCALE"));
        assertEquals(5, scalar(dataSource, "SELECT COUNT(*) FROM KNOWLEDGE_DIFFICULTY_SCALE_LEVEL"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM KNOWLEDGE_SPACE WHERE CODE='DEMO_EDU_MATH'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM KNOWLEDGE_BASE WHERE CODE='MATH_LINEAR_EQUATION'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM VECTOR_KNOWLEDGE_CHUNK WHERE DOCUMENT_ID=1001 AND EMBEDDING_DIMENSION=1536"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_STUDENT WHERE NAME='示例学生' AND GRADE=7"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM EDU_QUESTION WHERE TAGS LIKE '%一元一次方程%'"));
        assertEquals(2, scalar(dataSource, "SELECT COUNT(*) FROM EDU_STUDY_RECORD WHERE STUDENT_ID=1"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_STUDY_PLAN WHERE STUDENT_ID=1 AND STATUS=0"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_REVIEW_TASK WHERE STUDENT_ID=1 AND STATUS=0"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_WRONG_QUESTION WHERE QUESTION_ID=1 AND STUDENT_ANSWER='C'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_LEARNING_STATE WHERE STATE='PROFICIENT'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_ACHIEVEMENT WHERE CODE='ALGEBRA_FOUNDATION'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_TEXTBOOK WHERE ISBN='9787107335661'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_TEACHER WHERE TEACHER_NO='T-2026-001'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM EDU_EXAM WHERE ID=1 AND TEACHER_ID=1 AND SUBJECT_CODE='MATH'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM EDU_COURSE WHERE ID=1 AND TEACHER_ID=1 AND TEXTBOOK_ID=1"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_COURSE_KNOWLEDGE WHERE COURSE_ID=1 AND KNOWLEDGE_ID=1001"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_RESOURCE WHERE TYPE='VIDEO' AND SUBJECT_CODE='MATH'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_RESOURCE WHERE ID=2 AND TYPE='PDF'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_RESOURCE_KNOWLEDGE WHERE RESOURCE_ID=1 AND KNOWLEDGE_ID=1001"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM AI_APP WHERE ID='edu-tutor-app' AND STATUS='PUBLISHED'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM AI_APP_VERSION WHERE ID='edu-tutor-v1' AND APP_ID='edu-tutor-app' AND CONFIG_JSON LIKE '%deepseek-v4-flash%'"));
        assertEquals(2, scalar(dataSource, "SELECT COUNT(*) FROM AI_APP WHERE STATUS='PUBLISHED'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM AI_APP_VERSION WHERE ID='knowledge-qa-v1' AND CONFIG_JSON LIKE '%rag-knowledge-agent%' AND CONFIG_JSON LIKE '%HYBRID%'"));
        assertEquals(2, scalar(dataSource,
                "SELECT COUNT(*) FROM AI_APP_VERSION WHERE STATUS='PUBLISHED' "
                        + "AND CONFIG_JSON LIKE '%module%' AND CONFIG_JSON LIKE '%platform%' "
                        + "AND CONFIG_JSON LIKE '%model%' AND CONFIG_JSON LIKE '%agentId%'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM EDU_QUESTION_KNOWLEDGE WHERE QUESTION_ID=1 AND KNOWLEDGE_ID=1001"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM CHAT_MESSAGE WHERE ROLE='ASSISTANT' AND CONTENT LIKE '%购物%'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM CHAT_CONVERSATION WHERE ID='demo-conversation-knowledge-20260908' AND SCENE_TYPE='KNOWLEDGE_QA'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM KNOWLEDGE_INGESTION_JOB WHERE JOB_KEY='demo-document-1001-v1' AND JOB_STATUS='SUCCEEDED' AND STAGE='INDEXED'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM KNOWLEDGE_AUDIT_LOG WHERE RESOURCE_ID=1001 AND ACTION='INGESTION_COMPLETED'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM KNOWLEDGE_EVALUATION_CASE WHERE SPACE_ID=1 AND EXPECTED_DOC_IDS='[1001]'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM GOVERNANCE_USAGE_RECORD WHERE ID='demo-usage-20260908-002' AND SOURCE_ID='demo-generation-knowledge-20260908-001'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM GOVERNANCE_USAGE_RECORD WHERE INPUT_TOKENS=842 AND EXT_INFO LIKE '%deepseek-v4-flash%'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM PLUGIN_MARKET_ENTRY WHERE ID='edu-calculator' AND ENABLED=FALSE"));
        assertEquals(8, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_INTENT_DEF"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_INTENT_DEF "
                        + "WHERE NAME='闲聊' AND DESCRIPTION='日常闲聊对话'"));
        assertTrue(scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_VERSION WHERE GRAPH_CONFIG LIKE '%scoreThreshold%'") > 0);
        assertTrue(scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_VERSION WHERE GRAPH_CONFIG LIKE '%spaceIds%[1]%'") >= 3);
        assertTrue(scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_VERSION WHERE GRAPH_CONFIG LIKE '%sourceTypes%DOCUMENT%'") >= 2);
        assertEquals(0, scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_VERSION WHERE GRAPH_CONFIG LIKE '%similarityThreshold%'"));

        assertInformationArchitecture(dataSource);

        assertAllNonSeedTablesEmpty(dataSource);

        execute(dataSource, "INSERT INTO GOVERNANCE_USAGE_RECORD "
                + "(ID,TENANT_ID,USER_ID,CORRELATION_ID,SOURCE_TYPE,SOURCE_ID,INPUT_TOKENS,OUTPUT_TOKENS,COST,OCCURRED_AT) "
                + "VALUES ('usage-1',1,1,'trace-1','MODEL_INVOCATION','call-42',10,2,0.01,CURRENT_TIMESTAMP)");
        assertThrows(Exception.class, () -> execute(dataSource, "INSERT INTO GOVERNANCE_USAGE_RECORD "
                + "(ID,TENANT_ID,USER_ID,CORRELATION_ID,SOURCE_TYPE,SOURCE_ID,INPUT_TOKENS,OUTPUT_TOKENS,COST,OCCURRED_AT) "
                + "VALUES ('usage-2',1,1,'trace-2','MODEL_INVOCATION','call-42',10,2,0.01,CURRENT_TIMESTAMP)"));
        assertThrows(Exception.class, () -> execute(dataSource, "INSERT INTO GOVERNANCE_USAGE_RECORD "
                + "(ID,TENANT_ID,USER_ID,CORRELATION_ID,SOURCE_TYPE,SOURCE_ID,INPUT_TOKENS,OUTPUT_TOKENS,COST,OCCURRED_AT) "
                + "VALUES ('usage-3',NULL,1,'trace-3','MODEL_INVOCATION','call-43',10,2,0.01,CURRENT_TIMESTAMP)"));

        execute(dataSource, "INSERT INTO EDU_STUDENT (ID,TENANT_ID,USER_ID,NAME,GRADE) "
                + "VALUES (101,1,88,'tenant-one-student',7)");
        execute(dataSource, "INSERT INTO EDU_STUDENT (ID,TENANT_ID,USER_ID,NAME,GRADE) "
                + "VALUES (102,2,88,'tenant-two-student',7)");
        assertThrows(Exception.class, () -> execute(dataSource,
                "INSERT INTO EDU_STUDENT (ID,TENANT_ID,USER_ID,NAME,GRADE) "
                        + "VALUES (103,1,88,'duplicate-in-tenant',7)"));
        assertThrows(Exception.class, () -> execute(dataSource,
                "INSERT INTO EDU_STUDENT (ID,TENANT_ID,USER_ID,NAME,GRADE) "
                        + "VALUES (104,NULL,99,'missing-tenant',7)"));

        initializer.initialize();
        assertEquals(18, scalar(dataSource, "SELECT COUNT(*) FROM COMMON_DICT"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_USER"));
        assertEquals(11, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_VERSION"));
    }

    @Test
    void refusesDatabaseContainingRemovedRecordTables() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);
        initializer.initialize();

        execute(dataSource, "CREATE TABLE RECORD_LEGACY_SENTINEL (ID BIGINT PRIMARY KEY)");

        IllegalStateException error = assertThrows(IllegalStateException.class, initializer::initialize);

        assertTrue(error.getMessage().contains("schema does not match baseline"));
        assertTrue(error.getMessage().contains("RECORD_LEGACY_SENTINEL"));
    }

    @Test
    void refusesOldBaselineWithoutChangingExistingData() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);
        initializer.initialize();

        execute(dataSource, "INSERT INTO COMMON_DICT (ID,DICT_TYPE,DICT_LABEL,DICT_VALUE,TENANT_ID,REMARK,CREATE_BY,UPDATE_BY) VALUES (9999,'migration-check','保留业务数据','kept',1,'must survive rejected rebuild','test','test')");
        execute(dataSource, "CREATE TABLE MEMORY_LONG_TERM_MEMORY (ID BIGINT PRIMARY KEY, CONTENT VARCHAR(255))");
        execute(dataSource, "INSERT INTO MEMORY_LONG_TERM_MEMORY VALUES (1,'legacy memory remains untouched')");
        execute(dataSource, "UPDATE COMMON_SCHEMA_BASELINE SET BASELINE_VERSION='3'");

        IllegalStateException error = assertThrows(IllegalStateException.class, initializer::initialize);

        assertTrue(error.getMessage().contains("manual rebuild required"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM COMMON_SCHEMA_BASELINE WHERE BASELINE_VERSION='3'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM COMMON_DICT WHERE ID=9999 AND DICT_VALUE='kept'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM MEMORY_LONG_TERM_MEMORY WHERE ID=1"));
    }

    @Test
    void refusesV4DatabaseWithOutdatedModelPlatformSchema() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);
        initializer.initialize();

        execute(dataSource, "ALTER TABLE MODEL_AI_PLATFORM DROP COLUMN ADAPTER_TYPE");

        IllegalStateException error = assertThrows(IllegalStateException.class, initializer::initialize);

        assertTrue(error.getMessage().contains("ADAPTER_TYPE"));
        assertTrue(error.getMessage().contains("manual rebuild required"));
    }

    @Test
    void allowsOptionalInfrastructureTablesOnSubsequentValidation() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);
        initializer.initialize();

        execute(dataSource, "CREATE TABLE SHIYU_EVENT_INBOX (EVENT_ID VARCHAR(64) PRIMARY KEY, CONSUMED_AT TIMESTAMP NOT NULL)");
        execute(dataSource, "CREATE TABLE SHIYU_EVENT_OUTBOX (EVENT_ID VARCHAR(64) PRIMARY KEY, PAYLOAD TEXT NOT NULL)");
        execute(dataSource, "CREATE TABLE SHIYU_VECTOR_ITEM (VECTOR_NAMESPACE VARCHAR(512) NOT NULL, ITEM_ID VARCHAR(255) NOT NULL)");

        assertDoesNotThrow(initializer::initialize);
    }

    @Test
    void refusesNonEmptyDatabaseWithoutBaselineMarker() throws Exception {
        DataSource dataSource = newDataSource();
        execute(dataSource, "CREATE TABLE LEGACY_TABLE(ID INTEGER PRIMARY KEY)");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> newInitializer(dataSource).initialize());

        assertTrue(error.getMessage().contains("non-empty database"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='LEGACY_TABLE'"));
    }

    @Test
    void refusesMismatchedBaselineVersion() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);
        initializer.initialize();
        execute(dataSource, "UPDATE COMMON_SCHEMA_BASELINE SET BASELINE_VERSION='999'");

        IllegalStateException error = assertThrows(IllegalStateException.class,
                initializer::initialize);

        assertTrue(error.getMessage().contains("Unsupported database baseline"));
    }

    @Test
    void doesNotDeleteObjectsAfterFailedFreshInstall() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = new DatabaseInitializer(
                Map.of("agent", dataSource), new StaticApplicationContext()) {
            @Override
            List<String> schemaResources() {
                return java.util.stream.Stream.concat(
                        super.schemaResources().stream(),
                        java.util.stream.Stream.of(
                                "classpath:db/baseline/h2/test/invalid.sql"))
                        .toList();
            }
        };

        assertThrows(RuntimeException.class, initializer::initialize);
        assertTrue(scalar(dataSource,
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES "
                        + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_TYPE='BASE TABLE'") > 0);
    }

    private DatabaseInitializer newInitializer(DataSource dataSource) {
        return new DatabaseInitializer(Map.of("agent", dataSource), new StaticApplicationContext());
    }

    private DataSource newDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:baseline_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private void assertAllNonSeedTablesEmpty(DataSource dataSource) throws Exception {
        Set<String> tables = new HashSet<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                             + "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_TYPE='BASE TABLE'")) {
            while (resultSet.next()) {
                tables.add(resultSet.getString(1));
            }
        }
        tables.remove(DatabaseInitializer.BASELINE_TABLE);
        tables.removeAll(SEEDED_TABLES);
        assertFalse(tables.isEmpty());
        for (String table : tables) {
            assertEquals(0, scalar(dataSource, "SELECT COUNT(*) FROM \"PUBLIC\".\"" + table + "\""),
                    () -> table + " should not contain demo seed data");
        }
    }

    private void assertInformationArchitecture(DataSource dataSource) throws Exception {
        assertEquals(0, scalar(dataSource,
                "SELECT COUNT(*) FROM (SELECT CODE FROM AUTH_MENU "
                        + "WHERE CODE IS NOT NULL AND CODE <> '' GROUP BY CODE HAVING COUNT(*) > 1)"));
        assertEquals(8, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE PARENT_ID IS NULL"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=2010 AND PATH='/workspace' AND REDIRECT='/workspace/chat'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=2020 AND PATH='/app-studio' AND REDIRECT='/app-studio/apps'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=2030 AND PATH='/knowledge-center' AND REDIRECT='/knowledge-center/spaces'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=2050 AND CODE='EducationCenter'"));
        assertEquals(0, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE COMPONENT LIKE '%Workspace%'"));
        assertEquals(0, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE NAME LIKE '%工作区%' OR DESCRIPTION LIKE '%工作区%'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=1 AND NAME='系统管理'"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=90 AND PARENT_ID=1"));
        assertEquals(4, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU WHERE PARENT_ID=2050"));
        assertEquals(5, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU WHERE PARENT_ID=2030"));
        assertEquals(5, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU WHERE PARENT_ID=2020"));
        assertEquals(0, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU WHERE ID IN (10,40,70,80,1500,1600,1610,1620,1630,1640)"));
        assertEquals(0, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU WHERE PATH LIKE '/agent/%' OR PATH LIKE '/knowledge/%' OR PATH LIKE '/dashboard/%'"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU WHERE ID=2023 AND COMPONENT='feature:conversation.prompts'"));
    }

    private long scalar(DataSource dataSource, String sql) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private void execute(DataSource dataSource, String sql) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}

