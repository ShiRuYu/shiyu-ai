package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface AgentCheckpointRepository {
    void insert(TenantId tenantId, AgentCheckpointBO checkpoint);
    AgentCheckpointBO selectByCheckpointId(TenantId tenantId, String checkpointId);
    AgentCheckpointBO selectLatestByExecutionId(TenantId tenantId, String executionId);
    void deleteByCheckpointId(TenantId tenantId, String checkpointId);
    void deleteByExecutionId(TenantId tenantId, String executionId);
    List<AgentCheckpointBO> listByExecutionId(TenantId tenantId, String executionId);
}
