package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import java.util.List;

public interface AgentExecutionRepository {
    void insert(AgentExecutionBO bo);
    void update(AgentExecutionBO bo);
    AgentExecutionBO selectByExecutionId(String executionId);
    List<AgentExecutionBO> selectBySessionId(String sessionId);
    List<AgentExecutionBO> selectByAgentId(String agentId, int limit);
}
