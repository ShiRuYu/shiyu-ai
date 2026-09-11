package com.shiyu.ai.composition.database;

import com.shiyu.ai.common.mybatis.config.DatabaseInfrastructureProperties;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

/**
 * Installs the immutable local schema baseline and validates externally managed databases.
 *
 * <p>The schema and seed resources always describe the final baseline directly. Installed databases
 * are never patched in place by this initializer.
 */
@Slf4j
@Component
public class DatabaseInitializer {

    static final String BASELINE_VERSION = "4";
    static final String SEED_PROFILE = "system-ai";
    static final String BASELINE_TABLE = "COMMON_SCHEMA_BASELINE";

    private static final List<String> DEFAULT_SCHEMA_RESOURCES =
            List.of(
                    "classpath:db/baseline/h2/schema/application/00_baseline.sql",
                    "classpath:db/baseline/h2/schema/storage/01_storage.sql",
                    "classpath:db/baseline/h2/schema/common/02_common.sql",
                    "classpath:db/baseline/h2/schema/iam/03_auth.sql",
                    "classpath:db/baseline/h2/schema/agent/04_agent.sql",
                    "classpath:db/baseline/h2/schema/model/04_model.sql",
                    "classpath:db/baseline/h2/schema/governance/05_governance.sql",
                    "classpath:db/baseline/h2/schema/knowledge/06_knowledge.sql",
                    "classpath:db/baseline/h2/schema/education/07_education.sql",
                    "classpath:db/baseline/h2/schema/knowledge/09_vector.sql",
                    "classpath:db/baseline/h2/schema/governance/10_observation.sql",
                    "classpath:db/baseline/h2/schema/conversation/11_conversation.sql",
                    "classpath:db/baseline/h2/schema/memory/12_memory_magma.sql",
                    "classpath:db/baseline/h2/schema/tooling/15_plugin_market.sql",
                    "classpath:db/baseline/h2/schema/agent/16_ai_runtime.sql");

    private static final List<String> DEFAULT_SEED_RESOURCES =
            List.of(
                    "classpath:db/baseline/h2/seed/common/01_common.sql",
                    "classpath:db/baseline/h2/seed/iam/02_auth.sql",
                    "classpath:db/baseline/h2/seed/agent/03_agent.sql",
                    "classpath:db/baseline/h2/seed/agent/04_app_runtime.sql",
                    "classpath:db/baseline/h2/seed/model/03_model.sql",
                    "classpath:db/baseline/h2/seed/knowledge/04_knowledge.sql",
                    "classpath:db/baseline/h2/seed/knowledge/06_demo_content.sql",
                    "classpath:db/baseline/h2/seed/iam/05_navigation.sql",
                    "classpath:db/baseline/h2/seed/education/07_education.sql",
                    "classpath:db/baseline/h2/seed/education/08_learning_progress.sql",
                    "classpath:db/baseline/h2/seed/education/09_curriculum.sql",
                    "classpath:db/baseline/h2/seed/education/10_resources.sql",
                    "classpath:db/baseline/h2/seed/education/11_resource_variants.sql",
                    "classpath:db/baseline/h2/seed/conversation/11_conversation.sql",
                    "classpath:db/baseline/h2/seed/governance/10_governance.sql",
                    "classpath:db/baseline/h2/seed/memory/12_memory.sql",
                    "classpath:db/baseline/h2/seed/tooling/15_plugin_market.sql");

