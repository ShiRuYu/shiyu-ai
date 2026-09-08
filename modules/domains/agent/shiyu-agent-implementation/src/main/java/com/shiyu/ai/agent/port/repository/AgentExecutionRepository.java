package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;

public interface AgentExecutionRepository {
    void insert(TenantId tenantId, AgentExecutionBO bo);
    void update(TenantId tenantId, AgentExecutionBO bo);
    AgentExecutionBO selectByExecutionId(TenantId tenantId, String executionId);
    List<AgentExecutionBO> selectBySessionId(TenantId tenantId, String sessionId);
    List<AgentExecutionBO> selectByAgentId(TenantId tenantId, String agentId, int limit);
}
