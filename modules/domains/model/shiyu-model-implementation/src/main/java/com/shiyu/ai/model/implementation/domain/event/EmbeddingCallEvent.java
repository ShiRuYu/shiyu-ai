package com.shiyu.ai.model.implementation.domain.event;

import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import java.util.UUID;

/**
 * 表示 嵌入 Call 相关的领域事件或异常信息。
 */
public class EmbeddingCallEvent {

    /**
     * model 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final String model;
    /**
     * textLength 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int textLength;
    /**
     * estimatedTokens 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final int estimatedTokens;
    /**
     * 向量数量，表示当前对象中的对应属性。
     */
    private final int vectorCount;
    /**
     * latencyMs 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final long latencyMs;

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
     * 执行 嵌入 Call 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param model 用于完成本次业务处理的 model 参数。
     * @param textLength 用于完成本次业务处理的 textLength 参数。
     * @param estimatedTokens 用于完成本次业务处理的 estimatedTokens 参数。
     * @param vectorCount 用于完成本次业务处理的 vectorCount 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     */
    public EmbeddingCallEvent(
            String model, int textLength, int estimatedTokens, int vectorCount, long latencyMs) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs, null);
    }

    /**
     * 执行 嵌入 Call 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param model 用于完成本次业务处理的 model 参数。
     * @param textLength 用于完成本次业务处理的 textLength 参数。
     * @param estimatedTokens 用于完成本次业务处理的 estimatedTokens 参数。
     * @param vectorCount 用于完成本次业务处理的 vectorCount 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     * @param tenantId 当前操作涉及的租户标识。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            TenantId tenantId) {
        this(model, textLength, estimatedTokens, vectorCount, latencyMs, tenantId, null);
    }

    /**
     * 执行 嵌入 Call 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param model 用于完成本次业务处理的 model 参数。
     * @param textLength 用于完成本次业务处理的 textLength 参数。
     * @param estimatedTokens 用于完成本次业务处理的 estimatedTokens 参数。
     * @param vectorCount 用于完成本次业务处理的 vectorCount 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            TenantId tenantId,
            UserId userId) {
        this(
                model,
                textLength,
                estimatedTokens,
                vectorCount,
                latencyMs,
                UUID.randomUUID().toString(),
                tenantId,
                userId);
    }

    /**
     * 执行 嵌入 Call 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param model 用于完成本次业务处理的 model 参数。
     * @param textLength 用于完成本次业务处理的 textLength 参数。
     * @param estimatedTokens 用于完成本次业务处理的 estimatedTokens 参数。
     * @param vectorCount 用于完成本次业务处理的 vectorCount 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     * @param sourceId 用于定位source的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            String sourceId,
            TenantId tenantId,
            UserId userId) {
        this(
                model,
                textLength,
                estimatedTokens,
                vectorCount,
                latencyMs,
                sourceId,
                tenantId,
                userId,
                CorrelationId.random());
    }

    /**
     * 执行 嵌入 Call 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param model 用于完成本次业务处理的 model 参数。
     * @param textLength 用于完成本次业务处理的 textLength 参数。
     * @param estimatedTokens 用于完成本次业务处理的 estimatedTokens 参数。
     * @param vectorCount 用于完成本次业务处理的 vectorCount 参数。
     * @param latencyMs 用于完成本次业务处理的 latencyMs 参数。
     * @param sourceId 用于定位source的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param userId 当前操作涉及的用户标识。
     * @param correlationId 用于定位correlation的标识。
     */
    public EmbeddingCallEvent(
            String model,
            int textLength,
            int estimatedTokens,
            int vectorCount,
            long latencyMs,
            String sourceId,
            TenantId tenantId,
            UserId userId,
            CorrelationId correlationId) {
        this.model = model;
        this.textLength = textLength;
        this.estimatedTokens = estimatedTokens;
        this.vectorCount = vectorCount;
        this.latencyMs = latencyMs;
        this.sourceId =
                sourceId == null || sourceId.isBlank() ? UUID.randomUUID().toString() : sourceId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.correlationId = correlationId == null ? CorrelationId.random() : correlationId;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public String getModel() {
        return model;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public int getTextLength() {
        return textLength;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public int getEstimatedTokens() {
        return estimatedTokens;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public int getVectorCount() {
        return vectorCount;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public long getLatencyMs() {
        return latencyMs;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public TenantId getTenantId() {
        return tenantId;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * 查询 嵌入 Call 相关业务数据，并返回处理结果。
     *
     * @return 返回 嵌入 Call 相关操作生成的结果数据。
     */
    public CorrelationId getCorrelationId() {
        return correlationId;
    }
}
