package com.shiyu.ai.agent.implementation.service.impl;

import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * {@code ExecutionHistoryServiceImpl} 实现智能体模块的应用服务，负责编排用例流程并维护业务边界。
 */
@Slf4j
@Service
public class ExecutionHistoryServiceImpl implements ExecutionHistoryService {

    /**
     * agentExecutionRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutionRepository agentExecutionRepository;

    /**
     * {@code ExecutionHistoryServiceImpl} 创建并初始化当前类型实例。
     *
     * @param agentExecutionRepository 参数值，用于执行当前操作。
     */
    public ExecutionHistoryServiceImpl(AgentExecutionRepository agentExecutionRepository) {
        this.agentExecutionRepository = agentExecutionRepository;
    }

    /**
     * {@code startExecution} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     * @param sessionId 参数值，用于执行当前操作。
     * @param nodeId 参数值，用于执行当前操作。
     * @param nodeType 参数值，用于执行当前操作。
     * @param inputData 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String startExecution(
            ActorContext actor,
            String agentId,
            String version,
            String sessionId,
            String nodeId,
            String nodeType,
            String inputData) {
        String executionId = UUID.randomUUID().toString().replace("-", "");
        AgentExecutionBO exec = new AgentExecutionBO();
        exec.setExecutionId(executionId);
        exec.setAgentId(agentId);
        exec.setVersion(version);
        exec.setUserId(actor.userId().value());
        exec.setTenantId(actor.tenantId().value());
        exec.setSessionId(sessionId);
        exec.setNodeId(nodeId);
        exec.setNodeType(nodeType);
        exec.setInputData(inputData);
        exec.setStatus(
                com.shiyu.ai.agent.implementation.domain.enums.AgentExecutionStatus.RUNNING
                        .getCode());
        exec.setStartTime(LocalDateTime.now());
        exec.setCreateTime(LocalDateTime.now());
        agentExecutionRepository.insert(actor.tenantId(), exec);
        return executionId;
    }

    /**
     * {@code completeExecution} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     * @param outputData 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     * @param errorMessage 参数值，用于执行当前操作。
     */
    @Override
    public void completeExecution(
            ActorContext actor,
            String executionId,
            String outputData,
            Integer status,
            String errorMessage) {
        AgentExecutionBO exec =
                agentExecutionRepository.selectByExecutionId(actor.tenantId(), executionId);
        if (exec == null) {
            throw new IllegalStateException("执行记录不存在: " + executionId);
        }
        exec.setOutputData(outputData);
        exec.setStatus(status);
        exec.setErrorMessage(errorMessage);
        exec.setEndTime(LocalDateTime.now());
        exec.setDurationMs(
                java.time.Duration.between(exec.getStartTime(), exec.getEndTime()).toMillis());
        agentExecutionRepository.update(actor.tenantId(), exec);
    }
}
