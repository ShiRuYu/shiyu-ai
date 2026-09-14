package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationEvent;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * GenerationRepository 仓储接口，负责访问和持久化会话领域聚合数据。
 */
public interface GenerationRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param generation 方法参数。
     */
    void insert(GenerationRun generation);

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<GenerationRun> find(String id, TenantId tenantId, long ownerUserId);

    /**
     * 判断当前条件是否满足。
     *
     * @param conversationId 方法参数。
     * @param inputMessageId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 条件是否满足。
     */
    boolean hasRunning(String conversationId, String inputMessageId, TenantId tenantId);

    /**
     * 判断当前条件是否满足。
     *
     * @param conversationId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 条件是否满足。
     */
    boolean hasRunningConversation(String conversationId, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param conversationId 方法参数。
     * @param tenantId 租户标识。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<GenerationRun> listConversation(String conversationId, TenantId tenantId, int limit);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param generation 方法参数。
     * @param expectedVersion 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(GenerationRun generation, long expectedVersion);

    /**
     * 执行 {@code appendEvent} 定义的接口操作。
     *
     * @param event 方法参数。
     * @param tenantId 租户标识。
     */
    void appendEvent(GenerationEvent event, TenantId tenantId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param generationId 方法参数。
     * @param tenantId 租户标识。
     * @param afterSequence 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<GenerationEvent> listEvents(
            String generationId, TenantId tenantId, int afterSequence, int limit);

    /**
     * 执行 {@code nextEventSequence} 定义的接口操作。
     *
     * @param generationId 方法参数。
     * @param tenantId 租户标识。
     *
     * @return 操作影响的记录数或状态码。
     */
    int nextEventSequence(String generationId, TenantId tenantId);
}
