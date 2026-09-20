package com.shiyu.ai.governance.implementation.application;

import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageRecordResult;
import com.shiyu.ai.governance.implementation.persistence.UsageLedger;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;
import com.shiyu.ai.kernel.context.TenantScope;

import java.util.Objects;

/**
 * 编排 用量 Recorder 所属应用流程的输入、协作和业务结果。
 */
public final class UsageRecorder implements UsageGovernance {

    /**
     * ledger 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final UsageLedger ledger;

    /**
     * 执行 用量 Recorder 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param ledger 用于完成本次业务处理的 ledger 参数。
     */
    public UsageRecorder(UsageLedger ledger) {
        this.ledger = Objects.requireNonNull(ledger, "ledger must not be null");
    }

    /**
     * 执行 用量 Recorder 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param usage 用于完成本次业务处理的 usage 参数。
     * @return 返回 用量 Recorder 相关操作生成的结果数据。
     */
    @Override
    public UsageRecordResult record(
            ActorContext actor, DomainEventEnvelope<UsageMeasurement> usage) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(usage, "usage must not be null");
        actor.requireTenant(usage.tenantId());
        if (!actor.userId().equals(usage.userId())) {
            throw new DomainAccessDeniedException(
                    "ACTOR_MISMATCH", "The usage event user does not match the actor");
        }

        UsageMeasurement measurement = usage.event();
        UsageLedger.Entry entry =
                new UsageLedger.Entry(
                        usage.tenantId(),
                        usage.userId(),
                        usage.correlationId(),
                        measurement.sourceType(),
                        measurement.sourceId(),
                        measurement.inputTokens(),
                        measurement.outputTokens(),
                        measurement.cost(),
                        usage.occurredAt());

        return TenantScope.withTenant(
                                actor.tenantId(),
                                () -> ledger.insertIfAbsent(entry))
                        ? UsageRecordResult.RECORDED
                        : UsageRecordResult.DUPLICATE;
    }
}
