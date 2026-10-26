package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 负责 AI 运行 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface AiRunRepository {
    /**
     * 保存 AI 运行记录。
     *
     * @param run 运行记录。
     */
    void insert(AiRun run);

    /**
     * 根据运行标识、租户和所有者查询 AI 运行记录。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     *
     * @return 匹配结果；不存在时返回空。
     */
    Optional<AiRun> find(String id, TenantId tenantId, long ownerUserId);

    /**
     * 查询 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AiRun> list(TenantId tenantId, long ownerUserId, int limit);

    /**
     * 根据生成记录标识查询关联的 AI 运行记录。
     *
     * @param generationId 生成记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     *
     * @return 匹配结果；不存在时返回空。
     */
    Optional<AiRun> findByGeneration(String generationId, TenantId tenantId, long ownerUserId);

    /**
     * 将 AI 运行记录与生成记录关联。
     *
     * @param runId 运行记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     * @param generationId 生成记录标识。
     *
     * @return 受影响的记录数或生成的序号。
     */
    int linkGeneration(String runId, TenantId tenantId, long ownerUserId, String generationId);

    /**
     * 根据执行记录标识查询关联的 AI 运行记录。
     *
     * @param executionId 执行记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     *
     * @return 匹配结果；不存在时返回空。
     */
    Optional<AiRun> findByExecution(String executionId, TenantId tenantId, long ownerUserId);

    /**
     * 按期望版本更新 AI 运行记录。
     *
     * @param run 运行记录。
     * @param expectedVersion 期望版本号。
     *
     * @return 受影响的记录数或生成的序号。
     */
    int update(AiRun run, long expectedVersion);

    /**
     * 更新终态运行记录并追加对应事件。
     *
     * @param run 运行记录。
     * @param expectedVersion 期望版本号。
     * @param eventType eventType 参数。
     * @param payload 事件载荷。
     * @param redacted redacted 参数。
     *
     * @return 处理结果。
     */
    default AiRun updateTerminalAndAppend(
            AiRun run,
            long expectedVersion,
            AiRunEventType eventType,
            String payload,
            boolean redacted) {
        if (update(run, expectedVersion) != 1) throw new IllegalStateException("run was modified");
        long seq =
                appendNextEvent(
                        run.id(),
                        run.tenantId(),
                        run.ownerUserId().value(),
                        eventType,
                        payload == null ? "{}" : payload,
                        redacted,
                        Instant.now());
        return run.withLastEventSeq(seq);
    }

    /**
     * 按生成终态完成关联运行记录，并追加对应的终态事件。
     *
     * @param id 运行记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     * @param terminal 目标终态。
     * @param errorCode 失败或取消原因。
     * @return 已完成的运行记录。
     */
    default AiRun finishGenerationRun(
            String id,
            TenantId tenantId,
            long ownerUserId,
            AiRunStatus terminal,
            String errorCode,
            List<AiRunEventType> precedingEvents,
            String turnId,
            String stepId) {
        AiRun current =
                find(id, tenantId, ownerUserId)
                        .orElseThrow(() -> new IllegalArgumentException("run not found"));
        AiRunEventType eventType =
                terminal == AiRunStatus.COMPLETED
                        ? AiRunEventType.RUN_COMPLETED
                        : terminal == AiRunStatus.CANCELLED
                                ? AiRunEventType.RUN_CANCELLED
                                : AiRunEventType.RUN_FAILED;
        String payload = terminalPayload(errorCode);
        if (current.status() == terminal) {
            long seq = current.lastEventSeq();
            if (seq > 0) {
                List<AiRunEvent> lastEvent = events(id, tenantId, ownerUserId, seq - 1, 1);
                if (!lastEvent.isEmpty()) {
                    AiRunEvent event = lastEvent.getFirst();
                    if (event.seq() == seq
                            && event.type() == eventType
                            && java.util.Objects.equals(event.payload(), payload)
                            && event.redacted()) {
                        return current;
                    }
                }
            }
            long appendedSeq = appendNextEvent(
                    id, tenantId, ownerUserId, eventType, payload,
                    true, Instant.now(), turnId, stepId, null);
            return current.withLastEventSeq(appendedSeq);
        }
        if (current.status() == AiRunStatus.COMPLETED
                || current.status() == AiRunStatus.FAILED
                || current.status() == AiRunStatus.CANCELLED) {
            throw new IllegalStateException("run already has a different terminal status");
        }
        AiRun transitioned = current.transition(terminal);
        AiRun next =
                new AiRun(
                        transitioned.id(), transitioned.tenantId(), transitioned.ownerUserId(),
                        transitioned.appId(), transitioned.appVersionId(), transitioned.sourceType(),
                        transitioned.sourceId(), transitioned.parentRunId(), transitioned.traceId(),
                        transitioned.conversationId(), transitioned.generationId(), transitioned.executionId(),
                        transitioned.model(), transitioned.promptHash(), transitioned.status(),
                        transitioned.promptTokens(), transitioned.completionTokens(), transitioned.estimatedUsage(),
                        transitioned.costSnapshot(), transitioned.createdAt(), transitioned.completedAt(), errorCode,
                        transitioned.version(), transitioned.lastEventSeq());
        for (AiRunEventType event : precedingEvents == null ? List.<AiRunEventType>of() : precedingEvents) {
            appendNextEvent(id, tenantId, ownerUserId, event, "{}", true, Instant.now(), turnId, stepId, null);
        }
        return updateTerminalAndAppend(next, current.version(), eventType, payload, true);
    }

    private static String terminalPayload(String errorCode) {
        if (errorCode == null) return "{}";
        StringBuilder payload = new StringBuilder("{\"errorCode\":\"");
        for (int i = 0; i < errorCode.length(); i++) {
            char value = errorCode.charAt(i);
            switch (value) {
                case '\\' -> payload.append("\\\\");
                case '"' -> payload.append("\\\"");
                case '\b' -> payload.append("\\b");
                case '\f' -> payload.append("\\f");
                case '\n' -> payload.append("\\n");
                case '\r' -> payload.append("\\r");
                case '\t' -> payload.append("\\t");
                default -> {
                    if (value < 0x20) {
                        payload.append(String.format("\\u%04x", (int) value));
                    } else {
                        payload.append(value);
                    }
                }
            }
        }
        return payload.append("\"}").toString();
    }

    /**
     * 为运行记录追加下一个事件并分配序号。
     *
     * @param runId 运行记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     * @param type 数据类型。
     * @param payload 事件载荷。
     * @param redacted redacted 参数。
     * @param createdAt createdAt 参数。
     *
     * @return 受影响的记录数或生成的序号。
     */
    default long appendNextEvent(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt) {
        throw new UnsupportedOperationException(
                "database event sequence allocation is not configured");
    }

    /**
     * 执行 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param redacted 用于完成本次业务处理的 redacted 参数。
     * @param createdAt 用于完成本次业务处理的 createdAt 参数。
     * @param turnId 用于定位turn的标识。
     * @param stepId 用于定位step的标识。
     * @param providerRequestId 用于定位provider的标识。
     * @return 返回 AI 运行 相关操作生成的结果数据。
     */
    default long appendNextEvent(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            AiRunEventType type,
            String payload,
            boolean redacted,
            Instant createdAt,
            String turnId,
            String stepId,
            String providerRequestId) {
        return appendNextEvent(runId, tenantId, ownerUserId, type, payload, redacted, createdAt);
    }

    /**
     * 追加事件。
     *
     * @param event 领域事件。
     *
     * @return 受影响的记录数或生成的序号。
     */
    long appendEvent(AiRunEvent event);

    /**
     * 执行 AI 运行 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param afterSeq 用于完成本次业务处理的 afterSeq 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AiRunEvent> events(
            String runId, TenantId tenantId, long ownerUserId, long afterSeq, int limit);
}
