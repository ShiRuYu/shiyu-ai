package com.shiyu.ai.conversation.contract.api;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

/**
 * Outbound usage boundary. Conversation remains usable without the usage module; a deployment may
 * provide a sink to turn the durable run into a billing/usage ledger record.
 */
public interface GenerationUsageSink {
    /** Records a completed generation for usage or billing projections. */
    default void completed(GenerationRun run) {}

    /** Records completion with explicit tenant and owner attribution. */
    default void completed(GenerationRun run, TenantId tenantId, UserId ownerUserId) {
        completed(run);
    }

    /** Records a failed generation without making the conversation module depend on governance. */
    default void failed(GenerationRun run) {}
}
