package com.shiyu.ai.conversation.port;

import com.shiyu.ai.conversation.domain.Conversation;
import com.shiyu.ai.conversation.domain.ConversationMessage;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository {
    void insertConversation(Conversation conversation);
    Optional<Conversation> findConversation(String id, TenantId tenantId, long ownerUserId);
    List<Conversation> listConversations(TenantId tenantId, long ownerUserId, int limit, int offset);
    List<Conversation> listBranches(String parentConversationId, TenantId tenantId, long ownerUserId);
    int updateConversation(Conversation conversation, long expectedVersion);
    void insertMessage(ConversationMessage message);
    Optional<ConversationMessage> findMessage(String id, TenantId tenantId, long ownerUserId);
    List<ConversationMessage> listMessages(String conversationId, TenantId tenantId, long ownerUserId, int limit);
    int deleteConversation(String id, TenantId tenantId, long ownerUserId);
    /** Compensating delete used when generation finalization loses an optimistic-lock race. */
    int deleteMessage(String id, TenantId tenantId, long ownerUserId);
}
