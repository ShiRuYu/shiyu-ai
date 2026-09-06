package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.checkpoint.Checkpoint;
import com.shiyu.ai.agent.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.NodeExecution;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.retry.RetryConfig;
import com.shiyu.ai.agent.retry.RetryPolicy;
import com.shiyu.ai.agent.retry.RetryPolicyImpl;
import com.shiyu.ai.agent.timeout.TimeoutConfig;
import com.shiyu.ai.agent.timeout.TimeoutPolicy;
import com.shiyu.ai.agent.timeout.TimeoutPolicyImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Callable;
import com.shiyu.ai.kernel.context.TenantId;

/**
 * Agent 执行器
 * 基于 LangGraph4j CompiledGraph，包装检查点、重试、超时等能力
 */
@Slf4j
public class AgentExecutor {

    private final CheckpointManager checkpointManager;
    private final RetryPolicy retryPolicy;
    private final TimeoutPolicy timeoutPolicy;
    private final RetryConfig retryConfig;
    private final TimeoutConfig timeoutConfig;

    public AgentExecutor(CheckpointManager checkpointManager) {
        this.checkpointManager = checkpointManager;
        this.retryPolicy = new RetryPolicyImpl();
        this.timeoutPolicy = new TimeoutPolicyImpl();
        this.retryConfig = RetryConfig.defaultConfig();
        this.timeoutConfig = TimeoutConfig.defaultConfig();
    }

    /**
     * 同步执行 Agent（使用 CompiledGraph.invoke）
     */
    public Execution executeAgent(TenantId tenantId, AgentDefinition definition, AgentVersion agentVersion,
                                   Map<String, Object> input, Execution execution) {
        if (execution.getStatus() == com.shiyu.ai.agent.execution.ExecutionStatus.PENDING) {
            execution.start();
        }
        log.info("Agent 执行开始: agentId={}, executionId={}",
                definition.getAgentId(), execution.getExecutionId());

        try {
            if (!execution.awaitResumeOrCancellation()) {
                return execution;
            }
            Graph graph = agentVersion.getGraph();
            if (graph == null) {
                throw new IllegalStateException("Agent 版本 graph 为空");
            }

            // 编译并执行图
            Map<String, Object> result = agentVersion.getGraph().execute(input);

            if (!execution.awaitResumeOrCancellation()) {
                return execution;
            }
            
            // 记录节点执行（从结果中提取）
            NodeExecution nodeExec = new NodeExecution("graph_exec", "COMPILED_GRAPH");
            nodeExec.setInput(input);
            nodeExec.start();
            nodeExec.complete(result);
            execution.addNodeExecution(nodeExec);

            // 保存检查点
            checkpointManager.createCheckpoint(tenantId,
                execution.getExecutionId(), "graph_final", result
            );

            execution.complete(result);
            log.info("Agent 执行完成: agentId={}, executionId={}, duration={}ms",
                    definition.getAgentId(), execution.getExecutionId(), execution.getDurationMs());

        } catch (Exception e) {
            if (execution.getStatus() == com.shiyu.ai.agent.execution.ExecutionStatus.CANCELLED) {
                return execution;
            }
            execution.fail("执行异常: " + e.getMessage());
            log.error("Agent 执行异常: agentId={}, executionId={}",
                    definition.getAgentId(), execution.getExecutionId(), e);
        }

        return execution;
    }

    /**
     * 从检查点恢复执行
     */
    public Execution resumeFromCheckpoint(TenantId tenantId, Execution execution, AgentDefinition definition,
                                           AgentVersion agentVersion, Checkpoint checkpoint) {
        execution.resume();
        log.info("从检查点恢复执行: executionId={}", execution.getExecutionId());

        try {
            Map<String, Object> state = checkpoint.getState();
            Map<String, Object> result = agentVersion.getGraph().execute(state);

            if (!execution.awaitResumeOrCancellation()) {
                return execution;
            }

            NodeExecution nodeExec = new NodeExecution("graph_resume", "COMPILED_GRAPH");
            nodeExec.setInput(state);
            nodeExec.start();
            nodeExec.complete(result);
            execution.addNodeExecution(nodeExec);

            execution.complete(result);
        } catch (Exception e) {
            if (execution.getStatus() == com.shiyu.ai.agent.execution.ExecutionStatus.CANCELLED) {
                return execution;
            }
            execution.fail("恢复执行异常: " + e.getMessage());
        }

        return execution;
    }

    /**
     * 带重试和超时的图执行
     */
    public Map<String, Object> executeWithRetryAndTimeout(
            AgentDefinition definition, AgentVersion agentVersion, Map<String, Object> input) throws Exception {

        Callable<Map<String, Object>> task = () -> {
            try {
                return agentVersion.getGraph().execute(input);
            } catch (Exception e) {
                throw new RuntimeException("图执行失败", e);
            }
        };

        return retryPolicy.executeWithRetry(
            () -> {
                try {
                    return timeoutPolicy.executeWithTimeout(task, timeoutConfig);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            retryConfig
        );
    }
}
