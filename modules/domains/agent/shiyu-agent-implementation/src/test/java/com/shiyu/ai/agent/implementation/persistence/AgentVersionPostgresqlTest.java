package com.shiyu.ai.agent.implementation.persistence;

import static org.junit.jupiter.api.Assertions.*;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.datasource.FlexDataSource;
import com.mybatisflex.core.dialect.DbType;
import com.mybatisflex.core.mybatis.FlexConfiguration;
import com.mybatisflex.core.mybatis.FlexSqlSessionFactoryBuilder;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.tenant.TenantManager;
import com.shiyu.ai.agent.implementation.persistence.dataobject.AgentVersionDO;
import com.shiyu.ai.agent.implementation.persistence.mapper.AgentVersionMapper;
import com.shiyu.ai.common.mybatis.tenant.ContextTenantFactory;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Pattern;

/** 验证 PostgreSQL 智能体版本表与整数删除标志的真实 MyBatis Flex 映射。 */
class AgentVersionPostgresqlTest {
    @Test
    @EnabledIfEnvironmentVariable(named = "SHIYU_PG_TEST_URL", matches = ".+")
    void queriesAndUpdatesNumericDeleteFlagsThroughRealMapper() throws Exception {
        verifyVersionMapping(false);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "SHIYU_PG_TEST_URL", matches = ".+")
    void migratesLegacyFlagsTwiceWithoutLosingValuesThenUsesRealMapper() throws Exception {
        verifyVersionMapping(true);
    }

