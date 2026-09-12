package com.shiyu.ai.governance.implementation.application;

import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageRecordResult;
import com.shiyu.ai.governance.implementation.persistence.UsageLedger;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import java.util.Objects;

/**
 * {@code UsageRecorder} 承载治理模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
public final class UsageRecorder implements UsageGovernance {

    /**
     * ledger 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final UsageLedger ledger;

    /**
     * {@code UsageRecorder} 创建并初始化当前类型实例。
     *
     * @param ledger 参数值，用于执行当前操作。
     */
    public UsageRecorder(UsageLedger ledger) {
        this.ledger = Objects.requireNonNull(ledger, "ledger must not be null");
    }

    /**
     * {@code record} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param usage 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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

        return ledger.insertIfAbsent(entry)
                ? UsageRecordResult.RECORDED
                : UsageRecordResult.DUPLICATE;
    }
}
