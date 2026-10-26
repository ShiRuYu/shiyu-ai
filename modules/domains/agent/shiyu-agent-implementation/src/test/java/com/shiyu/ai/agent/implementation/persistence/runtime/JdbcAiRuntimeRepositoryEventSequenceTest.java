package com.shiyu.ai.agent.implementation.persistence.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEvent;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunSource;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.time.Instant;

/** 使用真实事件表验证运行事件的顺序、终态幂等和访问边界。 */
class JdbcAiRuntimeRepositoryEventSequenceTest {
    private static final TenantId TENANT = new TenantId(7L);
    private static final Instant NOW = Instant.parse("2025-01-01T00:00:00Z");

    @Test
    void nextEventKeepsOneTerminalRowAndRejectsLaterOrForeignWrites() {
        EmbeddedDatabase database = database();
        TenantScope.set(TENANT);
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            JdbcAiRuntimeRepository repository = new JdbcAiRuntimeRepository(jdbc);
            repository.insert(run());

            assertEquals(1, repository.appendNextEvent("run", TENANT, 9,
                    AiRunEventType.MODEL_STARTED, "start", false, NOW,
                    "turn-1", "step-1", "provider-1"));
            assertEquals(2, repository.appendNextEvent("run", TENANT, 9,
                    AiRunEventType.RUN_FAILED, "failure", true, NOW));
            assertEquals(2, repository.appendNextEvent("run", TENANT, 9,
                    AiRunEventType.RUN_FAILED, "failure", true, NOW));

            assertThrows(IllegalStateException.class,
                    () -> repository.appendNextEvent("run", TENANT, 9,
                            AiRunEventType.MODEL_DELTA, "late", false, NOW));
            assertThrows(IllegalStateException.class,
                    () -> repository.appendNextEvent("run", TENANT, 9,
                            AiRunEventType.RUN_FAILED, "changed", true, NOW));
            assertThrows(IllegalStateException.class,
                    () -> repository.appendNextEvent("run", TENANT, 9,
                            AiRunEventType.RUN_COMPLETED, "failure", true, NOW));
            assertThrows(IllegalArgumentException.class,
                    () -> repository.appendNextEvent("run", TENANT, 10,
                            AiRunEventType.MODEL_DELTA, "foreign", false, NOW));
            assertThrows(IllegalArgumentException.class,
                    () -> repository.appendNextEvent("run", new TenantId(8L), 9,
                            AiRunEventType.MODEL_DELTA, "foreign", false, NOW));

            assertEquals(2L, jdbc.queryForObject("SELECT COUNT(*) FROM AI_RUN_EVENT "
                    + "WHERE RUN_ID='run'", Long.class).longValue());
            assertEquals(2L, jdbc.queryForObject("SELECT LAST_EVENT_SEQ FROM AI_RUN "
                    + "WHERE ID='run'", Long.class).longValue());
            assertEquals("turn-1", jdbc.queryForObject("SELECT TURN_ID FROM AI_RUN_EVENT "
                    + "WHERE RUN_ID='run' AND SEQ=1", String.class));
            assertEquals("provider-1", jdbc.queryForObject(
                    "SELECT PROVIDER_REQUEST_ID FROM AI_RUN_EVENT "
                            + "WHERE RUN_ID='run' AND SEQ=1", String.class));
        } finally {
            TenantScope.clear();
            database.shutdown();
        }
    }

    @Test
    void terminalReplayRepairsAnAheadEventSequencePointer() {
        EmbeddedDatabase database = database();
        TenantScope.set(TENANT);
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            JdbcAiRuntimeRepository repository = new JdbcAiRuntimeRepository(jdbc);
            repository.insert(run());
            AiRun failed = repository.finishGenerationRun(
                    "run", TENANT, 9, AiRunStatus.FAILED, "agent-error",
                    java.util.List.of(), null, null);
            assertEquals(1L, failed.lastEventSeq());
            jdbc.update("UPDATE AI_RUN SET LAST_EVENT_SEQ=? WHERE ID=?", 0L, "run");
            AiRun repairedMissingPointer = repository.finishGenerationRun(
                    "run", TENANT, 9, AiRunStatus.FAILED, "agent-error",
                    java.util.List.of(), null, null);
            assertEquals(1L, repairedMissingPointer.lastEventSeq());
            assertEquals(1L, repository.find("run", TENANT, 9).orElseThrow().lastEventSeq());

            jdbc.update("UPDATE AI_RUN SET LAST_EVENT_SEQ=? WHERE ID=?", 5L, "run");
            AiRun repairedAheadPointer = repository.finishGenerationRun(
                    "run", TENANT, 9, AiRunStatus.FAILED, "agent-error",
                    java.util.List.of(), null, null);

            assertEquals(1L, repairedAheadPointer.lastEventSeq());
            assertEquals(1L, repository.find("run", TENANT, 9).orElseThrow().lastEventSeq());
            assertEquals(1L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM AI_RUN_EVENT WHERE RUN_ID='run'", Long.class));
        } finally {
            TenantScope.clear();
            database.shutdown();
        }
    }

    @Test
    void terminalReplayRepairsAnEventSequencePointerBehindTheTerminalEvent() {
        EmbeddedDatabase database = database();
        TenantScope.set(TENANT);
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            JdbcAiRuntimeRepository repository = new JdbcAiRuntimeRepository(jdbc);
            repository.insert(run());
            assertEquals(1L, repository.appendNextEvent(
                    "run", TENANT, 9, AiRunEventType.MODEL_STARTED, "start", true, NOW));
            AiRun failed = repository.finishGenerationRun(
                    "run", TENANT, 9, AiRunStatus.FAILED, "agent-error",
                    java.util.List.of(), null, null);
            assertEquals(2L, failed.lastEventSeq());
            jdbc.update("DELETE FROM AI_RUN_EVENT WHERE RUN_ID=? AND SEQ=?", "run", 1L);
            jdbc.update("UPDATE AI_RUN SET LAST_EVENT_SEQ=? WHERE ID=?", 1L, "run");

            AiRun repaired = repository.finishGenerationRun(
                    "run", TENANT, 9, AiRunStatus.FAILED, "agent-error",
                    java.util.List.of(), null, null);

            assertEquals(2L, repaired.lastEventSeq());
            assertEquals(2L, repository.find("run", TENANT, 9).orElseThrow().lastEventSeq());
            assertEquals(1L, jdbc.queryForObject(
                    "SELECT COUNT(*) FROM AI_RUN_EVENT WHERE RUN_ID='run'", Long.class));
        } finally {
            TenantScope.clear();
            database.shutdown();
        }
    }

    @Test
    void explicitEventIsIdempotentOnlyForSamePayloadAndMustBeContiguous() {
        EmbeddedDatabase database = database();
        TenantScope.set(TENANT);
        try {
            JdbcTemplate jdbc = new JdbcTemplate(database);
            JdbcAiRuntimeRepository repository = new JdbcAiRuntimeRepository(jdbc);
            repository.insert(run());
            AiRunEvent started = event(1, AiRunEventType.MODEL_STARTED, "start", false);

            assertEquals(1, repository.appendEvent(started));
            assertEquals(1, repository.appendEvent(started));
            assertThrows(IllegalStateException.class,
                    () -> repository.appendEvent(event(1, AiRunEventType.MODEL_STARTED,
                            "changed", false)));
            assertThrows(IllegalStateException.class,
                    () -> repository.appendEvent(event(3, AiRunEventType.MODEL_DELTA,
                            "gap", false)));
            assertEquals(2, repository.appendEvent(event(2, AiRunEventType.RUN_COMPLETED,
                    "done", false)));
            assertThrows(IllegalStateException.class,
                    () -> repository.appendEvent(event(3, AiRunEventType.MODEL_DELTA,
                            "late", false)));

            assertEquals(2L, jdbc.queryForObject("SELECT COUNT(*) FROM AI_RUN_EVENT "
                    + "WHERE RUN_ID='run'", Long.class).longValue());
            assertEquals(2L, jdbc.queryForObject("SELECT LAST_EVENT_SEQ FROM AI_RUN "
                    + "WHERE ID='run'", Long.class).longValue());
        } finally {
            TenantScope.clear();
            database.shutdown();
        }
    }

    private static EmbeddedDatabase database() {
        return new EmbeddedDatabaseBuilder().generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .addScript("db/baseline/h2/schema/agent/16_ai_runtime.sql").build();
    }

    private static AiRunEvent event(long seq, AiRunEventType type,
            String payload, boolean redacted) {
        return new AiRunEvent("run", TENANT, seq, type, payload, redacted, NOW);
    }

    private static AiRun run() {
        return new AiRun("run", TENANT, new UserId(9), "app", "version",
                AiRunSource.CONVERSATION, "source", null, "trace", "conversation",
                "generation", "execution", "model", "hash", AiRunStatus.RUNNING,
                0, 0, false, "cost", NOW, null, null, 0, 0);
    }
}
