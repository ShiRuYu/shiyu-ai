package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.NodeExecutionBO;
import java.util.List;

public interface NodeExecutionRepository {
    void insert(NodeExecutionBO bo);
    void update(NodeExecutionBO bo);
    List<NodeExecutionBO> selectByExecutionId(String executionId);
}
