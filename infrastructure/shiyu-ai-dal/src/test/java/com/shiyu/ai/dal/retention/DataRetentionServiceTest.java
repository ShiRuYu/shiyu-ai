package com.shiyu.ai.dal.retention;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DataRetentionServiceTest {

    @Test
    void removesOnlyExpiredOperationalRecordsOnH2() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:retention_" + UUID.randomUUID().toString().replace("-", "")
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        createOperationalTables(jdbcTemplate);
        Instant old = Instant.now().minus(2, ChronoUnit.DAYS);
        Instant current = Instant.now();
        for (String table : new String[]{
                "agent_node_execution", "agent_checkpoint", "observation_execution_timeline",
                "memory_episodic_memory", "agent_execution", "observation_audit_log",
                "knowledge_audit_log", "agent_usage_record"}) {
            insertTimestamped(jdbcTemplate, table, old);
            insertTimestamped(jdbcTemplate, table, current);
        }
        jdbcTemplate.update("INSERT INTO knowledge_ingestion_job (create_time, job_status) VALUES (?, ?)",
                Timestamp.from(old), "SUCCEEDED");
        jdbcTemplate.update("INSERT INTO knowledge_ingestion_job (create_time, job_status) VALUES (?, ?)",
                Timestamp.from(old), "RUNNING");
        jdbcTemplate.update("INSERT INTO knowledge_ingestion_job (create_time, job_status) VALUES (?, ?)",
                Timestamp.from(current), "FAILED");

        DataRetentionProperties properties = new DataRetentionProperties();
        properties.setEnabled(true);
        properties.setExecutionDays(1);
        properties.setAuditDays(1);
        properties.setUsageDays(1);
        properties.setTaskDays(1);

        new DataRetentionService(jdbcTemplate, properties).cleanup();

        for (String table : new String[]{
                "agent_node_execution", "agent_checkpoint", "observation_execution_timeline",
                "memory_episodic_memory", "agent_execution", "observation_audit_log",
                "knowledge_audit_log", "agent_usage_record"}) {
            assertEquals(1, count(jdbcTemplate, table), table);
        }
        assertEquals(2, count(jdbcTemplate, "knowledge_ingestion_job"));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM knowledge_ingestion_job WHERE job_status = 'RUNNING'", Integer.class));
    }

    private void createOperationalTables(JdbcTemplate jdbcTemplate) {
        for (String table : new String[]{
                "agent_node_execution", "agent_checkpoint", "observation_execution_timeline",
                "memory_episodic_memory", "agent_execution", "observation_audit_log",
                "knowledge_audit_log", "agent_usage_record"}) {
            jdbcTemplate.execute("CREATE TABLE " + table + " (id BIGINT AUTO_INCREMENT PRIMARY KEY, create_time TIMESTAMP)");
        }
        jdbcTemplate.execute("CREATE TABLE knowledge_ingestion_job (id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "create_time TIMESTAMP, job_status VARCHAR(32))");
    }

    private void insertTimestamped(JdbcTemplate jdbcTemplate, String table, Instant createTime) {
        jdbcTemplate.update("INSERT INTO " + table + " (create_time) VALUES (?)", Timestamp.from(createTime));
    }

    private int count(JdbcTemplate jdbcTemplate, String table) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
    }
}
