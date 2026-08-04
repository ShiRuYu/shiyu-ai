package com.shiyu.ai.agent.service;

/**
 * Execution History 接口
 */

public interface ExecutionHistoryService {

    /**
     * Start Execution
     * @return 处理结果
     */
    String startExecution(String agentId, String version, Long userId, String sessionId, String nodeId, String nodeType, String inputData);

    /**
     * Complete Execution
     * @return 处理结果
     */
    void completeExecution(String executionId, String outputData, Integer status, String errorMessage);
}
