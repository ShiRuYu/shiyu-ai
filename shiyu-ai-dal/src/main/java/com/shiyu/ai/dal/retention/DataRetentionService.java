package com.shiyu.ai.dal.retention;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Keeps operational tables bounded without touching conversations or durable user knowledge.
 * Child execution records are deleted before their parent execution rows.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataRetentionService {

    private final JdbcTemplate jdbcTemplate;
    private final DataRetentionProperties properties;

    @Scheduled(fixedDelayString = "${shiyu.retention.data.interval-ms:3600000}",
            initialDelayString = "${shiyu.retention.data.initial-delay-ms:180000}")
    @Transactional
    void cleanup() {
        if (!properties.isEnabled()) return;
        Instant now = Instant.now();
        Instant executionCutoff = now.minus(Math.max(1, properties.getExecutionDays()), ChronoUnit.DAYS);
        Instant auditCutoff = now.minus(Math.max(1, properties.getAuditDays()), ChronoUnit.DAYS);
        Instant usageCutoff = now.minus(Math.max(1, properties.getUsageDays()), ChronoUnit.DAYS);
        Instant taskCutoff = now.minus(Math.max(1, properties.getTaskDays()), ChronoUnit.DAYS);
        deleteBefore("agent_node_execution", "create_time", executionCutoff);
        deleteBefore("agent_checkpoint", "create_time", executionCutoff);
        deleteBefore("observation_execution_timeline", "create_time", executionCutoff);
        deleteBefore("memory_episodic_memory", "create_time", executionCutoff);
        deleteBefore("agent_execution", "create_time", executionCutoff);
        deleteBefore("observation_audit_log", "create_time", auditCutoff);
        deleteBefore("knowledge_audit_log", "create_time", auditCutoff);
        deleteBefore("agent_usage_record", "create_time", usageCutoff);
        deleteCompletedTasks(taskCutoff);
    }

    private void deleteCompletedTasks(Instant cutoff) {
        execute("knowledge_ingestion_job", "DELETE FROM knowledge_ingestion_job "
                + "WHERE create_time < ? AND job_status IN ('SUCCEEDED', 'FAILED', 'CANCELLED')",
                cutoff);
    }

    private void deleteBefore(String table, String timeColumn, Instant cutoff) {
        execute(table, "DELETE FROM " + table + " WHERE " + timeColumn + " < ?", cutoff);
    }

    private void execute(String table, String sql, Instant cutoff) {
        try {
            int deleted = jdbcTemplate.update(sql, Timestamp.from(cutoff));
            if (deleted > 0) log.info("Retention cleanup deleted {} rows from {}", deleted, table);
        } catch (DataAccessException exception) {
            // Optional modules may not have been migrated in a lightweight deployment.
            log.debug("Retention table {} is unavailable or cleanup failed", table, exception);
        }
    }
}
