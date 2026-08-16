package com.shiyu.ai.dal.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Installs the immutable H2 schema and system-ai seed baseline.
 *
 * <p>The schema and seed resources always describe the final baseline directly. Installed
 * databases are never patched in place by this initializer.</p>
 */
@Slf4j
@Component
public class DatabaseInitializer {

    static final String BASELINE_VERSION = "3";
    static final String SEED_PROFILE = "system-ai";
    static final String BASELINE_TABLE = "COMMON_SCHEMA_BASELINE";

    private static final List<String> DEFAULT_SCHEMA_RESOURCES = List.of(
            "classpath:db/baseline/h2/schema/00_baseline.sql",
            "classpath:db/baseline/h2/schema/01_storage.sql",
            "classpath:db/baseline/h2/schema/02_common.sql",
            "classpath:db/baseline/h2/schema/03_auth.sql",
            "classpath:db/baseline/h2/schema/04_agent.sql",
            "classpath:db/baseline/h2/schema/06_knowledge.sql",
            "classpath:db/baseline/h2/schema/07_education.sql",
            "classpath:db/baseline/h2/schema/08_record.sql",
            "classpath:db/baseline/h2/schema/09_vector.sql",
            "classpath:db/baseline/h2/schema/10_observation.sql",
            "classpath:db/baseline/h2/schema/11_conversation.sql",
            "classpath:db/baseline/h2/schema/12_memory_magma.sql",
            "classpath:db/baseline/h2/schema/13_chat_product.sql",
            "classpath:db/baseline/h2/schema/14_chat_group.sql",
            "classpath:db/baseline/h2/schema/15_plugin_market.sql"
            ,"classpath:db/baseline/h2/schema/16_ai_runtime.sql"
            ,"classpath:db/baseline/h2/schema/17_ai_evaluation.sql"
    );

    private static final List<String> DEFAULT_SEED_RESOURCES = List.of(
            "classpath:db/baseline/h2/seed/01_common.sql",
            "classpath:db/baseline/h2/seed/02_auth.sql",
            "classpath:db/baseline/h2/seed/03_agent.sql",
            "classpath:db/baseline/h2/seed/04_knowledge.sql"
    );

