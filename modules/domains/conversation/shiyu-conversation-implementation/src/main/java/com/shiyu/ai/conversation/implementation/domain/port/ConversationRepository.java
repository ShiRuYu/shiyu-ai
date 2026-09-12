package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * ConversationRepository 仓储接口，负责访问和持久化会话领域聚合数据。
 */
public interface ConversationRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param conversation 方法参数。
     */
    void insertConversation(Conversation conversation);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<Conversation> findConversation(String id, TenantId tenantId, long ownerUserId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param limit 方法参数。
     * @param offset 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Conversation> listConversations(
            TenantId tenantId, long ownerUserId, int limit, int offset);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param parentConversationId 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<Conversation> listBranches(
            String parentConversationId, TenantId tenantId, long ownerUserId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param conversation 方法参数。
     * @param expectedVersion 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int updateConversation(Conversation conversation, long expectedVersion);

    /**
     * 创建并保存业务对象。
     *
     * @param message 方法参数。
     */
    void insertMessage(ConversationMessage message);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<ConversationMessage> findMessage(String id, TenantId tenantId, long ownerUserId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param conversationId 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ConversationMessage> listMessages(
            String conversationId, TenantId tenantId, long ownerUserId, int limit);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int deleteConversation(String id, TenantId tenantId, long ownerUserId);

    /**
     * 删除消息。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 所有者用户标识。
     *
     * @return 受影响的记录数或生成的序号。
     */
    int deleteMessage(String id, TenantId tenantId, long ownerUserId);
}
