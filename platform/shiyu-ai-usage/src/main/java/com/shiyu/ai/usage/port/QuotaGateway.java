package com.shiyu.ai.usage.port;

/** Usage/quota boundary. Estimated tokens are suitable for admission only, never for billing. */
public interface QuotaGateway {
    Decision reserve(long tenantId, int promptTokens, int maxConcurrent);
    void settle(long tenantId, long runId, int providerPromptTokens, int providerCompletionTokens);
    void release(long tenantId, long runId);
    record Decision(boolean allowed, String errorCode, long reservationId) { }
}
