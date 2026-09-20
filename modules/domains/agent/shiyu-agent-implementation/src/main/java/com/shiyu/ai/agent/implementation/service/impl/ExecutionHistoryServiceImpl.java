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
 * 提供 Execution History 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
@Slf4j
@Service
public class ExecutionHistoryServiceImpl implements ExecutionHistoryService {

    /**
     * agentExecutionRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentExecutionRepository agentExecutionRepository;

    /**
     * 执行 Execution History 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentExecutionRepository 用于完成本次业务处理的 agentExecutionRepository 参数。
     */
    public ExecutionHistoryServiceImpl(AgentExecutionRepository agentExecutionRepository) {
        this.agentExecutionRepository = agentExecutionRepository;
    }

    /**
     * 执行 Execution History 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @param sessionId 用于定位session的标识。
     * @param nodeId 用于定位node的标识。
     * @param nodeType 用于完成本次业务处理的 nodeType 参数。
     * @param inputData 用于完成本次业务处理的 inputData 参数。
     * @return 返回 Execution History 相关操作生成的结果数据。
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
     * 执行 Execution History 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param executionId 用于定位execution的标识。
     * @param outputData 用于完成本次业务处理的 outputData 参数。
     * @param status 用于完成本次业务处理的 status 参数。
     * @param errorMessage 用于完成本次业务处理的 errorMessage 参数。
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
