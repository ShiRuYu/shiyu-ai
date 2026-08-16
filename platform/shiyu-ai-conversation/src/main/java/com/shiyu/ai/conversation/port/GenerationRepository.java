package com.shiyu.ai.conversation.port;

import com.shiyu.ai.conversation.domain.GenerationEvent;
import com.shiyu.ai.conversation.domain.GenerationRun;

import java.util.List;
import java.util.Optional;

public interface GenerationRepository {
    void insert(GenerationRun generation);
    Optional<GenerationRun> find(String id, long tenantId, long ownerUserId);
    boolean hasRunning(String conversationId, String inputMessageId);
    default boolean hasRunningConversation(String conversationId, long tenantId) { return false; }
    default List<GenerationRun> listConversation(String conversationId, long tenantId, int limit) { return List.of(); }
    int update(GenerationRun generation, long expectedVersion);
    void appendEvent(GenerationEvent event, long tenantId);
    List<GenerationEvent> listEvents(String generationId, int afterSequence, int limit);
    default int nextEventSequence(String generationId) { return 0; }
}