    private static final Set<String> EXPECTED_TABLES = Set.of(
            BASELINE_TABLE,
            "AGENT_AI_MODEL", "AGENT_AI_PLATFORM", "AGENT_CHECKPOINT", "AGENT_DEF",
            "AGENT_EXECUTION", "AGENT_INTENT_DEF", "AGENT_NODE_EXECUTION",
            "AGENT_USAGE_RECORD", "AGENT_VERSION",
            "AUTH_AUTH_CODE", "AUTH_MENU", "AUTH_ROLE", "AUTH_ROLE_SCOPE_AUTH_CODE",
            "AUTH_ROLE_SCOPE_MENU", "AUTH_TENANT", "AUTH_TENANT_AUTH_CODE",
            "AUTH_TENANT_MENU", "AUTH_TENANT_QUOTA", "AUTH_USER", "AUTH_USER_SCOPE_ROLE",
            "COMMON_DICT",
            "EDU_ABILITY", "EDU_ACHIEVEMENT", "EDU_CHAPTER", "EDU_COURSE",
            "EDU_COURSE_CHAPTER", "EDU_COURSE_KNOWLEDGE", "EDU_COURSE_SECTION",
            "EDU_EXAM", "EDU_EXAM_QUESTION", "EDU_EXAM_SECTION",
            "EDU_KNOWLEDGE_TEXTBOOK", "EDU_LEARNING_STATE", "EDU_QUESTION",
            "EDU_QUESTION_KNOWLEDGE", "EDU_RESOURCE", "EDU_RESOURCE_KNOWLEDGE",
            "EDU_REVIEW_TASK", "EDU_STUDENT", "EDU_STUDY_PLAN", "EDU_STUDY_PLAN_ITEM",
            "EDU_STUDY_RECORD", "EDU_SUBJECT", "EDU_TEACHER", "EDU_TEXTBOOK",
            "EDU_WRONG_QUESTION",
            "KNOWLEDGE_AUDIT_LOG", "KNOWLEDGE_BASE", "KNOWLEDGE_DIFFICULTY_SCALE",
            "KNOWLEDGE_DIFFICULTY_SCALE_LEVEL", "KNOWLEDGE_DOCUMENT",
            "KNOWLEDGE_DOCUMENT_RELATION", "KNOWLEDGE_DOCUMENT_VERSION",
            "KNOWLEDGE_DOC_RELATION", "KNOWLEDGE_EVALUATION_CASE",
            "KNOWLEDGE_INGESTION_JOB", "KNOWLEDGE_RELATION", "KNOWLEDGE_REVIEW_RECORD",
            "KNOWLEDGE_SPACE", "KNOWLEDGE_SPACE_MEMBER",
            "CHAT_CONVERSATION", "CHAT_MESSAGE", "CHAT_GENERATION_RUN", "CHAT_GENERATION_EVENT", "CHAT_IDEMPOTENCY_KEY",
            "CHAT_CHARACTER_ASSET", "CHAT_PERSONA_ASSET", "CHAT_LOREBOOK_ASSET", "CHAT_PROMPT_TEMPLATE", "CHAT_GROUP_CHAT",
            "PLUGIN_MARKET_ENTRY",
            "AI_APP", "AI_APP_VERSION", "AI_RUN", "AI_RUN_EVENT", "AI_TOOL_APPROVAL",
            "AI_EVAL_DATASET", "AI_EVAL_CASE", "AI_EVAL_RUN",
            "MEMORY_EVENT", "MEMORY_ENTITY", "MEMORY_EDGE", "MEMORY_CONSOLIDATION_JOB", "MEMORY_RETRIEVAL_TRACE",
            "OBSERVATION_AUDIT_LOG", "OBSERVATION_EXECUTION_TIMELINE",
            "RECORD_ENTRY", "RECORD_MEDIA", "RECORD_PROFILE", "RECORD_PROFILE_MEMBER",
            "RECORD_RECORD_TAG", "RECORD_TAG", "RECORD_TIMELINE_EVENT",
            "STORAGE_OBJECT", "STORAGE_UPLOAD_CHUNK", "STORAGE_UPLOAD_SESSION",
            "VECTOR_KNOWLEDGE_CHUNK"
    );

    private final Map<String, DataSource> dataSources;
    private final PathMatchingResourcePatternResolver resourceResolver;

    public DatabaseInitializer(Map<String, DataSource> dataSources, ApplicationContext applicationContext) {
        this.dataSources = dataSources;
        this.resourceResolver = new PathMatchingResourcePatternResolver(applicationContext);
    }

