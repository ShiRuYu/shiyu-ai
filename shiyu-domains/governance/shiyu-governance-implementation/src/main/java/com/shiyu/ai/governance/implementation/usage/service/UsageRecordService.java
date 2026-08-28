package com.shiyu.ai.governance.implementation.usage.service;

import com.shiyu.ai.common.core.utils.JSONUtils;
import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageRecordResult;
import com.shiyu.ai.governance.implementation.usage.domain.model.UsageRecordBO;
import com.shiyu.ai.governance.implementation.usage.port.repository.UsageRecordRepository;
import com.shiyu.ai.governance.implementation.usage.port.BillingPriceProvider;
import com.shiyu.ai.governance.implementation.usage.model.ModelPricing;
import com.shiyu.ai.governance.implementation.usage.realtime.UsageRealtimePublisher;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
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
public class UsageRecordService implements UsageGovernance {

    private final UsageRecordRepository usageRecordRepository;
    private final Map<String, ModelPricing> pricingMap = new ConcurrentHashMap<>();
    private UsageRealtimePublisher realtimePublisher;
    private BillingPriceProvider billingPriceProvider;

    public UsageRecordService(UsageRecordRepository usageRecordRepository) {
        this.usageRecordRepository = usageRecordRepository;
        ModelPricing defaultPricing = ModelPricing.defaultOpenAI();
        pricingMap.put(defaultPricing.getPlatform() + ":" + defaultPricing.getModel(), defaultPricing);
    }

    public void setRealtimePublisher(UsageRealtimePublisher publisher) {
        this.realtimePublisher = publisher;
    }

    /**
     * Contract entry point used by cross-domain application adapters. The
     * repository owns the unique (tenant, source type, source id) constraint,
     * so retries are reported as duplicates instead of charging twice.
     */
    @Override
    public UsageRecordResult record(ActorContext actor, DomainEventEnvelope<UsageMeasurement> envelope) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(envelope, "envelope must not be null");
        actor.requireTenant(envelope.tenantId());
        if (!actor.userId().equals(envelope.userId())) {
            throw new DomainAccessDeniedException("ACTOR_MISMATCH", "The usage event user does not match the actor");
        }

        UsageMeasurement measurement = envelope.event();
        Map<String, Object> payload = new java.util.HashMap<>();
        measurement.attributes().forEach(payload::put);
        payload.put("sourceType", measurement.sourceType().name());
        payload.put("sourceId", measurement.sourceId());
        payload.put("inputTokens", measurement.inputTokens());
        payload.put("outputTokens", measurement.outputTokens());
        payload.put("cost", measurement.cost());
        payload.put("latencyMs", measurement.latencyMs());

        String usageType = measurement.attributes().getOrDefault("usageType", "METERED");
        String sessionId = measurement.attributes().get("sessionId");
        UsageRecordBO record = buildRecord(
                usageType,
                measurement.latencyMs(),
                envelope.tenantId(),
                envelope.userId().value(),
                sessionId,
                JSONUtils.toJsonString(payload));
        record.setSourceType(measurement.sourceType().name());
        record.setSourceId(measurement.sourceId());
        record.setCorrelationId(envelope.correlationId().value());
        record.setInputTokens(measurement.inputTokens());
        record.setOutputTokens(measurement.outputTokens());
        record.setCost(measurement.cost());
        record.setOccurredAt(LocalDateTime.ofInstant(envelope.occurredAt(), java.time.ZoneOffset.UTC));

