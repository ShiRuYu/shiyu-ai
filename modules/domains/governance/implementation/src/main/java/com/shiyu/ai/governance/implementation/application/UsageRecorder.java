package com.shiyu.ai.governance.implementation.application;

import com.shiyu.ai.governance.contract.UsageGovernance;
import com.shiyu.ai.governance.contract.UsageMeasurement;
import com.shiyu.ai.governance.contract.UsageRecordResult;
import com.shiyu.ai.governance.implementation.persistence.UsageLedger;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.error.DomainAccessDeniedException;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

import java.util.Objects;

public final class UsageRecorder implements UsageGovernance {

    private final UsageLedger ledger;

    public UsageRecorder(UsageLedger ledger) {
        this.ledger = Objects.requireNonNull(ledger, "ledger must not be null");
    }

    @Override
    public UsageRecordResult record(ActorContext actor, DomainEventEnvelope<UsageMeasurement> usage) {
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(usage, "usage must not be null");
        actor.requireTenant(usage.tenantId());
        if (!actor.userId().equals(usage.userId())) {
            throw new DomainAccessDeniedException(
                    "ACTOR_MISMATCH",
                    "The usage event user does not match the actor"
            );
        }

        UsageMeasurement measurement = usage.event();
        UsageLedger.Entry entry = new UsageLedger.Entry(
                usage.tenantId(),
                usage.userId(),
                usage.correlationId(),
                measurement.sourceType(),
                measurement.sourceId(),
                measurement.inputTokens(),
                measurement.outputTokens(),
                measurement.cost(),
                usage.occurredAt()
        );

        return ledger.insertIfAbsent(entry)
                ? UsageRecordResult.RECORDED
                : UsageRecordResult.DUPLICATE;
    }
}
