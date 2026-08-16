package com.shiyu.ai.usage.service;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.usage.port.BillingPriceProvider;
import com.shiyu.ai.usage.model.ModelPricing;
import com.shiyu.ai.usage.realtime.UsageRealtimePublisher;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.math.BigDecimal;

/**
 * 用量记录服务
 * <p>
 * 接收模型调用事件和 Embedding 事件，统一写入 usage_record 表。
 * 通用字段直接落列，类型专属字段以 JSON 存放于 ext_info。
 * </p>
 */
@Slf4j
public class UsageRecordService {

    private final UsageRecordRepository usageRecordRepository;
    private final Map<String, ModelPricing> pricingMap = new ConcurrentHashMap<>();
    private UsageRealtimePublisher realtimePublisher;
    private BillingPriceProvider billingPriceProvider;

    public UsageRecordService(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
        registerPricing(ModelPricing.defaultOpenAI());
    }

    public void setRealtimePublisher(UsageRealtimePublisher publisher) {
        this.realtimePublisher = publisher;
    }

    public void setBillingPriceProvider(BillingPriceProvider provider) {
        this.billingPriceProvider = provider;
    }

    public void registerPricing(ModelPricing pricing) {
        pricingMap.put(pricing.getPlatform() + ":" + pricing.getModel(), pricing);
    }

    /**
     * 记录 LLM 模型调用用量
     */
    public void recordUsage(String platform, String model,
                            int promptTokens, int completionTokens,
                            long latencyMs, Long userId, String sessionId) {
        recordUsage(platform, model, promptTokens, completionTokens, latencyMs, userId, sessionId, null);
    }

    public void recordUsage(String platform, String model,
                            int promptTokens, int completionTokens,
                            long latencyMs, Long userId, String sessionId, String generationRunId) {
        ModelPricing pricing = pricingMap.getOrDefault(platform + ":" + model, new ModelPricing(platform, model, 0.0, 0.0));
        BillingPriceProvider.PriceSnapshot snapshot = billingPriceProvider == null ? null : billingPriceProvider.price(platform, model);
        double cost = snapshot == null
                ? pricing.calculateCost(promptTokens, completionTokens)
                : snapshot.promptPerToken().multiply(BigDecimal.valueOf(promptTokens))
                    .add(snapshot.completionPerToken().multiply(BigDecimal.valueOf(completionTokens))).doubleValue();

        int totalTokens = promptTokens + completionTokens;
        Map<String, Object> usage = new java.util.HashMap<>(Map.of(
            "platform", platform,
            "model", model,
            "promptTokens", promptTokens,
            "completionTokens", completionTokens,
            "totalTokens", totalTokens,
            "cost", cost,
            "pricingVersion", snapshot == null ? "local-model-pricing" : snapshot.version()
        ));
        if (generationRunId != null && !generationRunId.isBlank()) usage.put("generationRunId", generationRunId);
        String extInfo = JSONUtils.toJsonString(usage);

        UsageRecordBO record = buildRecord("LLM", latencyMs, userId, sessionId, extInfo);

        try {
            usageRecordRepository.insert(record);
            log.debug("LLM 用量已保存: platform={}, model={}, tokens={}, cost={}, latency={}ms",
                    platform, model, totalTokens, cost, latencyMs);
        } catch (Exception e) {
            log.error("保存 LLM 用量失败: platform={}, model={}", platform, model, e);
        }

        publishUsageEvent(platform, model, promptTokens, completionTokens, latencyMs, cost);
    }

    /**
     * 记录 Embedding 用量
     */
    public void recordEmbedding(String model, int textLength,
                                int estimatedTokens, int vectorCount,
                                long latencyMs, Long userId, String sessionId) {
        String extInfo = JSONUtils.toJsonString(Map.of(
            "model", model,
            "textLength", textLength,
            "estimatedTokens", estimatedTokens,
            "vectorCount", vectorCount
        ));

        UsageRecordBO record = buildRecord("EMBEDDING", latencyMs, userId, sessionId, extInfo);

        try {
            usageRecordRepository.insert(record);
            log.debug("Embedding 用量已保存: model={}, tokens≈{}, vectors={}, latency={}ms",
                    model, estimatedTokens, vectorCount, latencyMs);
        } catch (Exception e) {
            log.error("保存 Embedding 用量失败: model={}", model, e);
        }

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publishEmbeddingUsage(model, textLength, estimatedTokens, vectorCount, latencyMs);
            } catch (Exception e) {
                log.warn("WebSocket 推送 Embedding 用量失败: {}", e.getMessage());
            }
        }
    }

    private UsageRecordBO buildRecord(String usageType, long latencyMs,
                                       Long userId, String sessionId, String extInfo) {
        UsageRecordBO record = new UsageRecordBO();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setUsageType(usageType);
        record.setLatencyMs(latencyMs);
        record.setUserId(userId);
        record.setSessionId(sessionId);
        record.setExtInfo(extInfo);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    private void publishUsageEvent(String platform, String model,
                                       int promptTokens, int completionTokens,
                                       long latencyMs, double cost) {
        if (realtimePublisher != null) {
            try {
                realtimePublisher.publishUsageRecord(platform, model,
                        promptTokens, completionTokens, latencyMs, cost);
            } catch (Exception e) {
                log.warn("WebSocket 推送用量失败: {}", e.getMessage());
            }
        }
    }

    public int getPricingCount() {
        return pricingMap.size();
    }
}
