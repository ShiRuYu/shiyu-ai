package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.NodeExecutionBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface NodeExecutionRepository {
    void insert(TenantId tenantId, NodeExecutionBO bo);
    void update(TenantId tenantId, NodeExecutionBO bo);
    List<NodeExecutionBO> selectByExecutionId(TenantId tenantId, String executionId);
}
