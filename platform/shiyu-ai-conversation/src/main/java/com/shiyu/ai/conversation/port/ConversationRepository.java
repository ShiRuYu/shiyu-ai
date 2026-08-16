package com.shiyu.ai.conversation.port;

import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.ConversationMessage;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
    void insertConversation(Conversation conversation);
    Optional<Conversation> findConversation(String id, long tenantId, long ownerUserId);
    List<Conversation> listConversations(long tenantId, long ownerUserId, int limit, int offset);
    default List<Conversation> listBranches(String parentConversationId, long tenantId, long ownerUserId) { return List.of(); }
    int updateConversation(Conversation conversation, long expectedVersion);
    void insertMessage(ConversationMessage message);
    Optional<ConversationMessage> findMessage(String id, long tenantId, long ownerUserId);
    List<ConversationMessage> listMessages(String conversationId, long tenantId, long ownerUserId, int limit);
    default int deleteConversation(String id, long tenantId, long ownerUserId) { return 0; }
    /** Compensating delete used when generation finalization loses an optimistic-lock race. */
    default int deleteMessage(String id, long tenantId, long ownerUserId) { return 0; }
}
