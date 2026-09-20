package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * 负责 Execution 时间线 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface ExecutionTimelineRepository {
    /**
     * 创建或保存 Execution 时间线 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param timeline 用于完成本次业务处理的 timeline 参数。
     */
    void insert(TenantId tenantId, ExecutionTimelineBO timeline);

    /**
     * 查询 Execution 时间线 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<ExecutionTimelineBO> listByExecutionId(TenantId tenantId, String executionId);
}
