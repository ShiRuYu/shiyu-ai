package com.shiyu.ai.governance.contract;

/**
 * 表示一次调用消耗的输入和输出 token 数量。
 * @param inputTokens inputTokens 属性，表示该记录组件承载的数据。
 * @param outputTokens outputTokens 属性，表示该记录组件承载的数据。
 */
public record QuotaUsage(int inputTokens, int outputTokens) {
    public QuotaUsage {
        if (inputTokens < 0 || outputTokens < 0) {
            throw new IllegalArgumentException("settled token counts must not be negative");
        }
    }
}
