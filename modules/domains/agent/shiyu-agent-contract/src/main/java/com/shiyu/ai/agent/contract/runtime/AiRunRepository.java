package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * AiRunRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
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
     * 查询指定租户和所有者的 AI 运行记录列表。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     * @param limit 最大返回条数。
     *
     * @return 结果列表。
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
     * 执行 {@code appendNextEvent} 定义的接口操作。
     *
     * @param runId 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param type 对象类型。
     * @param payload 方法参数。
     * @param redacted 方法参数。
     * @param createdAt 方法参数。
     * @param turnId 方法参数。
     * @param stepId 方法参数。
     * @param providerRequestId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
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
     * 查询运行记录在指定序号之后的事件。
     *
     * @param runId 运行记录标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     * @param afterSeq afterSeq 参数。
     * @param limit 最大返回条数。
     *
     * @return 结果列表。
     */
    List<AiRunEvent> events(
            String runId, TenantId tenantId, long ownerUserId, long afterSeq, int limit);
}
