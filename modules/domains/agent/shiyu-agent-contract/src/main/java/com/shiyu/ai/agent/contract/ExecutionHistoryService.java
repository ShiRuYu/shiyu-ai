package com.shiyu.ai.agent.contract;

import com.shiyu.ai.kernel.context.ActorContext;

/**
 * ExecutionHistoryService 服务接口，负责执行智能体领域相关业务操作。
 */
public interface ExecutionHistoryService {

    /**
     * 处理start执行。
     *
     * @param actor 调用方上下文。
     * @param agentId agentId 参数。
     * @param version version 参数。
     * @param sessionId sessionId 参数。
     * @param nodeId nodeId 参数。
     * @param nodeType nodeType 参数。
     * @param inputData inputData 参数。
     *
     * @return 处理结果。
     */
    String startExecution(
            ActorContext actor,
            String agentId,
            String version,
            String sessionId,
            String nodeId,
            String nodeType,
            String inputData);

    /**
     * 处理complete执行。
     *
     * @param actor 调用方上下文。
     * @param executionId 执行记录标识。
     * @param outputData outputData 参数。
     * @param status 状态。
     * @param errorMessage errorMessage 参数。
     */
    void completeExecution(
            ActorContext actor,
            String executionId,
            String outputData,
            Integer status,
            String errorMessage);
}
