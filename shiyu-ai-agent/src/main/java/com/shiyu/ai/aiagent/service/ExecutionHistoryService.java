package com.shiyu.ai.aiagent.service;

public interface ExecutionHistoryService {

    String startExecution(String agentId, String version, Long userId, String sessionId, String nodeId, String nodeType, String inputData);

    void completeExecution(String executionId, String outputData, String status, String errorMessage);
}
