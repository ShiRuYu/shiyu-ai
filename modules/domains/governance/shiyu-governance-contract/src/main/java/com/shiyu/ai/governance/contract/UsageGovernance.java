package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

/** Cross-domain usage accounting boundary for model and generation events. */
public interface UsageGovernance {

    /** Records one tenant-attributed usage measurement and returns its ledger result. */
    UsageRecordResult record(ActorContext actor, DomainEventEnvelope<UsageMeasurement> usage);
}
