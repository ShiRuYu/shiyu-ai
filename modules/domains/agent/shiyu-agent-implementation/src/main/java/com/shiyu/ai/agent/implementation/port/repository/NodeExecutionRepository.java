package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.NodeExecutionBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 Node Execution 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface NodeExecutionRepository {
    /**
     * 创建或保存 Node Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     */
    void insert(TenantId tenantId, NodeExecutionBO bo);

    /**
     * 更新或设置 Node Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param bo 用于完成本次业务处理的 bo 参数。
     */
    void update(TenantId tenantId, NodeExecutionBO bo);

    /**
     * 查询 Node Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<NodeExecutionBO> selectByExecutionId(TenantId tenantId, String executionId);
}
