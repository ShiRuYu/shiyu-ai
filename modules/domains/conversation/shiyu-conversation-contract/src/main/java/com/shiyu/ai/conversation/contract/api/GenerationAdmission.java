package com.shiyu.ai.conversation.contract.api;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.ActorContext;

/** Admission/settlement boundary for tenant generation budgets. */
public interface GenerationAdmission {
    default void reserve(ActorContext actor, GenerationRun run, int estimatedPromptTokens) { }
    default void settle(ActorContext actor, GenerationRun run) { }
    default void release(ActorContext actor, GenerationRun run) { }
}
