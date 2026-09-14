package com.shiyu.ai.model.implementation.domain.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.UUID;

/**
 * 模型调用事件
 *
 * <p>每次 LLM 对话调用完成后发布，携带 Token 用量、平台、模型等信息。 由应用装配层转发给 Governance 领域并记录用量。
 */
public class ModelCallEvent {

    /**
     * platform 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String platform;
    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String model;
    /**
     * promptTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int promptTokens;
    /**
     * completionTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int completionTokens;
    /**
     * latencyMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long latencyMs;
    /**
     * generationRunId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String generationRunId;

    /** 来源对象标识。 */
    private final String sourceId;

    /**
     * 租户标识，表示当前对象中的对应属性。
     */
    private final TenantId tenantId;
    /**
     * 用户标识，表示当前对象中的对应属性。
     */
    private final UserId userId;
    /**
     * correlationId 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CorrelationId correlationId;

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform, String model, int promptTokens, int completionTokens, long latencyMs) {
        this(platform, model, promptTokens, completionTokens, latencyMs, null);
    }

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param generationRunId 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            String generationRunId) {
        this(platform, model, promptTokens, completionTokens, latencyMs, generationRunId, null);
    }

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param generationRunId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            String generationRunId,
            TenantId tenantId) {
        this(
                platform,
                model,
                promptTokens,
                completionTokens,
                latencyMs,
                generationRunId,
                tenantId,
                null);
    }

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param generationRunId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            String generationRunId,
            TenantId tenantId,
            UserId userId) {
        this(
                platform,
                model,
                promptTokens,
                completionTokens,
                latencyMs,
                generationRunId,
                UUID.randomUUID().toString(),
                tenantId,
                userId);
    }

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param generationRunId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            String generationRunId,
            String sourceId,
            TenantId tenantId,
            UserId userId) {
        this(
                platform,
                model,
                promptTokens,
                completionTokens,
                latencyMs,
                generationRunId,
                sourceId,
                tenantId,
                userId,
                CorrelationId.random());
    }

    /**
     * {@code ModelCallEvent} 创建并初始化当前类型实例。
     *
     * @param platform 参数值，用于执行当前操作。
     * @param model 参数值，用于执行当前操作。
     * @param promptTokens 参数值，用于执行当前操作。
     * @param completionTokens 参数值，用于执行当前操作。
     * @param latencyMs 参数值，用于执行当前操作。
     * @param generationRunId 参数值，用于执行当前操作。
     * @param sourceId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param userId 参数值，用于执行当前操作。
     * @param correlationId 参数值，用于执行当前操作。
     */
    public ModelCallEvent(
            String platform,
            String model,
            int promptTokens,
            int completionTokens,
            long latencyMs,
            String generationRunId,
            String sourceId,
            TenantId tenantId,
            UserId userId,
            CorrelationId correlationId) {
        this.platform = platform;
        this.model = model;
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.latencyMs = latencyMs;
        this.generationRunId = generationRunId;
        this.sourceId =
                sourceId == null || sourceId.isBlank() ? UUID.randomUUID().toString() : sourceId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.correlationId = correlationId == null ? CorrelationId.random() : correlationId;
    }

    /**
     * {@code getPlatform} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * {@code getModel} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getModel() {
        return model;
    }

    /**
     * {@code getPromptTokens} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getPromptTokens() {
        return promptTokens;
    }

    /**
     * {@code getCompletionTokens} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getCompletionTokens() {
        return completionTokens;
    }

    /**
     * {@code getTotalTokens} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public int getTotalTokens() {
        return promptTokens + completionTokens;
    }

    /**
     * {@code getLatencyMs} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * {@code getGenerationRunId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getGenerationRunId() {
        return generationRunId;
    }

    /**
     * {@code getSourceId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * {@code getTenantId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * {@code getUserId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * {@code getCorrelationId} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
