package com.shiyu.ai.application.governance;

import com.shiyu.ai.model.event.ModelCallEvent;
import com.shiyu.ai.model.event.EmbeddingCallEvent;
import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 用量事件监听器
 * <p>
 * 监听 LLM 调用事件（{@link ModelCallEvent}）和 Embedding 调用事件（{@link EmbeddingCallEvent}），
 * 自动记录全平台用量。
 * </p>
 */
@Slf4j
@Component
public class UsageEventListener {

    private final UsageGovernance usageGovernance;

    public UsageEventListener(UsageGovernance usageGovernance) {
        this.usageGovernance = usageGovernance;
    }

    /**
     * 监听 LLM 模型调用事件
     */
    @EventListener
    @Async
    public void onModelCall(ModelCallEvent event) {
        // ConversationUsageSink records GenerationRun calls after the durable
        // terminal transition; do not create a second billable ledger row here.
        if (event.getGenerationRunId() != null && !event.getGenerationRunId().isBlank()) return;
        if (!attributable(event.getTenantId(), event.getUserId())) {
            log.warn("Ignoring unattributed model usage event: tenantId={}, userId={}, platform={}, model={}",
                    event.getTenantId(), event.getUserId(), event.getPlatform(), event.getModel());
            return;
        }
        ActorContext actor = actor(event.getTenantId(), event.getUserId());
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.MODEL_INVOCATION,
                event.getSourceId(),
                event.getPromptTokens(),
                event.getCompletionTokens(),
                BigDecimal.ZERO,
                event.getLatencyMs(),
                Map.of("usageType", "LLM", "platform", event.getPlatform(), "model", event.getModel()));
        usageGovernance.record(actor, envelope(actor, event.getCorrelationId(), measurement));
    }

    /**
     * 监听 Embedding 向量化调用事件
     */
    @EventListener
    @Async
    public void onEmbeddingCall(EmbeddingCallEvent event) {
        if (!attributable(event.getTenantId(), event.getUserId())) {
            log.warn("Ignoring unattributed embedding usage event: tenantId={}, userId={}, model={}",
                    event.getTenantId(), event.getUserId(), event.getModel());
            return;
        }
        ActorContext actor = actor(event.getTenantId(), event.getUserId());
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.KNOWLEDGE_INDEXING,
                event.getSourceId(),
                event.getEstimatedTokens(),
                0,
                BigDecimal.ZERO,
                event.getLatencyMs(),
                Map.of("usageType", "EMBEDDING", "model", event.getModel(),
                        "textLength", Integer.toString(event.getTextLength()),
                        "estimatedTokens", Integer.toString(event.getEstimatedTokens()),
                        "vectorCount", Integer.toString(event.getVectorCount())));
        usageGovernance.record(actor, envelope(actor, event.getCorrelationId(), measurement));
    }

    private static boolean attributable(TenantId tenantId, UserId userId) {
        return tenantId != null && userId != null;
    }

    private static ActorContext actor(TenantId tenantId, UserId userId) {
        return new ActorContext(tenantId, userId, false);
    }

    private static DomainEventEnvelope<UsageMeasurement> envelope(ActorContext actor,
                                                                  CorrelationId correlationId,
                                                                  UsageMeasurement measurement) {
        CorrelationId effectiveCorrelationId = correlationId == null ? CorrelationId.random() : correlationId;
        return new DomainEventEnvelope<>(actor.tenantId(), actor.userId(), effectiveCorrelationId, Instant.now(), measurement);
    }
}
