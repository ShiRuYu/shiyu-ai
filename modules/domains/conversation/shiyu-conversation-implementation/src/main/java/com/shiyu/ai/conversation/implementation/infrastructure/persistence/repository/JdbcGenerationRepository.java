package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunEventType;
import com.shiyu.ai.agent.contract.runtime.AiRunRepository;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.contract.model.GenerationStatus;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationEvent;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationEventType;
import com.shiyu.ai.conversation.implementation.domain.port.GenerationRepository;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * {@code JdbcGenerationRepository} 定义会话模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
@Component
public class JdbcGenerationRepository implements GenerationRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;
    /**
     * runtimeRuns 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRunRepository runtimeRuns;
    /**
     * recoveryTimeoutMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long recoveryTimeoutMs;

    /**
     * {@code JdbcGenerationRepository} 创建并初始化当前类型实例。
     *
     * @param dataSource 参数值，用于执行当前操作。
     * @param runtimeRuns 参数值，用于执行当前操作。
     */
    public JdbcGenerationRepository(
            @Qualifier("agentDataSource") DataSource dataSource, AiRunRepository runtimeRuns) {
        this(dataSource, runtimeRuns, 300_000L);
    }

    /**
     * {@code JdbcGenerationRepository} 创建并初始化当前类型实例。
     *
     * @param dataSource 参数值，用于执行当前操作。
     * @param runtimeRuns 参数值，用于执行当前操作。
     * @param recoveryTimeoutMs 参数值，用于执行当前操作。
     */
    @Autowired
    public JdbcGenerationRepository(
            @Qualifier("agentDataSource") DataSource dataSource,
            AiRunRepository runtimeRuns,
            @Value("${shiyu.conversation.recovery-timeout-ms:300000}") long recoveryTimeoutMs) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.runtimeRuns = runtimeRuns;
        this.recoveryTimeoutMs = Math.max(1000L, recoveryTimeoutMs);
    }

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param g 参数值，用于执行当前操作。
     */
    @Override
    @Transactional
    public void insert(GenerationRun g) {
        Long tenant =
                jdbc.queryForObject(
                        "SELECT TENANT_ID FROM CHAT_CONVERSATION WHERE ID=?",
                        Long.class,
                        g.conversationId());
        if (tenant == null) throw new IllegalArgumentException("conversation not found");
        try {
            jdbc.update(
                    "INSERT INTO CHAT_GENERATION_ACTIVE"
                        + " (TENANT_ID,CONVERSATION_ID,INPUT_MESSAGE_ID,GENERATION_ID,CREATED_AT)"
                        + " VALUES (?,?,?,?,?)",
                    tenant,
                    g.conversationId(),
                    g.inputMessageId(),
                    g.id(),
                    ts(g.createdAt()));
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            throw new IllegalStateException("a generation is already running for this message", ex);
        }
        jdbc.update(
                "INSERT INTO CHAT_GENERATION_RUN"
                    + " (ID,TENANT_ID,CONVERSATION_ID,INPUT_MESSAGE_ID,ASSISTANT_MESSAGE_ID,RUNTIME_RUN_ID,SPEAKER_ID,PLATFORM,MODEL,STATUS,PROMPT_TOKENS,COMPLETION_TOKENS,LATENCY_MS,ERROR_CODE,LAST_EVENT_SEQUENCE,CANCEL_REQUESTED,VERSION,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                g.id(),
                tenant,
                g.conversationId(),
                g.inputMessageId(),
                g.assistantMessageId(),
                g.runtimeRunId(),
                g.speakerId(),
                g.platform(),
                g.model(),
                g.status().name(),
                g.promptTokens(),
                g.completionTokens(),
                g.latencyMs(),
                g.errorCode(),
                g.lastEventSequence(),
                g.cancelRequested(),
                g.version(),
                ts(g.createdAt()),
                ts(g.updatedAt()));
    }

    /**
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<GenerationRun> find(String id, TenantId tenantId, long ownerUserId) {
        return jdbc
                .query(
                        "SELECT g.* FROM CHAT_GENERATION_RUN g JOIN CHAT_CONVERSATION c ON"
                                + " c.ID=g.CONVERSATION_ID WHERE g.ID=? AND g.TENANT_ID=? AND"
                                + " c.OWNER_USER_ID=?",
                        this::map,
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * {@code hasRunning} 校验当前操作的输入或状态是否满足约束。
     *
     * @param conversationId 参数值，用于执行当前操作。
     * @param inputMessageId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean hasRunning(String conversationId, String inputMessageId, TenantId tenantId) {
        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CHAT_GENERATION_ACTIVE WHERE CONVERSATION_ID=? AND"
                                + " INPUT_MESSAGE_ID=? AND TENANT_ID=?",
                        Integer.class,
                        conversationId,
                        inputMessageId,
                        tenantId.value());
        return count != null && count > 0;
    }

    /**
     * {@code hasRunningConversation} 校验当前操作的输入或状态是否满足约束。
     *
     * @param conversationId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean hasRunningConversation(String conversationId, TenantId tenantId) {
        Integer count =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM CHAT_GENERATION_ACTIVE WHERE CONVERSATION_ID=? AND"
                                + " TENANT_ID=?",
                        Integer.class,
                        conversationId,
                        tenantId.value());
        return count != null && count > 0;
    }

    /**
     * {@code listConversation} 查询并返回当前操作所需的数据。
     *
     * @param conversationId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GenerationRun> listConversation(
            String conversationId, TenantId tenantId, int limit) {
        return jdbc.query(
                "SELECT g.* FROM CHAT_GENERATION_RUN g WHERE g.CONVERSATION_ID=? AND g.TENANT_ID=?"
                        + " ORDER BY g.CREATED_AT,g.ID LIMIT ?",
                this::map,
                conversationId,
                tenantId.value(),
                Math.min(Math.max(limit, 1), 1000));
    }

    /**
     * 回收过期的生成记录。
     *
     * @return 处理结果。
     */
    @Scheduled(fixedDelayString = "${shiyu.conversation.recovery-delay-ms:30000}")
    @Transactional
    public void recoverStaleGenerations() {
        recoverStaleGenerations(recoveryTimeoutMs);
    }

    /**
     * {@code recoverStaleGenerations} 执行当前类型定义的业务操作。
     *
     * @param timeoutMs 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Transactional
    public int recoverStaleGenerations(long timeoutMs) {
        long grace = Math.max(1000L, timeoutMs);
        Timestamp cutoff = Timestamp.from(Instant.now().minusMillis(grace));
        List<StaleGeneration> stale =
                jdbc.query(
                        "SELECT g.*,c.OWNER_USER_ID FROM CHAT_GENERATION_RUN g JOIN"
                            + " CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID WHERE g.STATUS IN"
                            + " ('CREATED','RUNNING') AND g.UPDATED_AT<? ORDER BY g.UPDATED_AT,g.ID"
                            + " LIMIT 100",
                        (rs, rowNum) ->
                                new StaleGeneration(map(rs, rowNum), rs.getLong("OWNER_USER_ID")),
                        cutoff);
        int recovered = 0;
        for (StaleGeneration abandoned : stale) {
            GenerationRun current = abandoned.run();
            GenerationRun failed = current.transition(GenerationStatus.FAILED);
            failed =
                    new GenerationRun(
                            failed.id(),
                            failed.conversationId(),
                            failed.inputMessageId(),
                            failed.assistantMessageId(),
                            failed.speakerId(),
                            failed.platform(),
                            failed.model(),
                            failed.status(),
                            failed.promptTokens(),
                            failed.completionTokens(),
                            Duration.between(current.createdAt(), Instant.now()).toMillis(),
                            "SERVICE_RESTART",
                            failed.lastEventSequence(),
                            false,
                            failed.version(),
                            failed.createdAt(),
                            Instant.now(),
                            failed.runtimeRunId());
            if (update(failed, current.version()) != 1) continue;
            recovered++;
            if (failed.runtimeRunId() != null && !failed.runtimeRunId().isBlank()) {
                try {
                    AiRun run =
                            runtimeRuns
                                    .find(
                                            failed.runtimeRunId(),
                                            new TenantId(currentTenant(current)),
                                            abandoned.ownerUserId())
                                    .orElse(null);
                    if (run != null
                            && run.status() != AiRunStatus.COMPLETED
                            && run.status() != AiRunStatus.FAILED
                            && run.status() != AiRunStatus.CANCELLED) {
                        AiRun failedRun =
                                new AiRun(
                                        run.id(),
                                        run.tenantId(),
                                        run.ownerUserId(),
                                        run.appId(),
                                        run.appVersionId(),
                                        run.sourceType(),
                                        run.sourceId(),
                                        run.parentRunId(),
                                        run.traceId(),
                                        run.conversationId(),
                                        run.generationId(),
                                        run.executionId(),
                                        run.model(),
                                        run.promptHash(),
                                        AiRunStatus.FAILED,
                                        run.promptTokens(),
                                        run.completionTokens(),
                                        run.estimatedUsage(),
                                        run.costSnapshot(),
                                        run.createdAt(),
                                        Instant.now(),
                                        "SERVICE_RESTART",
                                        run.version() + 1,
                                        run.lastEventSeq());
                        runtimeRuns.updateTerminalAndAppend(
                                failedRun,
                                run.version(),
                                AiRunEventType.RUN_FAILED,
                                "{\"errorCode\":\"SERVICE_RESTART\"}",
                                true);
                    }
                } catch (RuntimeException ignored) {
                }
            }
        }
        return recovered;
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param g 参数值，用于执行当前操作。
     * @param expectedVersion 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional
    public int update(GenerationRun g, long expectedVersion) {
        int updated =
                jdbc.update(
                        "UPDATE CHAT_GENERATION_RUN SET"
                            + " STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,LATENCY_MS=?,ERROR_CODE=?,LAST_EVENT_SEQUENCE=?,CANCEL_REQUESTED=?,VERSION=?,UPDATED_AT=?,RUNTIME_RUN_ID=COALESCE(?,RUNTIME_RUN_ID)"
                            + " WHERE ID=? AND VERSION=?",
                        g.status().name(),
                        g.promptTokens(),
                        g.completionTokens(),
                        g.latencyMs(),
                        g.errorCode(),
                        g.lastEventSequence(),
                        g.cancelRequested(),
                        g.version(),
                        ts(g.updatedAt()),
                        g.runtimeRunId(),
                        g.id(),
                        expectedVersion);
        if (updated == 1
                && (g.status() == GenerationStatus.COMPLETED
                        || g.status() == GenerationStatus.FAILED
                        || g.status() == GenerationStatus.CANCELLED)) {
            jdbc.update("DELETE FROM CHAT_GENERATION_ACTIVE WHERE GENERATION_ID=?", g.id());
        }
        return updated;
    }

    /**
     * {@code appendEvent} 执行当前类型定义的业务操作。
     *
     * @param e 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     */
    @Override
    public void appendEvent(GenerationEvent e, TenantId tenantId) {
        GenerationRuntimeLink link = runtimeLink(e.generationRunId(), tenantId);
        runtimeRuns.appendNextEvent(
                link.runtimeRunId(),
                tenantId,
                link.ownerUserId(),
                runtimeType(e.type()),
                e.payload(),
                true,
                e.createdAt());
    }

    /**
     * {@code listEvents} 查询并返回当前操作所需的数据。
     *
     * @param generationId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param afterSequence 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<GenerationEvent> listEvents(
            String generationId, TenantId tenantId, int afterSequence, int limit) {
        return jdbc.query(
                "SELECT e.SEQ,e.TYPE,e.PAYLOAD,e.CREATED_AT FROM AI_RUN_EVENT e JOIN"
                    + " CHAT_GENERATION_RUN g ON g.RUNTIME_RUN_ID=e.RUN_ID WHERE e.GENERATION_ID=?"
                    + " AND e.TENANT_ID=? AND g.TENANT_ID=? AND e.SEQ>? ORDER BY e.SEQ LIMIT ?",
                (r, n) ->
                        new GenerationEvent(
                                generationId,
                                r.getInt("SEQ"),
                                generationType(r.getString("TYPE")),
                                r.getString("PAYLOAD"),
                                r.getTimestamp("CREATED_AT").toInstant()),
                generationId,
                tenantId.value(),
                tenantId.value(),
                afterSequence,
                Math.min(Math.max(limit, 1), 1000));
    }

    /**
     * {@code nextEventSequence} 执行当前类型定义的业务操作。
     *
     * @param generationId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public int nextEventSequence(String generationId, TenantId tenantId) {
        Integer max =
                jdbc.queryForObject(
                        "SELECT COALESCE(MAX(e.SEQ),-1)+1 FROM AI_RUN_EVENT e JOIN"
                                + " CHAT_GENERATION_RUN g ON g.RUNTIME_RUN_ID=e.RUN_ID WHERE"
                                + " e.GENERATION_ID=? AND e.TENANT_ID=? AND g.TENANT_ID=?",
                        Integer.class,
                        generationId,
                        tenantId.value(),
                        tenantId.value());
        return max == null ? 0 : max;
    }

    private GenerationRuntimeLink runtimeLink(String generationId, TenantId tenantId) {
        GenerationRuntimeLink link =
                jdbc.query(
                        "SELECT g.RUNTIME_RUN_ID,c.OWNER_USER_ID FROM CHAT_GENERATION_RUN g JOIN"
                                + " CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID WHERE g.ID=? AND"
                                + " g.TENANT_ID=?",
                        rs ->
                                rs.next()
                                        ? new GenerationRuntimeLink(
                                                rs.getString("RUNTIME_RUN_ID"),
                                                rs.getLong("OWNER_USER_ID"))
                                        : null,
                        generationId,
                        tenantId.value());
        if (link == null) throw new IllegalArgumentException("generation not found");
        return link;
    }

    private AiRunEventType runtimeType(GenerationEventType type) {
        return switch (type) {
            case STARTED -> AiRunEventType.RUN_STARTED;
            case BLOCK_STARTED -> AiRunEventType.MODEL_BLOCK_STARTED;
            case DELTA -> AiRunEventType.MODEL_DELTA;
            case REASONING_DELTA -> AiRunEventType.MODEL_REASONING_DELTA;
            case TOOL_CALL -> AiRunEventType.MODEL_TOOL_CALL_DELTA;
            case BLOCK_COMPLETED -> AiRunEventType.MODEL_BLOCK_COMPLETED;
            case USAGE -> AiRunEventType.MODEL_USAGE;
            case COMPLETED -> AiRunEventType.RUN_COMPLETED;
            case FAILED -> AiRunEventType.RUN_FAILED;
            case CANCELLED -> AiRunEventType.RUN_CANCELLED;
        };
    }

    private GenerationEventType generationType(String type) {
        return switch (AiRunEventType.valueOf(type)) {
            case RUN_STARTED -> GenerationEventType.STARTED;
            case MODEL_BLOCK_STARTED -> GenerationEventType.BLOCK_STARTED;
            case MODEL_DELTA -> GenerationEventType.DELTA;
            case MODEL_REASONING_DELTA -> GenerationEventType.REASONING_DELTA;
            case MODEL_TOOL_CALL_DELTA -> GenerationEventType.TOOL_CALL;
            case MODEL_BLOCK_COMPLETED, MODEL_COMPLETED -> GenerationEventType.BLOCK_COMPLETED;
            case MODEL_USAGE -> GenerationEventType.USAGE;
            case RUN_COMPLETED -> GenerationEventType.COMPLETED;
            case RUN_FAILED -> GenerationEventType.FAILED;
            case RUN_CANCELLED -> GenerationEventType.CANCELLED;
            default -> GenerationEventType.DELTA;
        };
    }

    /**
     * {@code GenerationRuntimeLink} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param runtimeRunId runtimeRunId 属性，表示该记录组件承载的数据。
     * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
     */
    private record GenerationRuntimeLink(String runtimeRunId, long ownerUserId) {
        private GenerationRuntimeLink {
            if (runtimeRunId == null || runtimeRunId.isBlank())
                throw new IllegalStateException("generation runtime run is not linked");
        }
    }

    private GenerationRun map(ResultSet r, int n) throws java.sql.SQLException {
        return new GenerationRun(
                r.getString("ID"),
                r.getString("CONVERSATION_ID"),
                r.getString("INPUT_MESSAGE_ID"),
                r.getString("ASSISTANT_MESSAGE_ID"),
                r.getString("SPEAKER_ID"),
                r.getString("PLATFORM"),
                r.getString("MODEL"),
                GenerationStatus.valueOf(r.getString("STATUS")),
                r.getLong("PROMPT_TOKENS"),
                r.getLong("COMPLETION_TOKENS"),
                r.getLong("LATENCY_MS"),
                r.getString("ERROR_CODE"),
                r.getInt("LAST_EVENT_SEQUENCE"),
                r.getBoolean("CANCEL_REQUESTED"),
                r.getLong("VERSION"),
                r.getTimestamp("CREATED_AT").toInstant(),
                r.getTimestamp("UPDATED_AT").toInstant(),
                r.getString("RUNTIME_RUN_ID"));
    }

    private long currentTenant(GenerationRun run) {
        Long tenant =
                jdbc.queryForObject(
                        "SELECT TENANT_ID FROM CHAT_GENERATION_RUN WHERE ID=?",
                        Long.class,
                        run.id());
        if (tenant == null) throw new IllegalStateException("generation tenant not found");
        return tenant;
    }

    /**
     * {@code StaleGeneration} 封装会话模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param run run 属性，表示该记录组件承载的数据。
     * @param ownerUserId 所属用户标识，表示该记录组件承载的数据。
     */
    private record StaleGeneration(GenerationRun run, long ownerUserId) {}

    private static Timestamp ts(Instant i) {
        return Timestamp.from(i == null ? Instant.now() : i);
    }
}