        boolean inserted = usageRecordRepository.insertIfAbsent(record);
        if (inserted) {
            if ("LLM".equalsIgnoreCase(usageType)) {
                publishUsageEvent(
                        measurement.attributes().getOrDefault("platform", "UNKNOWN"),
                        measurement.attributes().getOrDefault("model", "UNKNOWN"),
                        safeInt(measurement.inputTokens()),
                        safeInt(measurement.outputTokens()),
                        measurement.latencyMs(),
                        measurement.cost().doubleValue());
            } else if ("EMBEDDING".equalsIgnoreCase(usageType)) {
                publishEmbeddingEvent(
                        measurement.attributes().getOrDefault("model", "UNKNOWN"),
                        safeInt(measurement.attributes().get("textLength")),
                        safeInt(measurement.attributes().getOrDefault("estimatedTokens", Long.toString(measurement.inputTokens()))),
                        safeInt(measurement.attributes().get("vectorCount")),
                        measurement.latencyMs());
            }
        }
        return inserted ? UsageRecordResult.RECORDED : UsageRecordResult.DUPLICATE;
    }

    public void setBillingPriceProvider(BillingPriceProvider provider) {
        this.billingPriceProvider = provider;
    }

    public void registerPricing(ModelPricing pricing) {
        pricingMap.put(pricing.getPlatform() + ":" + pricing.getModel(), pricing);
    }

    /** Records an LLM usage row with an explicit tenant boundary. */
    public void recordUsage(String platform, String model,
                            int promptTokens, int completionTokens,
                            long latencyMs, TenantId tenantId, Long userId,
                            String sessionId, String generationRunId) {
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

        UsageRecordBO record = buildRecord("LLM", latencyMs, tenantId, userId, sessionId, extInfo);
        record.setSourceType(generationRunId == null || generationRunId.isBlank()
                ? "MODEL_CALL" : "GENERATION_RUN");
        record.setSourceId(firstNonBlank(generationRunId, sessionId, record.getId()));
        record.setCorrelationId(UUID.randomUUID().toString());
        record.setInputTokens((long) promptTokens);
        record.setOutputTokens((long) completionTokens);
        record.setCost(BigDecimal.valueOf(cost));
        record.setOccurredAt(record.getCreateTime());

        usageRecordRepository.insert(record);
        log.debug("LLM 用量已保存: platform={}, model={}, tokens={}, cost={}, latency={}ms",
                platform, model, totalTokens, cost, latencyMs);

        publishUsageEvent(platform, model, promptTokens, completionTokens, latencyMs, cost);
    }

    /**
     * 记录 Embedding 用量
     */
    public void recordEmbedding(String model, int textLength,
                                int estimatedTokens, int vectorCount,
                                long latencyMs, TenantId tenantId, Long userId, String sessionId) {
        String extInfo = JSONUtils.toJsonString(Map.of(
            "model", model,
            "textLength", textLength,
            "estimatedTokens", estimatedTokens,
            "vectorCount", vectorCount
        ));

        UsageRecordBO record = buildRecord("EMBEDDING", latencyMs, tenantId, userId, sessionId, extInfo);
        record.setSourceType("EMBEDDING_CALL");
        record.setSourceId(firstNonBlank(sessionId, record.getId()));
        record.setCorrelationId(UUID.randomUUID().toString());
        record.setInputTokens((long) estimatedTokens);
        record.setOutputTokens(0L);
        record.setCost(BigDecimal.ZERO);
        record.setOccurredAt(record.getCreateTime());

        usageRecordRepository.insert(record);
        log.debug("Embedding 用量已保存: model={}, tokens≈{}, vectors={}, latency={}ms",
                model, estimatedTokens, vectorCount, latencyMs);

        if (realtimePublisher != null) {
            try {
                realtimePublisher.publishEmbeddingUsage(model, textLength, estimatedTokens, vectorCount, latencyMs);
            } catch (Exception e) {
                log.warn("WebSocket 推送 Embedding 用量失败: {}", e.getMessage());
            }
        }
    }

    private UsageRecordBO buildRecord(String usageType, long latencyMs,
                                       TenantId tenantId, Long userId, String sessionId, String extInfo) {
        UsageRecordBO record = new UsageRecordBO();
        record.setId(UUID.randomUUID().toString().replace("-", ""));
        record.setUsageType(usageType);
        record.setLatencyMs(latencyMs);
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        record.setTenantId(tenantId.value());
        record.setUserId(requirePositiveId(userId, "userId"));
        record.setSessionId(sessionId);
        record.setExtInfo(extInfo);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    private static Long requirePositiveId(Long value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value <= 0) {
            throw new IllegalArgumentException(field + " must be positive");
        }
        return value;
    }

    private static String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) return candidate;
        }
        throw new IllegalArgumentException("at least one source identifier is required");
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

    private void publishEmbeddingEvent(String model, int textLength, int estimatedTokens,
                                       int vectorCount, long latencyMs) {
        if (realtimePublisher != null) {
            try {
                realtimePublisher.publishEmbeddingUsage(model, textLength, estimatedTokens, vectorCount, latencyMs);
            } catch (Exception e) {
                log.warn("WebSocket 推送 Embedding 用量失败: {}", e.getMessage());
            }
        }
    }

    private static int safeInt(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }

    private static int safeInt(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return safeInt(Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public int getPricingCount() {
        return pricingMap.size();
    }
}
