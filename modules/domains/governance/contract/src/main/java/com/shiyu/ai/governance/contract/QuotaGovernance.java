package com.shiyu.ai.governance.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/** Tenant quota and admission boundary owned by Governance. */
public interface QuotaGovernance {

    QuotaDecision reserve(ActorContext actor, QuotaRequest request);

    void settle(ActorContext actor, long reservationId, QuotaUsage usage);

    void release(ActorContext actor, long reservationId);
}
