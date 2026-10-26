package com.shiyu.ai.conversation.implementation.domain.model;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;

/** A claimed, retryable side effect created with a terminal generation transition. */
public record GenerationPostProcessing(
        GenerationRun run,
        TenantId tenantId,
        long ownerUserId,
        QuotaAction quotaAction,
        UsageAction usageAction,
        boolean quotaDone,
        boolean usageDone,
        int attempts,
        String claimToken) {
    public enum QuotaAction {
        NONE,
        SETTLE,
        RELEASE
    }

    public enum UsageAction {
        NONE,
        COMPLETED,
        FAILED
    }

    public enum Step {
        QUOTA,
        USAGE
    }
}
