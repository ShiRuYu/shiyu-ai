package com.shiyu.ai.conversation.port;

import com.shiyu.ai.conversation.domain.GenerationRun;

/** Admission/settlement boundary for tenant generation budgets. */
public interface GenerationAdmission {
    default void reserve(long tenantId, GenerationRun run, int estimatedPromptTokens) { }
    default void settle(long tenantId, GenerationRun run) { }
    default void release(long tenantId, GenerationRun run) { }
}
