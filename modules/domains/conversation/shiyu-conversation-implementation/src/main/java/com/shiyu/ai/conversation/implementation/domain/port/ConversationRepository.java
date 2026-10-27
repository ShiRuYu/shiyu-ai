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

    /**
     * 按预期版本原子提交生成完成状态、助手消息、会话更新及运行事件。
     *
     * @param completed 待提交的生成完成状态
     * @param expectedGenerationVersion 生成记录应匹配的乐观锁版本
     * @param assistant 生成的助手消息
     * @param updatedConversation 待提交的会话最新状态
     * @param expectedConversationVersion 会话记录应匹配的乐观锁版本
     * @param runtimeEvents 随状态变更追加的运行事件类型
     *
     * @return 成功提交时返回 1；版本或运行状态已变化时返回 0
     */
    default int completeGeneration(
            com.shiyu.ai.conversation.contract.model.GenerationRun completed,
            long expectedGenerationVersion,
            ConversationMessage assistant,
            Conversation updatedConversation,
            long expectedConversationVersion,
            java.util.List<com.shiyu.ai.agent.contract.runtime.AiRunEventType> runtimeEvents) {
        return 0;
    }

    /**
     * 按预期版本将运行中的生成转为指定终态，并追加运行事件。
     *
     * @param terminal 待提交的生成终态
     * @param expectedGenerationVersion 生成记录应匹配的乐观锁版本
     * @param ownerUserId 生成或幂等记录所属用户标识
     * @param runtimeEvents 随状态变更追加的运行事件类型
     *
     * @return 成功转换时返回 1；版本或运行状态已变化时返回 0
     */
    default int transitionRunningGeneration(
            com.shiyu.ai.conversation.contract.model.GenerationRun terminal,
            long expectedGenerationVersion,
            long ownerUserId,
            java.util.List<com.shiyu.ai.agent.contract.runtime.AiRunEventType> runtimeEvents) {
        return 0;
    }

    /**
     * 领取指定生成的待执行后处理任务，并返回带领取凭据的任务状态。
     *
     * @param generationId 生成运行标识
     *
     * @return 成功领取的任务；不存在可领取任务时为空
     */
    default java.util.Optional<com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing>
            claimGenerationPostProcessing(String generationId) {
        return java.util.Optional.empty();
    }

    /**
     * 领取下一条已到执行时间的生成后处理任务。
     *
     * @return 成功领取的任务；暂无可领取任务时为空
     */
    default java.util.Optional<com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing>
            claimGenerationPostProcessingNext() {
        return java.util.Optional.empty();
    }

    /**
     * 仅在领取凭据仍有效时，推进生成后处理的指定步骤。
     *
     * @param generationId 生成运行标识
     * @param step 已完成的后处理步骤
     * @param claimToken 当前后处理领取凭据
     *
     * @return 成功推进时返回 1；领取凭据已失效时返回 0
     */
    default int completeGenerationPostProcessing(
            String generationId,
            com.shiyu.ai.conversation.implementation.domain.model.GenerationPostProcessing.Step step,
            String claimToken) {
        return 0;
    }

    /**
     * 释放当前领取的后处理任务，并安排延迟重试。
     *
     * @param generationId 生成运行标识
     * @param claimToken 当前后处理领取凭据
     * @param retryDelayMillis 再次允许领取前的等待毫秒数
     *
     * @return 成功释放时返回 1；领取凭据已失效时返回 0
     */
    default int releaseGenerationPostProcessing(
            String generationId, String claimToken, long retryDelayMillis) {
        return 0;
    }

}
