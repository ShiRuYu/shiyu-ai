package com.shiyu.ai.knowledge.implementation.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

/** 验证 PostgreSQL 和 H2 索引激活的参数类型及租约保护。 */
class KnowledgeSpaceMapperPostgresqlTest {
    @Test
    @EnabledIfEnvironmentVariable(named = "SHIYU_PG_TEST_URL", matches = ".+")
    void activatesSynchronousAndAsynchronousRebuildsWithTypedJobIds() {
        DriverManagerDataSource admin = new DriverManagerDataSource(
                System.getenv("SHIYU_PG_TEST_URL"),
                System.getenv().getOrDefault("SHIYU_PG_TEST_USERNAME", "postgres"),
                System.getenv().getOrDefault("SHIYU_PG_TEST_PASSWORD", ""));
        JdbcTemplate adminJdbc = new JdbcTemplate(admin);
        String schema = "knowledge_test_" + UUID.randomUUID().toString().replace("-", "");
        adminJdbc.execute("CREATE SCHEMA " + schema);
        try {
            DriverManagerDataSource scoped = new DriverManagerDataSource();
            scoped.setUrl(admin.getUrl());
            scoped.setUsername(admin.getUsername());
            scoped.setPassword(admin.getPassword());
            Properties properties = new Properties();
            properties.put("currentSchema", schema);
            scoped.setConnectionProperties(properties);
            verifyActivation(scoped);
        } finally {
            adminJdbc.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }

    @Test
    void activatesSynchronousAndAsynchronousRebuildsOnH2() {
        verifyActivation(new DriverManagerDataSource(
                "jdbc:h2:mem:knowledge_activation_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", ""));
    }

    /** 验证同步和异步激活成功，并拒绝过期租约及错误任务版本。 */
    private static void verifyActivation(DriverManagerDataSource scoped) {
        JdbcTemplate jdbc = new JdbcTemplate(scoped);
        jdbc.execute("CREATE TABLE knowledge_space (id BIGINT PRIMARY KEY, tenant_id BIGINT, active_index_version BIGINT, del_flag INTEGER)");
        jdbc.execute("CREATE TABLE knowledge_ingestion_job (id BIGINT PRIMARY KEY, tenant_id BIGINT, space_id BIGINT, job_type VARCHAR(64), job_key VARCHAR(128), checkpoint_data TEXT, job_status VARCHAR(32), lock_version BIGINT, heartbeat_time TIMESTAMP, del_flag INTEGER, stage VARCHAR(32))");
        jdbc.update("INSERT INTO knowledge_space VALUES (5,7,0,0)");
        jdbc.update("INSERT INTO knowledge_ingestion_job VALUES (8,7,5,'INDEX_REBUILD_LEASE','INDEX_REBUILD_LEASE:5',NULL,'RUNNING',2,CURRENT_TIMESTAMP,0,NULL)");
        jdbc.update("INSERT INTO knowledge_ingestion_job VALUES (9,7,5,'INDEX_REBUILD',NULL,NULL,'RUNNING',3,CURRENT_TIMESTAMP,0,'ACTIVATING')");
        Configuration configuration = new Configuration(new Environment(
                "activation-contract", new JdbcTransactionFactory(), scoped));
        configuration.addMapper(KnowledgeSpaceMapper.class);
        try (var session = new SqlSessionFactoryBuilder().build(configuration).openSession(true)) {
            KnowledgeSpaceMapper mapper = session.getMapper(KnowledgeSpaceMapper.class);
            LocalDateTime heartbeatBefore = LocalDateTime.now().minusMinutes(2);
            assertEquals(1, mapper.activateIndexRebuild(
                    7L, 5L, "INDEX_REBUILD_LEASE:5", 2L, heartbeatBefore, 0L, 1L, null, null));
            jdbc.update("UPDATE knowledge_ingestion_job SET checkpoint_data='9' WHERE id=8");
            assertEquals(1, mapper.activateIndexRebuild(
                    7L, 5L, "INDEX_REBUILD_LEASE:5", 2L, heartbeatBefore, 1L, 2L, "9", 3L));
            assertEquals(0, mapper.activateIndexRebuild(
                    7L, 5L, "INDEX_REBUILD_LEASE:5", 1L, heartbeatBefore, 2L, 3L, "9", 3L));
            assertEquals(0, mapper.activateIndexRebuild(
                    7L, 5L, "INDEX_REBUILD_LEASE:5", 2L, heartbeatBefore, 2L, 3L, "9", 2L));
            assertEquals(2L, jdbc.queryForObject(
                    "SELECT active_index_version FROM knowledge_space WHERE id=5", Long.class));
        }
    }
}