    private static final Set<String> EXPECTED_TABLES =
            Set.of(
                    BASELINE_TABLE,
                    "MODEL_AI_MODEL",
                    "MODEL_AI_PLATFORM",
                    "AGENT_CHECKPOINT",
                    "AGENT_DEF",
                    "AGENT_EXECUTION",
                    "AGENT_INTENT_DEF",
                    "AGENT_NODE_EXECUTION",
                    "AGENT_VERSION",
                    "GOVERNANCE_USAGE_RECORD",
                    "AUTH_AUTH_CODE",
                    "AUTH_MENU",
                    "AUTH_ROLE",
                    "AUTH_ROLE_SCOPE_AUTH_CODE",
                    "AUTH_ROLE_SCOPE_MENU",
                    "AUTH_TENANT",
                    "AUTH_TENANT_AUTH_CODE",
                    "AUTH_TENANT_MENU",
                    "AUTH_USER",
                    "AUTH_USER_SCOPE_ROLE",
                    "COMMON_DICT",
                    "EDU_ABILITY",
                    "EDU_ACHIEVEMENT",
                    "EDU_CHAPTER",
                    "EDU_COURSE",
                    "EDU_COURSE_CHAPTER",
                    "EDU_COURSE_KNOWLEDGE",
                    "EDU_COURSE_SECTION",
                    "EDU_EXAM",
                    "EDU_EXAM_QUESTION",
                    "EDU_EXAM_SECTION",
                    "EDU_KNOWLEDGE_TEXTBOOK",
                    "EDU_LEARNING_STATE",
                    "EDU_QUESTION",
                    "EDU_QUESTION_KNOWLEDGE",
                    "EDU_RESOURCE",
                    "EDU_RESOURCE_KNOWLEDGE",
                    "EDU_REVIEW_TASK",
                    "EDU_STUDENT",
                    "EDU_STUDY_PLAN",
                    "EDU_STUDY_PLAN_ITEM",
                    "EDU_STUDY_RECORD",
                    "EDU_SUBJECT",
                    "EDU_TEACHER",
                    "EDU_TEXTBOOK",
                    "EDU_WRONG_QUESTION",
                    "KNOWLEDGE_AUDIT_LOG",
                    "KNOWLEDGE_BASE",
                    "KNOWLEDGE_DIFFICULTY_SCALE",
                    "KNOWLEDGE_DIFFICULTY_SCALE_LEVEL",
                    "KNOWLEDGE_DOCUMENT",
                    "KNOWLEDGE_DOCUMENT_RELATION",
                    "KNOWLEDGE_DOCUMENT_VERSION",
                    "KNOWLEDGE_DOC_RELATION",
                    "KNOWLEDGE_EVALUATION_CASE",
                    "KNOWLEDGE_INGESTION_JOB",
                    "KNOWLEDGE_RELATION",
                    "KNOWLEDGE_REVIEW_RECORD",
                    "KNOWLEDGE_SPACE",
                    "KNOWLEDGE_SPACE_MEMBER",
                    "CHAT_CONVERSATION",
                    "CHAT_MESSAGE",
                    "CHAT_GENERATION_RUN",
                    "CHAT_GENERATION_ACTIVE",
                    "CHAT_IDEMPOTENCY_KEY",
                    "CHAT_CHARACTER_ASSET",
                    "CHAT_PERSONA_ASSET",
                    "CHAT_LOREBOOK_ASSET",
                    "CHAT_PROMPT_TEMPLATE",
                    "CHAT_GROUP_CHAT",
                    "PLUGIN_MARKET_ENTRY",
                    "AI_APP",
                    "AI_APP_VERSION",
                    "AI_RUN",
                    "AI_RUN_EVENT",
                    "AI_TOOL_APPROVAL",
                    "AGENT_EVAL_DATASET",
                    "AGENT_EVAL_CASE",
                    "AGENT_EVAL_RUN",
                    "MEMORY_EVENT",
                    "MEMORY_ENTITY",
                    "MEMORY_EDGE",
                    "MEMORY_CONSOLIDATION_JOB",
                    "MEMORY_RETRIEVAL_TRACE",
                    "OBSERVATION_AUDIT_LOG",
                    "OBSERVATION_EXECUTION_TIMELINE",
                    "STORAGE_OBJECT",
                    "STORAGE_UPLOAD_CHUNK",
                    "STORAGE_UPLOAD_SESSION",
                    "VECTOR_KNOWLEDGE_CHUNK");

    /** Tables created by optional infrastructure adapters, outside the application baseline. */
    private static final Set<String> INFRASTRUCTURE_TABLES =
            Set.of("SHIYU_VECTOR_ITEM", "SHIYU_EVENT_OUTBOX", "SHIYU_EVENT_INBOX");

    private final Map<String, DataSource> dataSources;
    private final PathMatchingResourcePatternResolver resourceResolver;
    private final DatabaseInfrastructureProperties databaseProperties;