    @Test
    void h2MigrationPreservesLegacyFlagsAndNullsAcrossRepeatedRuns() {
        JdbcTemplate jdbc = new JdbcTemplate(new DriverManagerDataSource(
                "jdbc:h2:mem:agent_version_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        jdbc.execute("CREATE TABLE AGENT_VERSION (ID BIGINT PRIMARY KEY, DEL_FLAG CHAR(1) DEFAULT '0')");
        jdbc.update("INSERT INTO AGENT_VERSION VALUES (1,'0'),(2,'1'),(3,NULL)");
        var migration = new ResourceDatabasePopulator(new ClassPathResource(
                "db/updates/agent/20261027_version_numeric_delete_flag.sql"));
        migration.execute(jdbc.getDataSource());
        migration.execute(jdbc.getDataSource());
        assertEquals(Arrays.asList(0, 1, null), jdbc.query(
                "SELECT DEL_FLAG FROM AGENT_VERSION ORDER BY ID", (rs, row) -> rs.getObject(1, Integer.class)));
        jdbc.update("INSERT INTO AGENT_VERSION (ID) VALUES (4)");
        assertEquals(0, jdbc.queryForObject("SELECT DEL_FLAG FROM AGENT_VERSION WHERE ID=4", Integer.class));
    }

    /** 用真实基线或已迁移的字符删除标志表验证整数映射与租户查询。 */
    private static void verifyVersionMapping(boolean legacy) throws Exception {
        DriverManagerDataSource admin = new DriverManagerDataSource(
                System.getenv("SHIYU_PG_TEST_URL"),
                System.getenv().getOrDefault("SHIYU_PG_TEST_USERNAME", "postgres"),
                System.getenv().getOrDefault("SHIYU_PG_TEST_PASSWORD", ""));
        JdbcTemplate adminJdbc = new JdbcTemplate(admin);
        String schema = "agent_version_test_" + UUID.randomUUID().toString().replace("-", "");
        adminJdbc.execute("CREATE SCHEMA " + schema);
        var previousConfig = FlexGlobalConfig.getDefaultConfig();
        var previousTenantFactory = TenantManager.getTenantFactory();
        var previousTenant = TenantScope.current().orElse(null);
        try {
            DriverManagerDataSource scoped = new DriverManagerDataSource();
            scoped.setUrl(admin.getUrl());
            scoped.setUsername(admin.getUsername());
            scoped.setPassword(admin.getPassword());
            Properties properties = new Properties();
            properties.put("currentSchema", schema);
            scoped.setConnectionProperties(properties);
            JdbcTemplate jdbc = new JdbcTemplate(scoped);
            String baseline = Files.readString(Path.of(
                    "../../../applications/shiyu-platform-composition/src/main/resources/db/baseline/postgresql/schema.sql"));
            var table = Pattern.compile("CREATE TABLE \\\"agent_version\\\"\\s*\\(.*?\\);", Pattern.DOTALL)
                    .matcher(baseline);
            assertTrue(table.find());
            String tableSql = table.group();
            if (legacy) tableSql = tableSql.replace("\"del_flag\" SMALLINT DEFAULT 0", "\"del_flag\" CHARACTER(1) DEFAULT '0'");
            if (legacy) assertTrue(tableSql.contains("\"del_flag\" CHARACTER(1)"));
            jdbc.execute(tableSql);
            jdbc.update("INSERT INTO agent_version (id,tenant_id,agent_id,version_number,del_flag) VALUES (1,7,'test','v1',0),(2,7,'test','deleted',1),(3,8,'test','v1',0)");
            jdbc.update("INSERT INTO agent_version (id,tenant_id,agent_id,version_number,del_flag) VALUES (4,7,'test','nullable',NULL)");
            if (legacy) {
                var migration = new ResourceDatabasePopulator(new FileSystemResource(Path.of(
                        "../../../applications/shiyu-platform-composition/src/main/resources/db/updates/postgresql/agent/20261027_version_numeric_delete_flag.sql")));
                migration.execute(scoped);
                migration.execute(scoped);
                assertEquals(Arrays.asList(0, 1, 0, null), jdbc.query(
                        "SELECT del_flag FROM agent_version ORDER BY id", (rs, row) -> rs.getObject(1, Integer.class)));
                jdbc.update("INSERT INTO agent_version (id,tenant_id,agent_id,version_number) VALUES (5,9,'test','default')");
                assertEquals(0, jdbc.queryForObject("SELECT del_flag FROM agent_version WHERE id=5", Integer.class));
            }
            var configuration = new FlexConfiguration(new Environment(
                    "agent-version-pg", new JdbcTransactionFactory(), new FlexDataSource("agent", scoped)));
            var factory = new FlexSqlSessionFactoryBuilder().build(configuration);
            FlexGlobalConfig config = FlexGlobalConfig.getConfig(configuration);
            config.setDbType(DbType.POSTGRE_SQL);
            config.setLogicDeleteColumn("delFlag");
            config.setNormalValueOfLogicDelete(0);
            config.setDeletedValueOfLogicDelete(1);
            configuration.addMapper(AgentVersionMapper.class);
            TenantManager.setTenantFactory(new ContextTenantFactory());
            TenantScope.set(new TenantId(7));
            try (var session = factory.openSession(true)) {
                AgentVersionMapper mapper = session.getMapper(AgentVersionMapper.class);
                AgentVersionDO version = mapper.selectOneByQuery(QueryWrapper.create()
                        .eq(AgentVersionDO::getTenantId, 7L)
                        .eq(AgentVersionDO::getAgentId, "test")
                        .eq(AgentVersionDO::getVersionNumber, "v1")
                        .eq(AgentVersionDO::getDelFlag, 0));
                assertNotNull(version);
                assertEquals(1L, version.getId());
                assertEquals(0, version.getDelFlag());
                version.setDelFlag(1);
                assertEquals(1, mapper.update(version));
                assertEquals(1, jdbc.queryForObject("SELECT del_flag FROM agent_version WHERE id=1", Integer.class));
                assertEquals(0L, mapper.selectCountByQuery(QueryWrapper.create()
                        .eq(AgentVersionDO::getTenantId, 7L).eq(AgentVersionDO::getDelFlag, 0)));
            }
        } finally {
            TenantManager.setTenantFactory(previousTenantFactory);
            FlexGlobalConfig.setDefaultConfig(previousConfig);
            if (previousTenant == null) TenantScope.clear();
            else TenantScope.set(previousTenant);
            adminJdbc.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }
}
