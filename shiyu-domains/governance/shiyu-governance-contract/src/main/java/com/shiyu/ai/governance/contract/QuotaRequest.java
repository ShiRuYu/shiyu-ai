package com.shiyu.ai.governance.contract;

/** Admission inputs. Estimated tokens are used only for reservation. */
public record QuotaRequest(int estimatedPromptTokens, int maxConcurrent) {
    public QuotaRequest {
        if (estimatedPromptTokens < 0) {
            throw new IllegalArgumentException("estimatedPromptTokens must not be negative");
        }
        if (maxConcurrent < 0) {
            throw new IllegalArgumentException("maxConcurrent must not be negative");
        }
    }
}
