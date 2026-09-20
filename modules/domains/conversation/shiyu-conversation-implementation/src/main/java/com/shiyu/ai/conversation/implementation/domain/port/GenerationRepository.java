package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.conversation.contract.model.GenerationRun;
import com.shiyu.ai.conversation.implementation.domain.model.GenerationEvent;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * 负责 生成 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface GenerationRepository {
    /**
     * 创建或保存 生成 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param generation 用于完成本次业务处理的 generation 参数。
     */
    void insert(GenerationRun generation);

    /**
     * 查询 生成 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<GenerationRun> find(String id, TenantId tenantId, long ownerUserId);

    /**
     * 校验或判断 生成 相关业务数据，并返回处理结果。
     *
     * @param conversationId 用于定位conversation的标识。
     * @param inputMessageId 用于定位input 消息的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean hasRunning(String conversationId, String inputMessageId, TenantId tenantId);

    /**
     * 校验或判断 生成 相关业务数据，并返回处理结果。
     *
     * @param conversationId 用于定位conversation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean hasRunningConversation(String conversationId, TenantId tenantId);

    /**
     * 查询 生成 相关业务数据，并返回处理结果。
     *
     * @param conversationId 用于定位conversation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<GenerationRun> listConversation(String conversationId, TenantId tenantId, int limit);

    /**
     * 更新或设置 生成 相关业务数据，并返回处理结果。
     *
     * @param generation 用于完成本次业务处理的 generation 参数。
     * @param expectedVersion 用于完成本次业务处理的 expectedVersion 参数。
     * @return 返回 生成 相关操作生成的结果数据。
     */
    int update(GenerationRun generation, long expectedVersion);

    /**
     * 执行 生成 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     * @param tenantId 当前操作涉及的租户标识。
     */
    void appendEvent(GenerationEvent event, TenantId tenantId);

    /**
     * 查询 生成 相关业务数据，并返回处理结果。
     *
     * @param generationId 用于定位generation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param afterSequence 用于完成本次业务处理的 afterSequence 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<GenerationEvent> listEvents(
            String generationId, TenantId tenantId, int afterSequence, int limit);

    /**
     * 执行 生成 相关业务数据，并返回处理结果。
     *
     * @param generationId 用于定位generation的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @return 返回 生成 相关操作生成的结果数据。
     */
    int nextEventSequence(String generationId, TenantId tenantId);
}