    public DatabaseInitializer(
            Map<String, DataSource> dataSources, ApplicationContext applicationContext) {
        this(dataSources, applicationContext, new DatabaseInfrastructureProperties());
    }

    @Autowired
    public DatabaseInitializer(
            Map<String, DataSource> dataSources,
            ApplicationContext applicationContext,
            DatabaseInfrastructureProperties databaseProperties) {
        this.dataSources = dataSources;
        this.resourceResolver = new PathMatchingResourcePatternResolver(applicationContext);
        this.databaseProperties = databaseProperties;
    }

    @PostConstruct
    public void initialize() {
        DataSource dataSource = resolveDataSource();
        try (Connection connection = dataSource.getConnection()) {
            databaseProperties.validate();
            String productName = connection.getMetaData().getDatabaseProductName();
            validateProvider(productName);
            if (!"H2".equalsIgnoreCase(productName)) {
                validateExternalDatabase(connection, productName);
                return;
            }
            Set<String> existingTables = loadPublicTables(connection);

            Set<String> legacyModelTables = new TreeSet<>(existingTables);
            legacyModelTables.retainAll(Set.of("AGENT_AI_" + "PLATFORM", "AGENT_AI_" + "MODEL"));
            if (!legacyModelTables.isEmpty()) {
                throw new IllegalStateException(
                        "Legacy model tables detected: "
                                + legacyModelTables
                                + "; manual rebuild required for baseline "
                                + BASELINE_VERSION);
            }

            if (existingTables.contains(BASELINE_TABLE)) {
                BaselineMarker marker = readBaselineMarker(connection);
                if (BASELINE_VERSION.equals(marker.version())) {
                    assertExpectedTables(loadPublicTables(connection));
                    assertExpectedColumns(connection);
                    log.info(
                            "Database baseline {} ({}) is already installed; initialization"
                                    + " skipped",
                            BASELINE_VERSION,
                            SEED_PROFILE);
                    return;
                }
                throw unsupportedBaseline(marker);
            }
            if (!existingTables.isEmpty()) {
                throw new IllegalStateException(
                        "Refusing to initialize a non-empty database without "
                                + BASELINE_TABLE
                                + "; existing tables="
                                + new TreeSet<>(existingTables));
            }

            installFreshBaseline(connection);
            log.info(
                    "Database baseline {} ({}) installed successfully: {} application tables",
                    BASELINE_VERSION,
                    SEED_PROFILE,
                    EXPECTED_TABLES.size() - 1);
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
            throw new IllegalStateException(
                    "No DataSource is available for database initialization");
        }
        return dataSource;
    }

    private void validateProvider(String productName) {
        String provider = databaseProperties.normalizedProvider();
        boolean matches =
                switch (provider) {
                    case "h2" -> "H2".equalsIgnoreCase(productName);
                    case "mysql" -> productName.toLowerCase(Locale.ROOT).contains("mysql");
                    case "postgresql" ->
                            productName.toLowerCase(Locale.ROOT).contains("postgresql");
                    default -> false;
                };
        if (!matches) {
            throw new IllegalStateException(
                    "Database provider mismatch: configured="
                            + provider
                            + ", actual="
                            + productName);
        }
    }

