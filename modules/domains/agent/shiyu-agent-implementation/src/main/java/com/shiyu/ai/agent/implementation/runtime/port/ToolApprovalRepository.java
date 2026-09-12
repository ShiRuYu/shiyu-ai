package com.shiyu.ai.agent.implementation.runtime.port;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * ToolApprovalRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface ToolApprovalRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param approval 方法参数。
     */
    void insert(ToolApproval approval);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param runId 方法参数。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ToolApproval> listAll(TenantId tenantId, long ownerUserId);

    /**
     * 根据标识查询对应的数据。
     *
     * @param id 目标对象标识。
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param approval 方法参数。
     * @param expectedStatus 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int update(ToolApproval approval, ToolApprovalStatus expectedStatus);

    /**
     * 执行 {@code expirePending} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     *
     * @return 操作影响的记录数或状态码。
     */
    int expirePending(TenantId tenantId, long ownerUserId);
}
