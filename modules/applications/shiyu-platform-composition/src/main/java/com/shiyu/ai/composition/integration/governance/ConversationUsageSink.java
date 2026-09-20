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
 * 接收并处理 会话 用量 相关的业务事件或统计数据。
 */
@Component
public class ConversationUsageSink implements GenerationUsageSink {
    /**
     * 用量，表示当前对象中的对应属性。
     */
    private final UsageGovernance usage;

    /**
     * 执行 会话 用量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param usage 用于完成本次业务处理的 usage 参数。
     */
    public ConversationUsageSink(UsageGovernance usage) {
        this.usage = usage;
    }

    /**
     * 执行 会话 用量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     */
    @Override
    public void completed(GenerationRun run) {
        throw new IllegalArgumentException(
                "Generation usage requires explicit tenant and owner user");
    }

    /**
     * 执行 会话 用量 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param run 用于完成本次业务处理的 run 参数。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
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