    private void validateExternalDatabase(Connection connection, String productName)
            throws Exception {
        Set<String> existingTables = loadPublicTables(connection);
        if (!existingTables.contains(BASELINE_TABLE)) {
            throw new IllegalStateException(
                    "External database "
                            + productName
                            + " requires a pre-provisioned schema baseline marked by "
                            + BASELINE_TABLE);
        }
        BaselineMarker marker = readBaselineMarker(connection);
        if (!BASELINE_VERSION.equals(marker.version())
                || !SEED_PROFILE.equals(marker.seedProfile())) {
            throw unsupportedBaseline(marker);
        }
        assertExpectedTables(existingTables);
        assertExpectedColumns(connection);
        log.info(
                "External database baseline {} ({}) validated: {} application tables",
                BASELINE_VERSION,
                SEED_PROFILE,
                EXPECTED_TABLES.size() - 1);
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
            throw initializationFailure;
        } finally {
            if (!connection.isClosed()) {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private void executeResources(Connection connection, List<String> locations, String phase) {
        for (String location : locations) {
            Resource resource = resourceResolver.getResource(location);
            if (!resource.exists()) {
                throw new IllegalStateException(
                        "Missing database " + phase + " resource: " + location);
            }
            log.info("Executing database {} resource: {}", phase, location);
            ScriptUtils.executeSqlScript(
                    connection, new EncodedResource(resource, StandardCharsets.UTF_8));
        }
    }

    private void insertBaselineMarker(Connection connection) throws Exception {
        try (PreparedStatement statement =
                connection.prepareStatement(
                        "INSERT INTO COMMON_SCHEMA_BASELINE "
                                + "(ID, BASELINE_VERSION, SEED_PROFILE) VALUES (1, ?, ?)")) {
            statement.setString(1, BASELINE_VERSION);
            statement.setString(2, SEED_PROFILE);
            statement.executeUpdate();
        }
    }

    private BaselineMarker readBaselineMarker(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement();
                ResultSet resultSet =
                        statement.executeQuery(
                                "SELECT BASELINE_VERSION, SEED_PROFILE FROM"
                                        + " COMMON_SCHEMA_BASELINE")) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Database baseline marker table is empty");
            }
            String version = resultSet.getString(1);
            String profile = resultSet.getString(2);
            if (resultSet.next()) {
                throw new IllegalStateException(
                        "Database baseline marker must contain exactly one row");
            }
            return new BaselineMarker(version, profile);
        }
    }

    private IllegalStateException unsupportedBaseline(BaselineMarker marker) {
        return new IllegalStateException(
                "Unsupported database baseline: version="
                        + marker.version()
                        + ", seedProfile="
                        + marker.seedProfile()
                        + "; expected version="
                        + BASELINE_VERSION
                        + ", seedProfile="
                        + SEED_PROFILE
                        + "; manual rebuild required");
    }

    private Set<String> loadPublicTables(Connection connection) throws Exception {
        Set<String> tables = new HashSet<>();
        String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        String catalog = connection.getCatalog();
        try (ResultSet resultSet =
                connection.getMetaData().getTables(null, null, "%", new String[] {"TABLE"})) {
            while (resultSet.next()) {
                String schema = resultSet.getString("TABLE_SCHEM");
                if (isApplicationSchema(product, schema, catalog)) {
                    tables.add(resultSet.getString("TABLE_NAME").toUpperCase());
                }
            }
        }
        return tables;
    }

    private boolean isApplicationSchema(String product, String schema, String catalog) {
        if (product.contains("h2")) {
            return "PUBLIC".equalsIgnoreCase(schema);
        }
        if (product.contains("postgresql")) {
            return "public".equalsIgnoreCase(schema);
        }
        if (product.contains("mysql")) {
            return schema == null
                    || schema.isBlank()
                    || (catalog != null && catalog.equalsIgnoreCase(schema));
        }
        return schema == null || schema.isBlank();
    }

    private void assertExpectedTables(Set<String> actualTables) {
        Set<String> missing = new TreeSet<>(EXPECTED_TABLES);
        missing.removeAll(actualTables);
        Set<String> unexpected = new TreeSet<>(actualTables);
        unexpected.removeAll(EXPECTED_TABLES);
        unexpected.removeAll(INFRASTRUCTURE_TABLES);
        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            throw new IllegalStateException(
                    "Database schema does not match baseline; missing="
                            + missing
                            + ", unexpected="
                            + unexpected);
        }
    }

    private void assertExpectedColumns(Connection connection) throws Exception {
        boolean found = false;
        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, "%", "%")) {
            while (resultSet.next()) {
                String table = resultSet.getString("TABLE_NAME");
                String column = resultSet.getString("COLUMN_NAME");
                if ("MODEL_AI_PLATFORM".equalsIgnoreCase(table)
                        && "ADAPTER_TYPE".equalsIgnoreCase(column)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new IllegalStateException(
                    "Database schema does not match baseline; "
                            + "missing=[MODEL_AI_PLATFORM.ADAPTER_TYPE]; manual rebuild required");
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

    private record BaselineMarker(String version, String seedProfile) {}
}
