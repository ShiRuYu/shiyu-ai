package com.shiyu.ai.conversation.contract.api;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

/**
 * Outbound usage boundary. Conversation remains usable without the usage
 * module; a deployment may provide a sink to turn the durable run into a
 * billing/usage ledger record.
 */
public interface GenerationUsageSink {
    default void completed(GenerationRun run) { }
    default void completed(GenerationRun run, TenantId tenantId, UserId ownerUserId) { completed(run); }
    default void failed(GenerationRun run) { }
}
