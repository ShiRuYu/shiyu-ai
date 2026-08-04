package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.AgentCheckpointBO;
import java.util.List;

public interface AgentCheckpointRepository {
    void insert(AgentCheckpointBO checkpoint);
    AgentCheckpointBO selectByCheckpointId(String checkpointId);
    AgentCheckpointBO selectLatestByExecutionId(String executionId);
    void deleteByCheckpointId(String checkpointId);
    void deleteByExecutionId(String executionId);
    List<AgentCheckpointBO> listByExecutionId(String executionId);
}
