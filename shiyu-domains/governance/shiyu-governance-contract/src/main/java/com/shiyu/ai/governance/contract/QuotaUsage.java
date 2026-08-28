package com.shiyu.ai.governance.contract;

/** Provider-reported usage used to settle an admission reservation. */
public record QuotaUsage(int inputTokens, int outputTokens) {
    public QuotaUsage {
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("settled token counts must not be negative");
        }
    }
}
