package com.shiyu.ai.agent.contract.runtime;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 定义 AI Runtime 领域与外部能力交互的端口契约。
 */
public interface AiRuntimePort {
    /**
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param context 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param source 用于完成本次业务处理的 source 参数。
     * @param sourceId 用于定位source的标识。
     * @param model 用于完成本次业务处理的 model 参数。
     * @param prompt 用于完成本次业务处理的 prompt 参数。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    AiRun startRun(
            AiRunContext context, AiRunSource source, String sourceId, String model, String prompt);

    /**
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param terminal 用于完成本次业务处理的 terminal 参数。
     * @param errorCode 用于完成本次业务处理的 errorCode 参数。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    AiRun finish(
            String id, TenantId tenantId, long ownerUserId, AiRunStatus terminal, String errorCode);

    /**
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param afterSeq 用于完成本次业务处理的 afterSeq 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<AiRunEvent> events(
            String id, TenantId tenantId, long ownerUserId, long afterSeq, int limit);

    /**
     * 获取并校验 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    AiRun requireRun(String id, TenantId tenantId, long ownerUserId);

    /**
     * 获取并校验 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param generationId 用于定位generation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    AiRun requireGenerationRun(String generationId, TenantId tenantId, long ownerUserId);

    /**
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param generationId 用于定位generation的标识。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    AiRun linkGeneration(AiRun run, String generationId);

    /**
     * 获取并校验 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param executionId 用于定位execution的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    AiRun requireExecutionRun(String executionId, TenantId tenantId, long ownerUserId);

    /**
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param promptTokens 用于完成本次业务处理的 promptTokens 参数。
     * @param completionTokens 用于完成本次业务处理的 completionTokens 参数。
     * @param estimated 用于完成本次业务处理的 estimated 参数。
     * @param costSnapshot 用于完成本次业务处理的 costSnapshot 参数。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
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
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param redacted 用于完成本次业务处理的 redacted 参数。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
     */
    long append(AiRun run, AiRunEventType type, String payload, boolean redacted);

    /**
     * 执行 AI Runtime 相关业务数据，并返回处理结果。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param type 用于完成本次业务处理的 type 参数。
     * @param payload 本次流程携带的事件或业务数据。
     * @param redacted 用于完成本次业务处理的 redacted 参数。
     * @param turnId 用于定位turn的标识。
     * @param stepId 用于定位step的标识。
     * @param providerRequestId 用于定位provider的标识。
     * @return 返回 AI Runtime 相关操作生成的结果数据。
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
