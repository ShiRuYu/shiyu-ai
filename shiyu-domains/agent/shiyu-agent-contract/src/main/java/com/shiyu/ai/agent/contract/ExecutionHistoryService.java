package com.shiyu.ai.agent.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * Execution History 接口
 */

public interface ExecutionHistoryService {

    /**
     * Start Execution
     * @return 处理结果
     */
    String startExecution(ActorContext actor, String agentId, String version, String sessionId,
                          String nodeId, String nodeType, String inputData);

    /**
     * Complete Execution
     * @return 处理结果
     */
    void completeExecution(ActorContext actor, String executionId, String outputData,
                           Integer status, String errorMessage);
}
