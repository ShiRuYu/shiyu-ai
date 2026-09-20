package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.conversation.implementation.domain.model.Conversation;
import com.shiyu.ai.conversation.implementation.domain.model.ConversationMessage;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 负责 会话 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface ConversationRepository {
    /**
     * 创建或保存 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     */
    void insertConversation(Conversation conversation);

    /**
     * 查询 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<Conversation> findConversation(String id, TenantId tenantId, long ownerUserId);

    /**
     * 查询 会话 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @param offset 用于完成本次业务处理的 offset 参数。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Conversation> listConversations(
            TenantId tenantId, long ownerUserId, int limit, int offset);

    /**
     * 查询 会话 相关业务数据，并返回处理结果。
     *
     * @param parentConversationId 用于定位parent 会话的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<Conversation> listBranches(
            String parentConversationId, TenantId tenantId, long ownerUserId);

    /**
     * 更新或设置 会话 相关业务数据，并返回处理结果。
     *
     * @param conversation 用于完成本次业务处理的 conversation 参数。
     * @param expectedVersion 用于完成本次业务处理的 expectedVersion 参数。
     * @return 返回 会话 相关操作生成的结果数据。
     */
    int updateConversation(Conversation conversation, long expectedVersion);

    /**
     * 创建或保存 会话 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param message 本次流程携带的事件或业务数据。
     */
    void insertMessage(ConversationMessage message);

    /**
     * 查询 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<ConversationMessage> findMessage(String id, TenantId tenantId, long ownerUserId);

    /**
     * 查询 会话 相关业务数据，并返回处理结果。
     *
     * @param conversationId 用于定位conversation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ConversationMessage> listMessages(
            String conversationId, TenantId tenantId, long ownerUserId, int limit);

    /**
     * 删除或移除 会话 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 会话 相关操作生成的结果数据。
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
