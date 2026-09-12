package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.kernel.context.TenantId;

import java.util.List;

/**
 * ExecutionTimelineRepository 仓储接口，负责访问和持久化智能体领域聚合数据。
 */
public interface ExecutionTimelineRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param tenantId 租户标识。
     * @param timeline 方法参数。
     */
    void insert(TenantId tenantId, ExecutionTimelineBO timeline);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param executionId 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<ExecutionTimelineBO> listByExecutionId(TenantId tenantId, String executionId);
}
