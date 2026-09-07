package com.shiyu.ai.conversation.port;

import com.shiyu.ai.conversation.domain.GenerationEvent;
import com.shiyu.ai.conversation.domain.GenerationRun;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

public interface GenerationRepository {
    void insert(GenerationRun generation);
    Optional<GenerationRun> find(String id, TenantId tenantId, long ownerUserId);
    boolean hasRunning(String conversationId, String inputMessageId, TenantId tenantId);
    boolean hasRunningConversation(String conversationId, TenantId tenantId);
    List<GenerationRun> listConversation(String conversationId, TenantId tenantId, int limit);
    int update(GenerationRun generation, long expectedVersion);
    void appendEvent(GenerationEvent event, TenantId tenantId);
    List<GenerationEvent> listEvents(String generationId, TenantId tenantId, int afterSequence, int limit);
    int nextEventSequence(String generationId, TenantId tenantId);
}
