package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * AiRuntimePort 边界接口，负责向外部组件提供智能体领域相关能力。
 */
public interface AiRuntimePort {
    /**
     * 变更当前业务对象的处理状态。
     *
     * @param context 方法参数。
     * @param source 方法参数。
     * @param sourceId 方法参数。
     * @param model 方法参数。
     * @param prompt 方法参数。
     *
     * @return 操作结果。
     */
    AiRun startRun(
            AiRunContext context, AiRunSource source, String sourceId, String model, String prompt);

    /**
     * 执行 {@code finish} 定义的接口操作。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param terminal 方法参数。
     * @param errorCode 方法参数。
     *
     * @return 操作结果。
     */
    AiRun finish(
            String id, TenantId tenantId, long ownerUserId, AiRunStatus terminal, String errorCode);

    /**
     * 执行 {@code events} 定义的接口操作。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param afterSeq 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<AiRunEvent> events(
            String id, TenantId tenantId, long ownerUserId, long afterSeq, int limit);

    /**
     * 执行 {@code requireRun} 定义的接口操作。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 操作结果。
     */
    AiRun requireRun(String id, TenantId tenantId, long ownerUserId);

    /**
     * 执行 {@code requireGenerationRun} 定义的接口操作。
     *
     * @param generationId 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 操作结果。
     */
    AiRun requireGenerationRun(String generationId, TenantId tenantId, long ownerUserId);

    /**
     * 执行 {@code linkGeneration} 定义的接口操作。
     *
     * @param run 方法参数。
     * @param generationId 方法参数。
     *
     * @return 操作结果。
     */
    AiRun linkGeneration(AiRun run, String generationId);

    /**
     * 执行 {@code requireExecutionRun} 定义的接口操作。
     *
     * @param executionId 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 操作结果。
     */
    AiRun requireExecutionRun(String executionId, TenantId tenantId, long ownerUserId);

    /**
     * 执行 {@code recordUsage} 定义的接口操作。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param promptTokens 方法参数。
     * @param completionTokens 方法参数。
     * @param estimated 方法参数。
     * @param costSnapshot 方法参数。
     *
     * @return 操作结果。
     */
    AiRun recordUsage(
            String id,
            TenantId tenantId,
            long ownerUserId,
            long promptTokens,
            long completionTokens,
            boolean estimated,
            String costSnapshot);

    /**
     * 执行 {@code append} 定义的接口操作。
     *
     * @param run 方法参数。
     * @param type 对象类型。
     * @param payload 方法参数。
     * @param redacted 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long append(AiRun run, AiRunEventType type, String payload, boolean redacted);

    /**
     * 执行 {@code append} 定义的接口操作。
     *
     * @param run 方法参数。
     * @param type 对象类型。
     * @param payload 方法参数。
     * @param redacted 方法参数。
     * @param turnId 方法参数。
     * @param stepId 方法参数。
     * @param providerRequestId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    long append(
            AiRun run,
            AiRunEventType type,
            String payload,
            boolean redacted,
            String turnId,
            String stepId,
            String providerRequestId);
}
