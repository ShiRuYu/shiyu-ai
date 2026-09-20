package com.shiyu.ai.governance.contract;

/**
 * 封装 Quota 相关的不可变数据及其字段约束。
 */
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
