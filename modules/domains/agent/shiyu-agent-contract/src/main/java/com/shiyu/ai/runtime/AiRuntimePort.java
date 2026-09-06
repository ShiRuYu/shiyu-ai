package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

/**
 * Cross-domain runtime boundary. Agent owns the implementation and storage;
 * consumers only depend on this narrow, framework-free contract.
 */
public interface AiRuntimePort {
    AiRun startRun(AiRunContext context, AiRunSource source, String sourceId, String model, String prompt);

    AiRun finish(String id, TenantId tenantId, long ownerUserId, AiRunStatus terminal, String errorCode);

    List<AiRunEvent> events(String id, TenantId tenantId, long ownerUserId, long afterSeq, int limit);

    AiRun requireRun(String id, TenantId tenantId, long ownerUserId);

    AiRun requireGenerationRun(String generationId, TenantId tenantId, long ownerUserId);

    AiRun linkGeneration(AiRun run, String generationId);

    AiRun requireExecutionRun(String executionId, TenantId tenantId, long ownerUserId);

    AiRun recordUsage(String id, TenantId tenantId, long ownerUserId, long promptTokens,
                      long completionTokens, boolean estimated, String costSnapshot);

    long append(AiRun run, AiRunEventType type, String payload, boolean redacted);

    long append(AiRun run, AiRunEventType type, String payload, boolean redacted,
                String turnId, String stepId, String providerRequestId);
}
