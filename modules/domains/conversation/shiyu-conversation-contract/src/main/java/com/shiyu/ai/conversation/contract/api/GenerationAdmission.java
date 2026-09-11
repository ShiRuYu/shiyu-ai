package com.shiyu.ai.conversation.contract.api;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.ActorContext;

/** Admission/settlement boundary for tenant generation budgets. */
public interface GenerationAdmission {
    /** Reserves the estimated prompt budget before a generation starts. */
    default void reserve(ActorContext actor, GenerationRun run, int estimatedPromptTokens) {}

    /** Settles reserved budget after a generation completes successfully. */
    default void settle(ActorContext actor, GenerationRun run) {}

    /** Releases the reservation when a generation is cancelled or fails. */
    default void release(ActorContext actor, GenerationRun run) {}
}
