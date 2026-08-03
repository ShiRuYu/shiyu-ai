package com.shiyu.ai.agent.port.repository;

import com.shiyu.ai.agent.domain.model.ExecutionTimelineBO;
import java.util.List;

public interface ExecutionTimelineRepository {
    void insert(ExecutionTimelineBO timeline);
    List<ExecutionTimelineBO> listByExecutionId(String executionId);
}