    @PostConstruct
    public void initialize() {
        DataSource dataSource = resolveDataSource();
        try (Connection connection = dataSource.getConnection()) {
            validateH2(connection);
            Set<String> existingTables = loadPublicTables(connection);

            if (existingTables.contains(BASELINE_TABLE)) {
                BaselineMarker marker = readBaselineMarker(connection);
                if (BASELINE_VERSION.equals(marker.version())) {
                    // v3 is the final baseline, but additive platform catalogs may be introduced
                    // after the original v3 rollout. Install only missing additive tables.
                    // Re-run the idempotent conversation DDL so columns added after the
                    // original v3 rollout (for example group speaker binding) are patched too.
                    executeResources(connection, List.of("classpath:db/baseline/h2/schema/11_conversation.sql"), "additive-v3");
                    if (!existingTables.contains("PLUGIN_MARKET_ENTRY")) {
                        executeResources(connection, List.of("classpath:db/baseline/h2/schema/15_plugin_market.sql"), "additive-v3");
                    }
                    executeResources(connection, List.of("classpath:db/baseline/h2/schema/16_ai_runtime.sql", "classpath:db/baseline/h2/schema/17_ai_evaluation.sql"), "additive-v3");
                    migratePluginMarketKey(connection);
                    assertExpectedTables(loadPublicTables(connection));
                    log.info("Database baseline {} ({}) is already installed; initialization skipped",
                            BASELINE_VERSION, SEED_PROFILE);
                    return;
                }
                if ("2".equals(marker.version())) {
                    migrateV2ToV3(connection);
                    log.info("Database baseline migrated from 2 to 3");
                    return;
                }
                throw unsupportedBaseline(marker);
            }
            if (!existingTables.isEmpty()) {
                throw new IllegalStateException("Refusing to initialize a non-empty database without "
                        + BASELINE_TABLE + "; existing tables=" + new TreeSet<>(existingTables));
            }

            installFreshBaseline(connection);
            log.info("Database baseline {} ({}) installed successfully: {} application tables",
                    BASELINE_VERSION, SEED_PROFILE, EXPECTED_TABLES.size() - 1);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Database baseline initialization failed", e);
        }
    }

    List<String> schemaResources() {
        return DEFAULT_SCHEMA_RESOURCES;
    }

    List<String> seedResources() {
        return DEFAULT_SEED_RESOURCES;
    }

    private DataSource resolveDataSource() {
        DataSource dataSource = dataSources.get("agent");
        if (dataSource == null) {
            dataSource = dataSources.get("agentDataSource");
        }
        if (dataSource == null && !dataSources.isEmpty()) {
            dataSource = dataSources.values().iterator().next();
        }
        if (dataSource == null) {
            throw new IllegalStateException("No DataSource is available for database initialization");
        }
        return dataSource;
    }

    private void validateH2(Connection connection) throws Exception {
        String productName = connection.getMetaData().getDatabaseProductName();
        if (!"H2".equalsIgnoreCase(productName)) {
            throw new IllegalStateException("Unsupported database " + productName
                    + "; this baseline supports H2 only");
        }
    }

