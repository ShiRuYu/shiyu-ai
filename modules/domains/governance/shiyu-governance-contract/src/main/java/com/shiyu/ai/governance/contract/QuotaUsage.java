package com.shiyu.ai.governance.contract;

/**
 * 封装 Quota 用量 相关的不可变数据及其字段约束。
 */
public record QuotaUsage(int inputTokens, int outputTokens) {
    public QuotaUsage {
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("settled token counts must not be negative");
        }
    }
}
