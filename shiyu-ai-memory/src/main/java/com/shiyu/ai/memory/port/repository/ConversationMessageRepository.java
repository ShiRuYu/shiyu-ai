package com.shiyu.ai.memory.port.repository;

import com.shiyu.ai.memory.domain.model.ConversationMessageBO;
import java.util.List;

public interface ConversationMessageRepository {
    void insert(ConversationMessageBO bo);
    int deleteBySessionBefore(java.time.LocalDate deadline);
    List<ConversationMessageBO> selectRecentBySession(String sessionId, int limit);
    long countBySession(String sessionId);
    void deleteBySession(String sessionId);
}