    private void installFreshBaseline(Connection connection) throws Exception {
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(true);
            executeResources(connection, schemaResources(), "schema");
            assertExpectedTables(loadPublicTables(connection));

            connection.setAutoCommit(false);
            executeResources(connection, seedResources(), "seed");
            insertBaselineMarker(connection);
            connection.commit();
        } catch (Exception initializationFailure) {
            rollbackQuietly(connection, initializationFailure);
            cleanupFailedFreshInstall(connection, initializationFailure);
            throw initializationFailure;
        } finally {
            if (!connection.isClosed()) {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void migrateV2ToV3(Connection connection) throws Exception {
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute("BACKUP TO 'data/db/backup-baseline-v2.zip'");
            } catch (Exception backupFailure) {
                log.warn("Unable to create automatic H2 baseline backup; continue with explicit operator backup", backupFailure);
            }
            executeResources(connection, List.of(
                    "classpath:db/baseline/h2/schema/11_conversation.sql",
                    "classpath:db/baseline/h2/schema/12_memory_magma.sql",
                    "classpath:db/baseline/h2/schema/13_chat_product.sql", "classpath:db/baseline/h2/schema/14_chat_group.sql",
                    "classpath:db/baseline/h2/schema/15_plugin_market.sql",
                    "classpath:db/baseline/h2/schema/16_ai_runtime.sql",
                    "classpath:db/baseline/h2/schema/17_ai_evaluation.sql"), "migration");
            migratePluginMarketKey(connection);
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS MEMORY_CONVERSATION_MESSAGE");
                statement.execute("DROP TABLE IF EXISTS MEMORY_LONG_TERM_MEMORY");
                statement.execute("DROP TABLE IF EXISTS MEMORY_EPISODIC_MEMORY");
                statement.execute("UPDATE COMMON_SCHEMA_BASELINE SET BASELINE_VERSION = '3', SEED_PROFILE = 'system-ai' WHERE ID = 1");
            }
        } finally {
            if (!connection.isClosed()) connection.setAutoCommit(originalAutoCommit);
        }
        assertExpectedTables(loadPublicTables(connection));
    }

    /** Upgrade the first plugin catalog shape (ID-only key) to immutable ID+VERSION entries. */
    private void migratePluginMarketKey(Connection connection) throws Exception {
        if (!loadPublicTables(connection).contains("PLUGIN_MARKET_ENTRY")) return;
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE PLUGIN_MARKET_ENTRY DROP CONSTRAINT IF EXISTS PK_PLUGIN_MARKET_ENTRY");
            statement.execute("ALTER TABLE PLUGIN_MARKET_ENTRY ADD CONSTRAINT PK_PLUGIN_MARKET_ENTRY PRIMARY KEY (ID, VERSION)");
        }
    }

    private void executeResources(Connection connection, List<String> locations, String phase) {
        for (String location : locations) {
            Resource resource = resourceResolver.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException("Missing database " + phase + " resource: " + location);
            }
            log.info("Executing database {} resource: {}", phase, location);
            ScriptUtils.executeSqlScript(
                    connection,
                    new EncodedResource(resource, StandardCharsets.UTF_8));
        }
    }

    private void insertBaselineMarker(Connection connection) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO COMMON_SCHEMA_BASELINE "
                        + "(ID, BASELINE_VERSION, SEED_PROFILE) VALUES (1, ?, ?)")) {
            statement.setString(1, BASELINE_VERSION);
            statement.setString(2, SEED_PROFILE);
            statement.executeUpdate();
        }
    }

    private BaselineMarker readBaselineMarker(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT BASELINE_VERSION, SEED_PROFILE FROM COMMON_SCHEMA_BASELINE")) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Database baseline marker table is empty");
            }
            String version = resultSet.getString(1);
            String profile = resultSet.getString(2);
            if (resultSet.next()) {
                throw new IllegalStateException("Database baseline marker must contain exactly one row");
            }
            return new BaselineMarker(version, profile);
        }
    }

    private IllegalStateException unsupportedBaseline(BaselineMarker marker) {
        return new IllegalStateException("Unsupported database baseline: version=" + marker.version()
                + ", seedProfile=" + marker.seedProfile() + "; expected version=" + BASELINE_VERSION
                + ", seedProfile=" + SEED_PROFILE);
    }

    private Set<String> loadPublicTables(Connection connection) throws Exception {
        Set<String> tables = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES "
                        + "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE'");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tables.add(resultSet.getString(1).toUpperCase());
            }
        }
        return tables;
    }

    private void assertExpectedTables(Set<String> actualTables) {
        Set<String> missing = new TreeSet<>(EXPECTED_TABLES);
        missing.removeAll(actualTables);
        Set<String> unexpected = new TreeSet<>(actualTables);
        unexpected.removeAll(EXPECTED_TABLES);
        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            throw new IllegalStateException("Database schema does not match baseline; missing=" + missing
                    + ", unexpected=" + unexpected);
        }
    }

    private void rollbackQuietly(Connection connection, Exception initializationFailure) {
        try {
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
        } catch (Exception rollbackFailure) {
            initializationFailure.addSuppressed(rollbackFailure);
        }
    }

    private void cleanupFailedFreshInstall(Connection connection, Exception initializationFailure) {
        try {
            connection.setAutoCommit(true);
            try (Statement statement = connection.createStatement()) {
                statement.execute("DROP ALL OBJECTS");
            }
            log.warn("Removed all objects created by the failed fresh database initialization");
        } catch (Exception cleanupFailure) {
            initializationFailure.addSuppressed(cleanupFailure);
            log.error("Failed to clean up objects created by fresh database initialization", cleanupFailure);
        }
    }

    private record BaselineMarker(String version, String seedProfile) {
    }
}
