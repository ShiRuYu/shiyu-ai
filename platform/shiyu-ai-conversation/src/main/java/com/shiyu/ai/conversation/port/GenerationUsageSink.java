package com.shiyu.ai.conversation.port;

import com.shiyu.ai.conversation.domain.GenerationRun;

/**
 * Outbound usage boundary. Conversation remains usable without the usage
 * module; a deployment may provide a sink to turn the durable run into a
 * billing/usage ledger record.
 */
public interface GenerationUsageSink {
    default void completed(GenerationRun run) { }
    default void failed(GenerationRun run) { }
}
