package com.shiyu.ai.conversation.implementation.infrastructure.persistence.repository;

import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.conversation.implementation.domain.model.*;
import com.shiyu.ai.conversation.implementation.domain.port.ConversationRepository;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.TenantScope;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

/**
 * 负责 Jdbc 会话 的持久化查询、保存和删除，并维护数据访问边界。
 */
@Component
public class JdbcConversationRepository implements ConversationRepository {
    /**
     * JDBC，表示当前对象中的对应属性。
     */
    private final JdbcTemplate jdbc;
    private final com.shiyu.ai.agent.contract.runtime.AiRunRepository runtimeRuns;

    /**
     * 执行 Jdbc 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param dataSource 用于完成本次业务处理的 dataSource 参数。
     */
    public JdbcConversationRepository(@Qualifier("agentDataSource") DataSource dataSource) {
        this(dataSource, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public JdbcConversationRepository(
            @Qualifier("agentDataSource") DataSource dataSource,
            com.shiyu.ai.agent.contract.runtime.AiRunRepository runtimeRuns) {
        this.jdbc = new JdbcTemplate(dataSource);
        this.runtimeRuns = runtimeRuns;
    }

    /**
     * 创建或保存 Jdbc 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param c 用于完成本次业务处理的 c 参数。
     */
    @Override
    public void insertConversation(Conversation c) {
        requireTenant(c.tenantId());
        jdbc.update(
                "INSERT INTO CHAT_CONVERSATION"
                    + " (ID,TENANT_ID,OWNER_USER_ID,SCENE_TYPE,TITLE,STATUS,PARENT_CONVERSATION_ID,BRANCH_FROM_MESSAGE_ID,ACTIVE_LEAF_MESSAGE_ID,ROLLING_SUMMARY,PLATFORM,MODEL,VERSION,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                c.id(),
                c.tenantId(),
                c.ownerUserId(),
                c.sceneType(),
                c.title(),
                c.status().name(),
                c.parentConversationId(),
                c.branchFromMessageId(),
                c.activeLeafMessageId(),
                c.rollingSummary(),
                c.platform(),
                c.model(),
                c.version(),
                ts(c.createdAt()),
                ts(c.updatedAt()));
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<Conversation> findConversation(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc
                .query(
                        "SELECT * FROM CHAT_CONVERSATION WHERE ID=? AND TENANT_ID=? AND"
                                + " OWNER_USER_ID=? AND STATUS<>'DELETED'",
                        this::mapConversation,
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Conversation> listConversations(
            TenantId tenantId, long ownerUserId, int limit, int offset) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT * FROM CHAT_CONVERSATION WHERE TENANT_ID=? AND OWNER_USER_ID=? AND"
                        + " STATUS<>'DELETED' ORDER BY UPDATED_AT DESC,ID ASC LIMIT ? OFFSET ?",
                this::mapConversation,
                tenantId.value(),
                ownerUserId,
                Math.min(Math.max(limit, 1), 100),
                Math.max(offset, 0));
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param parentConversationId 用于定位parent 会话的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<Conversation> listBranches(
            String parentConversationId, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT * FROM CHAT_CONVERSATION WHERE PARENT_CONVERSATION_ID=? AND TENANT_ID=? AND"
                        + " OWNER_USER_ID=? AND STATUS<>'DELETED' ORDER BY UPDATED_AT DESC,ID ASC",
                this::mapConversation,
                parentConversationId,
                tenantId.value(),
                ownerUserId);
    }

    /**
     * 更新或设置 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param c 用于完成本次业务处理的 c 参数。
     * @param expectedVersion 用于完成本次业务处理的 expectedVersion 参数。
     * @return 返回 Jdbc 会话 相关操作生成的结果数据。
     */
    @Override
    public int updateConversation(Conversation c, long expectedVersion) {
        requireTenant(c.tenantId());
        return jdbc.update(
                "UPDATE CHAT_CONVERSATION SET"
                    + " TITLE=?,STATUS=?,ACTIVE_LEAF_MESSAGE_ID=?,ROLLING_SUMMARY=?,VERSION=?,UPDATED_AT=?"
                    + " WHERE ID=? AND TENANT_ID=? AND OWNER_USER_ID=? AND VERSION=?",
                c.title(),
                c.status().name(),
                c.activeLeafMessageId(),
                c.rollingSummary(),
                c.version(),
                ts(c.updatedAt()),
                c.id(),
                c.tenantId(),
                c.ownerUserId(),
                expectedVersion);
    }

    /**
     * 创建或保存 Jdbc 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param m 用于完成本次业务处理的 m 参数。
     */
    @Override
    public void insertMessage(ConversationMessage m) {
        Long tenant =
                jdbc.queryForObject(
                        "SELECT TENANT_ID FROM CHAT_CONVERSATION WHERE ID=?",
                        Long.class,
                        m.conversationId());
        if (tenant == null) throw new IllegalArgumentException("conversation not found");
        requireTenant(tenant);
        jdbc.update(
                "INSERT INTO CHAT_MESSAGE"
                    + " (ID,TENANT_ID,CONVERSATION_ID,PARENT_MESSAGE_ID,SOURCE_MESSAGE_ID,ROLE,CONTENT,CONTENT_PARTS,TOOL_CALL,STATUS,SEQUENCE,GENERATION_ID,CREATED_AT,UPDATED_AT)"
                    + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                m.id(),
                tenant,
                m.conversationId(),
                m.parentMessageId(),
                m.sourceMessageId(),
                m.role().name(),
                m.textContent(),
                JSONUtils.toJsonString(m.contentParts()),
                JSONUtils.toJsonString(m.toolCall()),
                m.status().name(),
                m.sequence(),
                m.generationId(),
                ts(m.createdAt()),
                ts(m.updatedAt()));
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<ConversationMessage> findMessage(
            String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc
                .query(
                        "SELECT m.* FROM CHAT_MESSAGE m JOIN CHAT_CONVERSATION c ON"
                                + " c.ID=m.CONVERSATION_ID WHERE m.ID=? AND m.TENANT_ID=? AND"
                                + " c.OWNER_USER_ID=?",
                        this::mapMessage,
                        id,
                        tenantId.value(),
                        ownerUserId)
                .stream()
                .findFirst();
    }

    /**
     * 查询 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param conversationId 用于定位conversation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ConversationMessage> listMessages(
            String conversationId, TenantId tenantId, long ownerUserId, int limit) {
        TenantScope.requireMatches(tenantId);
        return jdbc.query(
                "SELECT m.* FROM CHAT_MESSAGE m JOIN CHAT_CONVERSATION c ON c.ID=m.CONVERSATION_ID"
                    + " WHERE m.CONVERSATION_ID=? AND m.TENANT_ID=? AND c.OWNER_USER_ID=? ORDER BY"
                    + " m.SEQUENCE DESC,m.ID ASC LIMIT ?",
                this::mapMessage,
                conversationId,
                tenantId.value(),
                ownerUserId,
                Math.min(Math.max(limit, 1), 1000));
    }

    /**
     * 在事务中以版本条件完成生成，保存助手消息和会话，并登记运行事件及后处理任务。
     *
     * @param completed 待写入的生成完成快照
     * @param expectedGenerationVersion 生成记录更新前应匹配的版本
     * @param assistant 本轮生成的助手消息
     * @param updatedConversation 包含本轮更新的会话快照
     * @param expectedConversationVersion 会话更新前应匹配的版本
     * @param runtimeEvents 需要追加到关联运行的终态事件
     * @return 完成写入时为 1，生成状态或版本不匹配时为 0
     */
    @Override
    @org.springframework.transaction.annotation.Transactional
    public int completeGeneration(
            com.shiyu.ai.conversation.contract.model.GenerationRun completed,
            long expectedGenerationVersion,
            ConversationMessage assistant,
            Conversation updatedConversation,
            long expectedConversationVersion,
            List<com.shiyu.ai.agent.contract.runtime.AiRunEventType> runtimeEvents) {
        TenantId tenantId = new TenantId(updatedConversation.tenantId());
        requireTenant(tenantId.value());
        if (!assistant.conversationId().equals(updatedConversation.id())
                || assistant.role() != MessageRole.ASSISTANT
                || !assistant.id().equals(completed.assistantMessageId())) {
            throw new IllegalArgumentException("assistant message does not belong to generation");
        }
        if (completed.runtimeRunId() == null && runtimeRuns != null) {
            String linkedRuntimeId = jdbc.query(
                    "SELECT RUNTIME_RUN_ID FROM CHAT_GENERATION_RUN WHERE ID=? AND TENANT_ID=?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    completed.id(), tenantId.value());
            if (linkedRuntimeId != null) completed = completed.withRuntimeRunId(linkedRuntimeId);
        }
        int updatedGeneration =
                jdbc.update(
                        "UPDATE CHAT_GENERATION_RUN SET STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,LATENCY_MS=?,ERROR_CODE=?,LAST_EVENT_SEQUENCE=?,CANCEL_REQUESTED=?,VERSION=?,UPDATED_AT=?,ASSISTANT_MESSAGE_ID=?,RUNTIME_RUN_ID=COALESCE(?,RUNTIME_RUN_ID) WHERE ID=? AND TENANT_ID=? AND STATUS='RUNNING' AND VERSION=? AND CANCEL_REQUESTED=FALSE",
                        completed.status().name(),
                        completed.promptTokens(),
                        completed.completionTokens(),
                        completed.latencyMs(),
                        completed.errorCode(),
                        completed.lastEventSequence(),
                        completed.cancelRequested(),
                        completed.version(),
                        ts(completed.updatedAt()),
                        assistant.id(),
                        completed.runtimeRunId(),
                        completed.id(),
                        tenantId.value(),
                        expectedGenerationVersion);
        if (updatedGeneration != 1) return 0;
        try {
            insertMessage(assistant);
            int updatedConversationRows =
                    updateConversation(updatedConversation, expectedConversationVersion);
            if (updatedConversationRows != 1) {
                throw new IllegalStateException("conversation was modified during completion");
            }
            jdbc.update(
                    "DELETE FROM CHAT_GENERATION_ACTIVE WHERE GENERATION_ID=? AND TENANT_ID=?",
                    completed.id(),
                    tenantId.value());
            if (completed.runtimeRunId() != null) {
                runtimeRuns.finishGenerationRun(
                        completed.runtimeRunId(),
                        tenantId,
                        updatedConversation.ownerUserId(),
                        com.shiyu.ai.agent.contract.runtime.AiRunStatus.COMPLETED,
                        null,
                        runtimeEvents,
                        "turn-" + completed.id(),
                        "model-" + completed.id());
            }
            enqueueGenerationPostProcessing(completed, tenantId, updatedConversation.ownerUserId());
            return 1;
        } catch (RuntimeException failure) {
            throw failure;
        }
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int transitionRunningGeneration(
            com.shiyu.ai.conversation.contract.model.GenerationRun terminal,
            long expectedGenerationVersion,
            long ownerUserId,
            List<com.shiyu.ai.agent.contract.runtime.AiRunEventType> runtimeEvents) {
        TenantId tenantId = TenantScope.current().orElseThrow(
                () -> new IllegalStateException("tenant scope is required"));
        requireTenant(tenantId.value());
        if (terminal.status() != com.shiyu.ai.conversation.contract.model.GenerationStatus.FAILED
                && terminal.status() != com.shiyu.ai.conversation.contract.model.GenerationStatus.CANCELLED) {
            throw new IllegalArgumentException("terminal status must be FAILED or CANCELLED");
        }
        if (terminal.runtimeRunId() == null) {
            String linkedRuntimeId = jdbc.query(
                    "SELECT RUNTIME_RUN_ID FROM CHAT_GENERATION_RUN WHERE ID=? AND TENANT_ID=?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    terminal.id(), tenantId.value());
            if (linkedRuntimeId != null) terminal = terminal.withRuntimeRunId(linkedRuntimeId);
        }
        int updated =
                jdbc.update(
                        "UPDATE CHAT_GENERATION_RUN SET STATUS=?,PROMPT_TOKENS=?,COMPLETION_TOKENS=?,LATENCY_MS=?,ERROR_CODE=?,LAST_EVENT_SEQUENCE=?,CANCEL_REQUESTED=?,VERSION=?,UPDATED_AT=?,RUNTIME_RUN_ID=COALESCE(?,RUNTIME_RUN_ID) WHERE ID=? AND TENANT_ID=? AND STATUS='RUNNING' AND VERSION=?",
                        terminal.status().name(), terminal.promptTokens(), terminal.completionTokens(),
                        terminal.latencyMs(), terminal.errorCode(), terminal.lastEventSequence(),
                        terminal.cancelRequested(), terminal.version(), ts(terminal.updatedAt()),
                        terminal.runtimeRunId(), terminal.id(), tenantId.value(), expectedGenerationVersion);
        if (updated != 1) return 0;
        if (terminal.runtimeRunId() != null && !terminal.runtimeRunId().isBlank()) {
            var runtimeStatus = terminal.status()
                    == com.shiyu.ai.conversation.contract.model.GenerationStatus.CANCELLED
                    ? com.shiyu.ai.agent.contract.runtime.AiRunStatus.CANCELLED
                    : com.shiyu.ai.agent.contract.runtime.AiRunStatus.FAILED;
            runtimeRuns.finishGenerationRun(
                    terminal.runtimeRunId(), tenantId, ownerUserId, runtimeStatus,
                    terminal.errorCode(), runtimeEvents,
                    "turn-" + terminal.id(), "model-" + terminal.id());
        }
        jdbc.update(
                "DELETE FROM CHAT_GENERATION_ACTIVE WHERE GENERATION_ID=? AND TENANT_ID=?",
                terminal.id(), tenantId.value());
        enqueueGenerationPostProcessing(terminal, tenantId, ownerUserId);
        return 1;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public java.util.Optional<com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing>
            claimGenerationPostProcessingNext() {
        String generationId =
                jdbc.query(
                        "SELECT GENERATION_ID FROM CHAT_GENERATION_POSTPROCESSING WHERE NEXT_ATTEMPT_AT<=CURRENT_TIMESTAMP AND (CLAIMED_UNTIL IS NULL OR CLAIMED_UNTIL<CURRENT_TIMESTAMP) ORDER BY NEXT_ATTEMPT_AT,GENERATION_ID LIMIT 1",
                        rs -> rs.next() ? rs.getString(1) : null);
        if (generationId == null) return java.util.Optional.empty();
        return claimGenerationPostProcessing(generationId);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public java.util.Optional<com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing>
            claimGenerationPostProcessing(String generationId) {
        String claimToken = java.util.UUID.randomUUID().toString();
        int updated =
                jdbc.update(
                        "UPDATE CHAT_GENERATION_POSTPROCESSING SET CLAIM_TOKEN=?,CLAIMED_UNTIL=? WHERE GENERATION_ID=? AND NEXT_ATTEMPT_AT<=CURRENT_TIMESTAMP AND (CLAIMED_UNTIL IS NULL OR CLAIMED_UNTIL<CURRENT_TIMESTAMP)",
                        claimToken,
                        ts(Instant.now().plusSeconds(300)),
                        generationId);
        return updated == 1
                ? mapClaimedPostProcessing(generationId, claimToken)
                : java.util.Optional.empty();
    }

    private java.util.Optional<com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing>
            mapClaimedPostProcessing(String generationId, String claimToken) {
        String claimFilter =
                claimToken == null
                        ? "p.CLAIM_TOKEN IS NULL"
                        : "p.CLAIM_TOKEN=?";
        Object[] params =
                claimToken == null
                        ? new Object[] {generationId}
                        : new Object[] {generationId, claimToken};
        return jdbc.query(
                "SELECT p.QUOTA_ACTION,p.USAGE_ACTION,p.QUOTA_DONE,p.USAGE_DONE,p.ATTEMPTS,g.*,c.OWNER_USER_ID FROM CHAT_GENERATION_POSTPROCESSING p JOIN CHAT_GENERATION_RUN g ON g.ID=p.GENERATION_ID AND g.TENANT_ID=p.TENANT_ID JOIN CHAT_CONVERSATION c ON c.ID=g.CONVERSATION_ID AND c.TENANT_ID=g.TENANT_ID WHERE p.GENERATION_ID=? AND " + claimFilter,
                rs -> {
                    if (!rs.next()) return java.util.Optional.empty();
                    long tenantValue = rs.getLong("TENANT_ID");
                    return java.util.Optional.of(
                            new com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing(
                                    mapGeneration(rs),
                                    new TenantId(tenantValue),
                                    rs.getLong("OWNER_USER_ID"),
                                    com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.QuotaAction.valueOf(rs.getString("QUOTA_ACTION")),
                                    com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.UsageAction.valueOf(rs.getString("USAGE_ACTION")),
                                    rs.getBoolean("QUOTA_DONE"),
                                    rs.getBoolean("USAGE_DONE"),
                                    rs.getInt("ATTEMPTS"),
                                    claimToken));
                },
                params);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int completeGenerationPostProcessing(
            String generationId,
            com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.Step step,
            String claimToken) {
        String column = step == com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.Step.QUOTA
                ? "QUOTA_DONE"
                : "USAGE_DONE";
        int updated =
                jdbc.update(
                        "UPDATE CHAT_GENERATION_POSTPROCESSING SET "
                                + column
                                + "=TRUE WHERE GENERATION_ID=? AND CLAIM_TOKEN=?",
                        generationId,
                        claimToken);
        if (updated == 1) {
            jdbc.update(
                    "DELETE FROM CHAT_GENERATION_POSTPROCESSING WHERE GENERATION_ID=? AND QUOTA_DONE=TRUE AND USAGE_DONE=TRUE AND CLAIM_TOKEN=?",
                    generationId,
                    claimToken);
        }
        return updated;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public int releaseGenerationPostProcessing(
            String generationId, String claimToken, long retryDelayMillis) {
        return jdbc.update(
                "UPDATE CHAT_GENERATION_POSTPROCESSING SET ATTEMPTS=ATTEMPTS+1,NEXT_ATTEMPT_AT=?,CLAIM_TOKEN=NULL,CLAIMED_UNTIL=NULL WHERE GENERATION_ID=? AND CLAIM_TOKEN=?",
                ts(Instant.now().plusMillis(Math.max(1000L, retryDelayMillis))),
                generationId,
                claimToken);
    }

    @Override
    public int deleteConversation(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.update(
                "UPDATE CHAT_CONVERSATION SET"
                    + " STATUS='DELETED',VERSION=VERSION+1,UPDATED_AT=CURRENT_TIMESTAMP WHERE ID=?"
                    + " AND TENANT_ID=? AND OWNER_USER_ID=? AND STATUS<>'DELETED'",
                id,
                tenantId.value(),
                ownerUserId);
    }

    /**
     * 删除或移除 Jdbc 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 Jdbc 会话 相关操作生成的结果数据。
     */
    @Override
    public int deleteMessage(String id, TenantId tenantId, long ownerUserId) {
        TenantScope.requireMatches(tenantId);
        return jdbc.update(
                "DELETE FROM CHAT_MESSAGE WHERE ID=? AND TENANT_ID=? AND CONVERSATION_ID IN (SELECT"
                        + " ID FROM CHAT_CONVERSATION WHERE TENANT_ID=? AND OWNER_USER_ID=?)"
                        + " AND NOT EXISTS (SELECT 1 FROM CHAT_MESSAGE child"
                        + " WHERE child.TENANT_ID=CHAT_MESSAGE.TENANT_ID"
                        + " AND child.PARENT_MESSAGE_ID=CHAT_MESSAGE.ID)"
                        + " AND NOT EXISTS (SELECT 1 FROM CHAT_CONVERSATION reference"
                        + " WHERE reference.TENANT_ID=CHAT_MESSAGE.TENANT_ID"
                        + " AND (reference.ACTIVE_LEAF_MESSAGE_ID=CHAT_MESSAGE.ID"
                        + " OR reference.BRANCH_FROM_MESSAGE_ID=CHAT_MESSAGE.ID))",
                id,
                tenantId.value(),
                tenantId.value(),
                ownerUserId);
    }

    private void enqueueGenerationPostProcessing(
            com.shiyu.ai.conversation.contract.model.GenerationRun run,
            TenantId tenantId,
            long ownerUserId) {
        var quotaAction =
                run.status() == com.shiyu.ai.conversation.contract.model.GenerationStatus.COMPLETED
                        ? com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.QuotaAction.SETTLE
                        : com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.QuotaAction.RELEASE;
        var usageAction =
                switch (run.status()) {
                    case COMPLETED -> com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.UsageAction.COMPLETED;
                    case FAILED -> com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.UsageAction.FAILED;
                    case CANCELLED -> com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.UsageAction.NONE;
                    default -> throw new IllegalArgumentException("generation must be terminal");
                };
        jdbc.update(
                "INSERT INTO CHAT_GENERATION_POSTPROCESSING (GENERATION_ID,TENANT_ID,OWNER_USER_ID,QUOTA_ACTION,USAGE_ACTION,QUOTA_DONE,USAGE_DONE,ATTEMPTS,NEXT_ATTEMPT_AT,CREATED_AT) VALUES (?,?,?,?,?,?,?,?,?,?)",
                run.id(),
                tenantId.value(),
                ownerUserId,
                quotaAction.name(),
                usageAction.name(),
                quotaAction == com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.QuotaAction.NONE,
                usageAction == com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.UsageAction.NONE,
                0,
                ts(Instant.now()),
                ts(Instant.now()));
    }

    private com.shiyu.ai.conversation.contract.model.GenerationRun mapGeneration(
            ResultSet r) throws java.sql.SQLException {
        return new com.shiyu.ai.conversation.contract.model.GenerationRun(
                r.getString("ID"),
                r.getString("CONVERSATION_ID"),
                r.getString("INPUT_MESSAGE_ID"),
                r.getString("ASSISTANT_MESSAGE_ID"),
                r.getString("SPEAKER_ID"),
                r.getString("PLATFORM"),
                r.getString("MODEL"),
                com.shiyu.ai.conversation.contract.model.GenerationStatus.valueOf(r.getString("STATUS")),
                r.getLong("PROMPT_TOKENS"),
                r.getLong("COMPLETION_TOKENS"),
                r.getLong("LATENCY_MS"),
                r.getString("ERROR_CODE"),
                r.getInt("LAST_EVENT_SEQUENCE"),
                r.getBoolean("CANCEL_REQUESTED"),
                r.getLong("VERSION"),
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")),
                r.getString("RUNTIME_RUN_ID"));
    }

    private Conversation mapConversation(ResultSet r, int n) throws java.sql.SQLException {
        return new Conversation(
                r.getString("ID"),
                r.getLong("TENANT_ID"),
                r.getLong("OWNER_USER_ID"),
                r.getString("SCENE_TYPE"),
                r.getString("TITLE"),
                ConversationStatus.valueOf(r.getString("STATUS")),
                r.getString("PARENT_CONVERSATION_ID"),
                r.getString("BRANCH_FROM_MESSAGE_ID"),
                r.getString("ACTIVE_LEAF_MESSAGE_ID"),
                r.getString("ROLLING_SUMMARY"),
                r.getString("PLATFORM"),
                r.getString("MODEL"),
                r.getLong("VERSION"),
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")));
    }

    private ConversationMessage mapMessage(ResultSet r, int n) throws java.sql.SQLException {
        List<ContentPart> parts =
                java.util.Optional.ofNullable(r.getString("CONTENT_PARTS"))
                        .map(json -> JSONUtils.parseArray(json, ContentPart.class))
                        .orElse(List.of(ContentPart.text(r.getString("CONTENT"))));
        java.util.Map<String, Object> toolCall =
                java.util.Optional.ofNullable(r.getString("TOOL_CALL"))
                        .map(JSONUtils::parseMap)
                        .orElse(java.util.Map.of());
        return new ConversationMessage(
                r.getString("ID"),
                r.getString("CONVERSATION_ID"),
                r.getString("PARENT_MESSAGE_ID"),
                r.getString("SOURCE_MESSAGE_ID"),
                MessageRole.valueOf(r.getString("ROLE")),
                parts,
                toolCall,
                MessageStatus.valueOf(r.getString("STATUS")),
                r.getInt("SEQUENCE"),
                r.getString("GENERATION_ID"),
                instant(r.getTimestamp("CREATED_AT")),
                instant(r.getTimestamp("UPDATED_AT")));
    }

    private static Timestamp ts(Instant i) {
        return Timestamp.from(i == null ? Instant.now() : i);
    }

    private static Instant instant(Timestamp t) {
        return t == null ? null : t.toInstant();
    }

    private static void requireTenant(long tenantId) {
        TenantScope.requireMatches(new TenantId(tenantId));
    }
}
