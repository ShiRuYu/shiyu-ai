package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.event.DomainEventEnvelope;

public interface UsageGovernance {

    UsageRecordResult record(ActorContext actor, DomainEventEnvelope<UsageMeasurement> usage);
}
