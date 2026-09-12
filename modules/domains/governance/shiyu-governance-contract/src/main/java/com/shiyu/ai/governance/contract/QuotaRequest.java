package com.shiyu.ai.governance.contract;

/**
 * 处理配额请求。
 * @param estimatedPromptTokens estimatedPromptTokens 属性，表示该记录组件承载的数据。
 * @param maxConcurrent maxConcurrent 属性，表示该记录组件承载的数据。
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
