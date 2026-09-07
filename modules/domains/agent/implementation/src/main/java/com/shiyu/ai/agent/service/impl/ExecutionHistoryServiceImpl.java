package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.port.repository.AgentExecutionRepository;
import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.kernel.context.ActorContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ExecutionHistoryServiceImpl implements ExecutionHistoryService {

    private final AgentExecutionRepository agentExecutionRepository;

    public ExecutionHistoryServiceImpl(AgentExecutionRepository agentExecutionRepository) {
        this.agentExecutionRepository = agentExecutionRepository;
    }

    @Override
    public String startExecution(ActorContext actor, String agentId, String version, String sessionId,
                                 String nodeId, String nodeType, String inputData) {
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
        exec.setStatus(com.shiyu.ai.agent.domain.enums.AgentExecutionStatus.RUNNING.getCode());
        exec.setStartTime(LocalDateTime.now());
        exec.setCreateTime(LocalDateTime.now());
        agentExecutionRepository.insert(actor.tenantId(), exec);
        return executionId;
    }

    @Override
    public void completeExecution(ActorContext actor, String executionId, String outputData,
                                  Integer status, String errorMessage) {
        AgentExecutionBO exec = agentExecutionRepository.selectByExecutionId(actor.tenantId(), executionId);
        if (exec == null) {
            throw new IllegalStateException("执行记录不存在: " + executionId);
        }
        exec.setOutputData(outputData);
        exec.setStatus(status);
        exec.setErrorMessage(errorMessage);
        exec.setEndTime(LocalDateTime.now());
        exec.setDurationMs(java.time.Duration.between(exec.getStartTime(), exec.getEndTime()).toMillis());
        agentExecutionRepository.update(actor.tenantId(), exec);
    }
}
