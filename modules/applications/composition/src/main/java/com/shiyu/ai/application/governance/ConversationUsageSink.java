package com.shiyu.ai.application.governance;

import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.conversation.port.GenerationUsageSink;
import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageSourceType;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.CorrelationId;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/** Converts the durable GenerationRun terminal state into the usage ledger. */
@Component
public class ConversationUsageSink implements GenerationUsageSink {
    private final UsageGovernance usage;

    public ConversationUsageSink(UsageGovernance usage) { this.usage = usage; }

    @Override
    public void completed(GenerationRun run) {
        throw new IllegalArgumentException("Generation usage requires explicit tenant and owner user");
    }

    @Override
    public void completed(GenerationRun run, TenantId tenantId, UserId ownerUserId) {
        ActorContext actor = new ActorContext(tenantId, ownerUserId, false);
        UsageMeasurement measurement = new UsageMeasurement(
                UsageSourceType.CONVERSATION_GENERATION,
                run.id(),
                safe(run.promptTokens()),
                safe(run.completionTokens()),
                BigDecimal.ZERO,
                Math.max(0L, run.latencyMs()),
                Map.of("usageType", "LLM", "platform", run.platform(), "model", run.model(),
                        "sessionId", run.conversationId(), "generationRunId", run.id()));
        usage.record(actor, new DomainEventEnvelope<>(actor.tenantId(), actor.userId(),
                CorrelationId.random(), Instant.now(), measurement));
    }

    private static int safe(long value) { return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value)); }
}
