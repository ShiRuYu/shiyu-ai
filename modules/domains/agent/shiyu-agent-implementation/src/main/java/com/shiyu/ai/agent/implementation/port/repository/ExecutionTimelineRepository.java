package com.shiyu.ai.agent.implementation.port.repository;

import com.shiyu.ai.agent.implementation.domain.model.ExecutionTimelineBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface ExecutionTimelineRepository {
    void insert(TenantId tenantId, ExecutionTimelineBO timeline);
    List<ExecutionTimelineBO> listByExecutionId(TenantId tenantId, String executionId);
}
