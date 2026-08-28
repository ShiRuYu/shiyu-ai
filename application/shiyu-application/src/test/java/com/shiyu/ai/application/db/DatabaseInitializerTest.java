package com.shiyu.ai.application.db;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInitializerTest {

    private static final Set<String> SEEDED_TABLES = Set.of(
            "COMMON_DICT",
            "AUTH_AUTH_CODE", "AUTH_MENU", "AUTH_ROLE", "AUTH_ROLE_SCOPE_AUTH_CODE",
            "AUTH_ROLE_SCOPE_MENU", "AUTH_TENANT", "AUTH_TENANT_AUTH_CODE",
            "AUTH_TENANT_MENU", "AUTH_USER", "AUTH_USER_SCOPE_ROLE",
            "AGENT_AI_MODEL", "AGENT_AI_PLATFORM", "AGENT_DEF", "AGENT_INTENT_DEF",
            "AGENT_VERSION",
            "KNOWLEDGE_DIFFICULTY_SCALE", "KNOWLEDGE_DIFFICULTY_SCALE_LEVEL"
    );

    @Test
    void installsCompleteBaselineAndSkipsSecondRun() throws Exception {
        DataSource dataSource = newDataSource();
        DatabaseInitializer initializer = newInitializer(dataSource);

        initializer.initialize();

        assertEquals(97, scalar(dataSource,
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
        assertEquals(41, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_MENU"));
        assertEquals(132, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_AUTH_CODE"));
        assertEquals(41, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_TENANT_MENU"));
        assertEquals(132, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_TENANT_AUTH_CODE"));
        assertTrue(scalar(dataSource, "SELECT COUNT(*) FROM AUTH_ROLE_SCOPE_MENU") >= 99);
        assertEquals(264, scalar(dataSource, "SELECT COUNT(*) FROM AUTH_ROLE_SCOPE_AUTH_CODE"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AUTH_USER_SCOPE_ROLE usr "
                        + "JOIN AUTH_USER u ON u.ID=usr.USER_ID "
                        + "JOIN AUTH_ROLE r ON r.ID=usr.ROLE_ID "
                        + "WHERE u.USERNAME='admin' AND r.CODE='super'"));

        assertEquals(4, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_AI_PLATFORM"));
        assertEquals(9, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_AI_MODEL"));
        assertEquals(11, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_DEF"));
        assertEquals(11, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_VERSION"));
        assertEquals(1, scalar(dataSource, "SELECT COUNT(*) FROM KNOWLEDGE_DIFFICULTY_SCALE"));
        assertEquals(5, scalar(dataSource, "SELECT COUNT(*) FROM KNOWLEDGE_DIFFICULTY_SCALE_LEVEL"));
        assertEquals(8, scalar(dataSource, "SELECT COUNT(*) FROM AGENT_INTENT_DEF"));
        assertEquals(1, scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_INTENT_DEF "
                        + "WHERE NAME='闲聊' AND DESCRIPTION='日常闲聊对话'"));
        assertTrue(scalar(dataSource,
                "SELECT COUNT(*) FROM AGENT_VERSION WHERE GRAPH_CONFIG LIKE '%scoreThreshold%'") > 0);
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
        assertEquals(9, scalar(dataSource,
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
