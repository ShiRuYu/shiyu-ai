package com.shiyu.ai.composition.integration.governance;

import com.shiyu.ai.conversation.contract.api.GenerationUsageSink;
import com.shiyu.ai.conversation.contract.model.GenerationRun;
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

/**
 * 将会话生成用量转换为治理模块可结算的用量事件。
 */
@Component
public class ConversationUsageSink implements GenerationUsageSink {
    /**
     * 用量，表示当前对象中的对应属性。
     */
    private final UsageGovernance usage;

    /**
     * {@code ConversationUsageSink} 创建并初始化当前类型实例。
     *
     * @param usage 参数值，用于执行当前操作。
     */
    public ConversationUsageSink(UsageGovernance usage) {
        this.usage = usage;
    }

    /**
     * {@code completed} 执行当前类型定义的业务操作。
     *
     * @param run 参数值，用于执行当前操作。
     */
    @Override
    public void completed(GenerationRun run) {
        throw new IllegalArgumentException(
                "Generation usage requires explicit tenant and owner user");
    }

    /**
     * {@code completed} 执行当前类型定义的业务操作。
     *
     * @param run 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     */
    @Override
    public void completed(GenerationRun run, TenantId tenantId, UserId ownerUserId) {
        ActorContext actor = new ActorContext(tenantId, ownerUserId, false);
        UsageMeasurement measurement =
                new UsageMeasurement(
                        UsageSourceType.CONVERSATION_GENERATION,
                        run.id(),
                        safe(run.promptTokens()),
                        safe(run.completionTokens()),
                        BigDecimal.ZERO,
                        Math.max(0L, run.latencyMs()),
                        Map.of(
                                "usageType",
                                "LLM",
                                "platform",
                                run.platform(),
                                "model",
                                run.model(),
                                "sessionId",
                                run.conversationId(),
                                "generationRunId",
                                run.id()));
        usage.record(
                actor,
                new DomainEventEnvelope<>(
                        actor.tenantId(),
                        actor.userId(),
                        CorrelationId.random(),
                        Instant.now(),
                        measurement));
    }

    private static int safe(long value) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, value));
    }
}
