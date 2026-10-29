package com.shiyu.ai.composition.database;

import com.shiyu.ai.common.mybatis.config.DatabaseInfrastructureProperties;
import com.shiyu.ai.common.foundation.database.DatabaseBaselineContributor;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.sql.DataSource;

/**
 * 实现 数据库 Initializer 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
@Component
public class DatabaseInitializer {

    /**
     * 版本，表示当前对象中的对应属性。
     */
    static final String BASELINE_VERSION = "4";
    /**
     * 配置档案，表示当前对象中的对应属性。
     */
    static final String SEED_PROFILE = "system-ai";
    /**
     * BASELINE_TABLE 属性，保存当前对象中的业务数据或协作依赖。
     */
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
                    "classpath:db/baseline/h2/seed/conversation/11_conversation.sql",
                    "classpath:db/baseline/h2/seed/governance/10_governance.sql",
                    "classpath:db/baseline/h2/seed/memory/12_memory.sql",
                    "classpath:db/baseline/h2/seed/tooling/15_plugin_market.sql");

    private static final Set<String> PLATFORM_EXPECTED_TABLES =
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
                    "GOVERNANCE_QUOTA_TENANT",
                    "GOVERNANCE_QUOTA_RESERVATION",
                    "CHAT_GENERATION_POSTPROCESSING",
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

    /** INFRASTRUCTURE_TABLES 字段，保存tables。 */
    private static final Set<String> INFRASTRUCTURE_TABLES =
            Set.of("SHIYU_VECTOR_ITEM", "SHIYU_EVENT_OUTBOX", "SHIYU_EVENT_INBOX");

    /** 由幂等增补脚本创建、但不属于破坏式 baseline 的表。 */
    private static final Set<String> UPDATE_TABLES =
            Set.of(
                    "AUTH_TENANT_MODULE",
                    "MODEL_ROUTE_POLICY",
                    "GOVERNANCE_QUOTA_TENANT",
                    "GOVERNANCE_QUOTA_RESERVATION",
                    "CHAT_GENERATION_POSTPROCESSING",
                    "CHAT_IDEMPOTENCY_KEY");

    /**
     * dataSources 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Map<String, DataSource> dataSources;
    /**
     * 资源解析器，表示当前对象中的对应属性。
     */
    private final PathMatchingResourcePatternResolver resourceResolver;
    /**
     * 数据库配置属性，表示当前对象中的对应属性。
     */
    private final DatabaseInfrastructureProperties databaseProperties;
    /**
     * contributors 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final List<DatabaseBaselineContributor> contributors;

    /**
     * 执行 数据库 Initializer 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSources 用于完成本次业务处理的 dataSources 参数。
     * @param applicationContext 用于完成本次业务处理的 applicationContext 参数。
     */
    public DatabaseInitializer(
            Map<String, DataSource> dataSources, ApplicationContext applicationContext) {
        this(
                dataSources,
                applicationContext,
                new DatabaseInfrastructureProperties(),
                List.of());
    }

    /**
     * 执行 数据库 Initializer 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSources 用于完成本次业务处理的 dataSources 参数。
     * @param applicationContext 用于完成本次业务处理的 applicationContext 参数。
     * @param databaseProperties 用于完成本次业务处理的 databaseProperties 参数。
     */
    public DatabaseInitializer(
            Map<String, DataSource> dataSources,
            ApplicationContext applicationContext,
            DatabaseInfrastructureProperties databaseProperties) {
        this(dataSources, applicationContext, databaseProperties, List.of());
    }

    /**
     * 执行 数据库 Initializer 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSources 用于完成本次业务处理的 dataSources 参数。
     * @param applicationContext 用于完成本次业务处理的 applicationContext 参数。
     * @param databaseProperties 用于完成本次业务处理的 databaseProperties 参数。
     * @param contributors 用于完成本次业务处理的 contributors 参数。
     */
    @Autowired
    public DatabaseInitializer(
            Map<String, DataSource> dataSources,
            ApplicationContext applicationContext,
            DatabaseInfrastructureProperties databaseProperties,
            List<DatabaseBaselineContributor> contributors) {
        this.dataSources = dataSources;
        this.resourceResolver = new PathMatchingResourcePatternResolver(applicationContext);
        this.databaseProperties = databaseProperties;
        this.contributors = contributors == null ? List.of() : List.copyOf(contributors);
    }

    /**
     * 执行 数据库 Initializer 相关业务操作，并维护必要的状态和协作关系。
     */
    @PostConstruct
    public void initialize() {
        DataSource dataSource = resolveDataSource();
        try (Connection connection = dataSource.getConnection()) {
            databaseProperties.validate();
            String productName = connection.getMetaData().getDatabaseProductName();
            validateProvider(productName);
            if (productName.toLowerCase(Locale.ROOT).contains("postgresql")) {
                initializePostgresql(connection);
                return;
            }
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
                if (BASELINE_VERSION.equals(marker.version())
                        && SEED_PROFILE.equals(marker.seedProfile())) {
                    ensureEducationBaselinePresent(existingTables);
                    installPermissionUpdates(connection);
                    assertExpectedTables(loadPublicTables(connection));
                    assertExpectedColumns(connection);
                    log.info(
                            "Database baseline {} ({}) is already installed; initialization"
                                    + " skipped; permission updates checked",
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
            installPermissionUpdates(connection);
            assertExpectedTables(loadPublicTables(connection));
            assertExpectedColumns(connection);
            log.info(
                    "Database baseline {} ({}) installed successfully: {} application tables",
                    BASELINE_VERSION,
                    SEED_PROFILE,
                    expectedTables().size() - 1);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Database baseline initialization failed", e);
        }
    }

    List<String> schemaResources() {
        return mergeResources(DEFAULT_SCHEMA_RESOURCES, DatabaseBaselineContributor::schemaResources);
    }

    List<String> seedResources() {
        return mergeResources(DEFAULT_SEED_RESOURCES, DatabaseBaselineContributor::seedResources);
    }

    Set<String> expectedTables() {
        Set<String> expected = new java.util.LinkedHashSet<>(PLATFORM_EXPECTED_TABLES);
        contributors.stream()
                .sorted(Comparator.comparingInt(DatabaseBaselineContributor::order))
                .map(DatabaseBaselineContributor::expectedTables)
                .filter(java.util.Objects::nonNull)
                .forEach(expected::addAll);
        return Set.copyOf(expected);
    }

    private List<String> mergeResources(
            List<String> platformResources,
            java.util.function.Function<DatabaseBaselineContributor, Collection<String>> extractor) {
        List<String> resources = new ArrayList<>(platformResources);
        contributors.stream()
                .sorted(Comparator.comparingInt(DatabaseBaselineContributor::order))
                .map(extractor)
                .filter(java.util.Objects::nonNull)
                .forEach(resources::addAll);
        return List.copyOf(resources);
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

    private void initializePostgresql(Connection connection) throws Exception {
        boolean originalAutoCommit = connection.getAutoCommit();
        int originalIsolation = connection.getTransactionIsolation();
        try {
            if (originalIsolation != Connection.TRANSACTION_READ_COMMITTED) {
                connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute("SET LOCAL search_path TO public");
                statement.execute("SELECT pg_advisory_xact_lock(746821044, 4)");
            }

            Set<String> existingTables = loadPublicTables(connection);
            boolean freshInstall = existingTables.isEmpty();
            if (freshInstall) {
                executePostgresqlResource(
                        connection,
                        "classpath:db/baseline/postgresql/schema.sql",
                        "schema");
                if (hasEducationBaselineContributor()) {
                    executePostgresqlResource(
                            connection,
                            "classpath:db/baseline/postgresql/education-schema.sql",
                            "education schema");
                }
                executePostgresqlResource(
                        connection,
                        "classpath:db/baseline/postgresql/seed.sql",
                        "seed");
                if (hasEducationBaselineContributor()) {
                    executePostgresqlResource(
                            connection,
                            "classpath:db/baseline/postgresql/education-seed.sql",
                            "education seed");
                }
                insertBaselineMarker(connection);
                executePostgresqlResource(
                        connection,
                        "classpath:db/baseline/postgresql/identity-sequences.sql",
                        "identity sequence");
            } else if (existingTables.contains(BASELINE_TABLE)) {
                try (Statement statement = connection.createStatement();
                        ResultSet markerRow = statement.executeQuery(
                                "SELECT ID FROM COMMON_SCHEMA_BASELINE WHERE ID=1 FOR UPDATE")) {
                    if (!markerRow.next()) {
                        throw new IllegalStateException("Database baseline marker is missing");
                    }
                }
                BaselineMarker marker = readBaselineMarker(connection);
                if (!BASELINE_VERSION.equals(marker.version())
                        || !SEED_PROFILE.equals(marker.seedProfile())) {
                    throw unsupportedBaseline(marker);
                }
                ensureEducationBaselinePresent(existingTables);
            } else {
                throw new IllegalStateException(
                        "Refusing to initialize a non-empty PostgreSQL database without "
                                + BASELINE_TABLE
                                + "; existing tables="
                                + new TreeSet<>(existingTables));
            }

            executePostgresqlUpdates(connection);
            assertExpectedTables(loadPublicTables(connection));
            assertExpectedColumns(connection);
            connection.commit();
            log.info(
                    "PostgreSQL database baseline {} ({}) initialized or validated: {} application tables",
                    BASELINE_VERSION,
                    SEED_PROFILE,
                    expectedTables().size() - 1);
        } catch (Exception failure) {
            rollbackQuietly(connection, failure);
            throw failure;
        } finally {
            if (!connection.isClosed()) {
                connection.setAutoCommit(originalAutoCommit);
                if (connection.getTransactionIsolation() != originalIsolation) {
                    connection.setTransactionIsolation(originalIsolation);
                }
            }
        }
    }

    private void ensureEducationBaselinePresent(Set<String> existingTables) {
        if (hasEducationBaselineContributor()
                && existingTables.stream().noneMatch(table -> table.startsWith("EDU_"))) {
            throw new IllegalStateException(
                    "Education baseline is enabled but this database was installed without"
                            + " Education; manual rebuild required");
        }
    }

    private void executePostgresqlUpdates(Connection connection) throws Exception {
        if (!hasEducationBaselineContributor()) {
            Set<String> tables = loadPublicTables(connection);
            if (tables.stream().anyMatch(table -> table.startsWith("EDU_"))) {
                throw new IllegalStateException(
                        "Education tables exist but no education baseline contributor is active");
            }
        }
        Resource manifest = resourceResolver.getResource(
                "classpath:db/baseline/postgresql/update-manifest.txt");
        if (!manifest.exists()) {
            throw new IllegalStateException("Missing PostgreSQL update manifest");
        }
        List<String> resources = new ArrayList<>();
        try (var reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(manifest.getInputStream(), StandardCharsets.UTF_8))) {
            String resource;
            while ((resource = reader.readLine()) != null) {
                resource = resource.trim();
                if (!resource.isEmpty() && !resource.startsWith("--")) {
                    resources.add("classpath:" + resource);
                }
            }
        }
        boolean educationEnabled = hasEducationBaselineContributor();
        resources.stream()
                .filter(resource -> educationEnabled || !resource.contains("/education/"))
                .forEach(resource -> executePostgresqlResource(connection, resource, "update"));
    }

    private void executePostgresqlResource(Connection connection, String location, String phase) {
        Resource resource = resourceResolver.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Missing PostgreSQL database " + phase + " resource: " + location);
        }
        log.info("Executing PostgreSQL database {} resource: {}", phase, location);
        try (var input = resource.getInputStream()) {
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String statementSql : PostgresqlScriptExecutor.splitStatements(sql)) {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(statementSql);
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to execute PostgreSQL database " + phase + " resource: " + location,
                    exception);
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
                expectedTables().size() - 1);
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

    private boolean hasEducationBaselineContributor() {
        return contributors.stream()
                .map(DatabaseBaselineContributor::expectedTables)
                .filter(java.util.Objects::nonNull)
                .anyMatch(tables -> tables.stream().anyMatch(table -> table.startsWith("EDU_")));
    }

    private void installPermissionUpdates(Connection connection) throws Exception {
        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);
            // 多实例启动时串行执行增补，避免分配相同的权限 ID。
            try (Statement statement = connection.createStatement();
                    ResultSet marker = statement.executeQuery(
                            "SELECT ID FROM COMMON_SCHEMA_BASELINE WHERE ID=1 FOR UPDATE")) {
                if (!marker.next()) {
                    throw new IllegalStateException("Database baseline marker is missing");
                }
            }
            boolean educationEnabled = hasEducationBaselineContributor();
            List<String> updateResources =
                    List.of(
                                    "classpath:db/updates/iam/20260914_platform_usage.sql",
                                    "classpath:db/updates/iam/20260916_tenant_module_access.sql",
                                    "classpath:db/updates/iam/20260923_permission_alignment.sql",
                                    "classpath:db/updates/education/20261005_exam_attempt.sql",
                                    "classpath:db/updates/education/20261006_question_purpose.sql",
                                    "classpath:db/updates/knowledge/20261007_demo_education_access.sql",
                                    "classpath:db/updates/education/20261007_practice_submission.sql",
                                    "classpath:db/updates/model/20261011_deepseek_flash.sql",
                                    "classpath:db/updates/agent/20261012_checkpoint_pointer.sql",
                                    "classpath:db/updates/agent/20261013_sample_apps.sql",
                                    "classpath:db/updates/agent/20261014_seed_deepseek_graphs.sql",
                                    "classpath:db/updates/governance/20261024_quota_state.sql",
                                    "classpath:db/updates/conversation/20261024_idempotency_fingerprint.sql",
                                    "classpath:db/updates/conversation/20261024_generation_postprocessing.sql",
                                    "classpath:db/updates/agent/20261027_version_numeric_delete_flag.sql")
                            .stream()
                            .filter(
                                    resource ->
                                            educationEnabled
                                                    || !resource.contains("/updates/education/"))
                            .toList();
            executeResources(connection, updateResources, "permission update");
            connection.commit();
        } catch (Exception failure) {
            rollbackQuietly(connection, failure);
            throw failure;
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
                    tables.add(resultSet.getString("TABLE_NAME").toUpperCase(Locale.ROOT));
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
        Set<String> expectedTables = expectedTables();
        Set<String> missing = new TreeSet<>(expectedTables);
        missing.removeAll(actualTables);
        Set<String> unexpected = new TreeSet<>(actualTables);
        unexpected.removeAll(expectedTables);
        unexpected.removeAll(INFRASTRUCTURE_TABLES);
        unexpected.removeAll(UPDATE_TABLES);
        unexpected.removeIf(table -> table.startsWith("EDU_"));
        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            throw new IllegalStateException(
                    "Database schema does not match baseline; missing="
                            + missing
                            + ", unexpected="
                            + unexpected);
        }
    }

    private void assertExpectedColumns(Connection connection) throws Exception {
        Set<String> requiredColumns =
                Set.of(
                        "MODEL_AI_PLATFORM.ADAPTER_TYPE",
                        "CHAT_IDEMPOTENCY_KEY.FINGERPRINT",
                        "GOVERNANCE_QUOTA_TENANT.TENANT_ID",
                        "GOVERNANCE_QUOTA_TENANT.DAY_START",
                        "GOVERNANCE_QUOTA_TENANT.MINUTE_START_EPOCH_SECOND",
                        "GOVERNANCE_QUOTA_TENANT.TOKENS_USED",
                        "GOVERNANCE_QUOTA_TENANT.TOKENS_RESERVED",
                        "GOVERNANCE_QUOTA_TENANT.REQUESTS_THIS_MINUTE",
                        "GOVERNANCE_QUOTA_TENANT.ACTIVE_RESERVATIONS",
                        "GOVERNANCE_QUOTA_RESERVATION.ID",
                        "GOVERNANCE_QUOTA_RESERVATION.TENANT_ID",
                        "GOVERNANCE_QUOTA_RESERVATION.REFERENCE_KEY",
                        "GOVERNANCE_QUOTA_RESERVATION.RESERVED_ON_DATE",
                        "GOVERNANCE_QUOTA_RESERVATION.ESTIMATED_TOKENS",
                        "GOVERNANCE_QUOTA_RESERVATION.STATUS",
                        "GOVERNANCE_QUOTA_RESERVATION.CREATED_AT_EPOCH_MILLIS",
                        "GOVERNANCE_QUOTA_RESERVATION.EXPIRES_AT_EPOCH_MILLIS",
                        "CHAT_GENERATION_POSTPROCESSING.GENERATION_ID",
                        "CHAT_GENERATION_POSTPROCESSING.TENANT_ID",
                        "CHAT_GENERATION_POSTPROCESSING.OWNER_USER_ID",
                        "CHAT_GENERATION_POSTPROCESSING.QUOTA_ACTION",
                        "CHAT_GENERATION_POSTPROCESSING.USAGE_ACTION",
                        "CHAT_GENERATION_POSTPROCESSING.QUOTA_DONE",
                        "CHAT_GENERATION_POSTPROCESSING.USAGE_DONE",
                        "CHAT_GENERATION_POSTPROCESSING.ATTEMPTS",
                        "CHAT_GENERATION_POSTPROCESSING.NEXT_ATTEMPT_AT",
                        "CHAT_GENERATION_POSTPROCESSING.CLAIM_TOKEN",
                        "CHAT_GENERATION_POSTPROCESSING.CLAIMED_UNTIL",
                        "CHAT_GENERATION_POSTPROCESSING.CREATED_AT");
        Set<String> foundColumns = new HashSet<>();
        String product = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);
        String catalog = connection.getCatalog();
        try (ResultSet resultSet = connection.getMetaData().getColumns(null, null, "%", "%")) {
            while (resultSet.next()) {
                String tableCatalog = resultSet.getString("TABLE_CAT");
                String tableSchema = resultSet.getString("TABLE_SCHEM");
                boolean applicationSchema =
                        product.contains("mysql")
                                ? catalog != null && catalog.equalsIgnoreCase(tableCatalog)
                                : isApplicationSchema(product, tableSchema, catalog);
                if (!applicationSchema) {
                    continue;
                }
                foundColumns.add(
                        (resultSet.getString("TABLE_NAME")
                                        + "."
                                        + resultSet.getString("COLUMN_NAME"))
                                .toUpperCase(Locale.ROOT));
            }
        }
        Set<String> missingColumns = new TreeSet<>(requiredColumns);
        missingColumns.removeAll(foundColumns);
        if (!missingColumns.isEmpty()) {
            throw new IllegalStateException(
                    "Database schema does not match baseline; missing="
                            + missingColumns
                            + "; manual rebuild required");
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

    /**
     * 封装 Baseline Marker 相关的不可变数据及其字段约束。
     */
    private record BaselineMarker(String version, String seedProfile) {}
}
