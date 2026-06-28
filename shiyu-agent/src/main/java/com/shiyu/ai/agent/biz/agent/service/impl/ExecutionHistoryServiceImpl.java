package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.dal.repository.AgentExecutionRepository;
import com.shiyu.ai.agent.biz.agent.service.ExecutionHistoryService;
import com.shiyu.ai.model.bo.AgentExecutionBO;
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
    public String startExecution(String agentId, String version, Long userId, String sessionId, String nodeId, String nodeType, String inputData) {
        String executionId = UUID.randomUUID().toString().replace("-", "");
        AgentExecutionBO exec = new AgentExecutionBO();
        exec.setExecutionId(executionId);
        exec.setAgentId(agentId);
        exec.setVersion(version);
        exec.setUserId(userId);
        exec.setSessionId(sessionId);
        exec.setNodeId(nodeId);
        exec.setNodeType(nodeType);
        exec.setInputData(inputData);
        exec.setStatus("RUNNING");
        exec.setStartTime(LocalDateTime.now());
        exec.setCreateTime(LocalDateTime.now());
        agentExecutionRepository.insert(exec);
        return executionId;
    }

    @Override
    public void completeExecution(String executionId, String outputData, String status, String errorMessage) {
        try {
            AgentExecutionBO exec = agentExecutionRepository.selectByExecutionId(executionId);
            if (exec == null) {
                log.warn("执行记录不存在: {}", executionId);
                return;
            }
            exec.setOutputData(outputData);
            exec.setStatus(status);
            exec.setErrorMessage(errorMessage);
            exec.setEndTime(LocalDateTime.now());
            exec.setDurationMs(java.time.Duration.between(exec.getStartTime(), exec.getEndTime()).toMillis());
            agentExecutionRepository.update(exec);
        } catch (Exception e) {
            log.warn("更新执行记录失败: {}", e.getMessage());
        }
    }
}
