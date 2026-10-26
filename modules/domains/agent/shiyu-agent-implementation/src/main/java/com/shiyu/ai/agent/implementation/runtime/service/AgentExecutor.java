package com.shiyu.ai.agent.implementation.runtime.service;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.agent.contract.node.NodeApprovalRequiredException;
import com.shiyu.ai.agent.implementation.checkpoint.Checkpoint;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.checkpoint.AgentGraphCheckpointSaver;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.NodeExecution;
import com.shiyu.ai.agent.implementation.graph.Graph;
import com.shiyu.ai.agent.implementation.graph.StateGraphBuilder;
import com.shiyu.ai.agent.implementation.retry.RetryConfig;
import com.shiyu.ai.agent.implementation.retry.RetryPolicy;
import com.shiyu.ai.agent.implementation.retry.RetryPolicyImpl;
import com.shiyu.ai.agent.implementation.timeout.TimeoutConfig;
import com.shiyu.ai.agent.implementation.timeout.TimeoutPolicy;
import com.shiyu.ai.agent.implementation.timeout.TimeoutPolicyImpl;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.Callable;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.GraphInput;
import org.bsc.langgraph4j.RunnableConfig;

/**
 * 实现 智能体 Executor 相关的业务处理、协作逻辑或基础设施能力。
 */
@Slf4j
public class AgentExecutor implements AutoCloseable {

    /**
     * checkpointManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final CheckpointManager checkpointManager;
    /**
     * retryPolicy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final RetryPolicy retryPolicy;
    /**
     * timeoutPolicy 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TimeoutPolicy timeoutPolicy;
    /**
     * retryConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final RetryConfig retryConfig;
    /**
     * timeoutConfig 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final TimeoutConfig timeoutConfig;

    /**
     * 执行 智能体 Executor 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param checkpointManager 用于完成本次业务处理的 checkpointManager 参数。
     */
    public AgentExecutor(CheckpointManager checkpointManager) {
        this.checkpointManager = checkpointManager;
        this.retryPolicy = new RetryPolicyImpl();
        this.timeoutPolicy = new TimeoutPolicyImpl();
        this.retryConfig = RetryConfig.defaultConfig();
        this.timeoutConfig = TimeoutConfig.defaultConfig();
    }

    /**
     * {@code close} 释放或移除当前操作涉及的资源。
     */
    @Override
    public void close() {
        timeoutPolicy.close();
    }

    /** 同步执行 Agent（使用 CompiledGraph.invoke） */
    public Execution executeAgent(
            TenantId tenantId,
            AgentDefinition definition,
            AgentVersion agentVersion,
            Map<String, Object> input,
            Execution execution) {
        return executeAgent(tenantId, definition, agentVersion, input, execution,
                ignored -> {});
    }

    /** 执行 Agent，并将每个已持久化的检查点记录到所属执行中。 */
    public Execution executeAgent(
            TenantId tenantId,
            AgentDefinition definition,
            AgentVersion agentVersion,
            Map<String, Object> input,
            Execution execution,
            Consumer<String> checkpointRecorded) {
        if (execution.getStatus()
                == com.shiyu.ai.agent.implementation.execution.ExecutionStatus.PENDING) {
            execution.start();
        }
        log.info(
                "Agent 执行开始: agentIdPresent={}, executionIdPresent={}",
                definition.getAgentId() != null,
                execution.getExecutionId() != null);

        try {
            if (!execution.awaitResumeOrCancellation()
                    || execution.getStatus()
                            != com.shiyu.ai.agent.implementation.execution.ExecutionStatus.RUNNING) {
                return execution;
            }
            Graph graph = agentVersion.getGraph();
            if (graph == null) {
                throw new IllegalStateException("Agent 版本 graph 为空");
            }

            // 编译并执行图
            var compiled = StateGraphBuilder.fromGraph(graph).build(CompileConfig.builder()
                    .checkpointSaver(new AgentGraphCheckpointSaver(tenantId,
                            execution.getExecutionId(), checkpointManager,
                            checkpointRecorded)).build());
            RunnableConfig config = RunnableConfig.builder()
                    .threadId(execution.getExecutionId()).build();
            Map<String, Object> result = NodeActorContext.publicData(compiled
                    .invoke(GraphInput.args(input), config).orElseThrow(() ->
                            new IllegalStateException("Graph执行返回空结果")).data());

            if (!execution.awaitResumeOrCancellation()
                    || execution.getStatus()
                            != com.shiyu.ai.agent.implementation.execution.ExecutionStatus.RUNNING) {
                return execution;
            }

            // 记录节点执行（从结果中提取）
            NodeExecution nodeExec = new NodeExecution("graph_exec", "COMPILED_GRAPH");
            nodeExec.setInput(NodeActorContext.publicData(input));
            nodeExec.start();
            nodeExec.complete(result);
            execution.addNodeExecution(nodeExec);

            // 保存检查点
            Checkpoint finalCheckpoint = checkpointManager.createCheckpoint(
                    tenantId, execution.getExecutionId(), "graph_final", result);
            checkpointRecorded.accept(finalCheckpoint.getCheckpointId());

            if (execution.isSuperseded() && execution.shouldYieldAfterCheckpoint()) return execution;
            execution.complete(result);
            log.info(
                    "Agent 执行完成: agentIdPresent={}, executionIdPresent={}, duration={}ms",
                    definition.getAgentId() != null,
                    execution.getExecutionId() != null,
                    execution.getDurationMs());

        } catch (Exception e) {
            if (execution.getStatus()
                    == com.shiyu.ai.agent.implementation.execution.ExecutionStatus.CANCELLED) {
                return execution;
            }
            if (approvalRequired(e) != null) {
                if (execution.getStatus() == com.shiyu.ai.agent.implementation.execution.ExecutionStatus.RUNNING) {
                    execution.pause();
                }
                return execution;
            }
            execution.fail("执行异常: " + e.getMessage());
            log.error(
                    "Agent 执行异常: agentIdPresent={}, executionIdPresent={}, errorType={},"
                            + " errorMessageLength={}",
                    definition.getAgentId() != null,
                    execution.getExecutionId() != null,
                    e.getClass().getSimpleName(),
                    e.getMessage() == null ? 0 : e.getMessage().length());
        }

        return execution;
    }

    /** 从检查点恢复执行 */
    public Execution resumeFromCheckpoint(
            ActorContext actor,
            Execution execution,
            AgentDefinition definition,
            AgentVersion agentVersion,
            Checkpoint checkpoint) {
        return resumeFromCheckpoint(actor, execution, definition, agentVersion, checkpoint,
                ignored -> {});
    }

    /** 从检查点恢复执行，并记录后续节点产生的检查点。 */
    public Execution resumeFromCheckpoint(
            ActorContext actor,
            Execution execution,
            AgentDefinition definition,
            AgentVersion agentVersion,
            Checkpoint checkpoint,
            Consumer<String> checkpointRecorded) {
        return resumeFromCheckpoint(actor, execution, definition, agentVersion, checkpoint,
                checkpointRecorded, null);
    }

    public Execution resumeFromCheckpoint(
            ActorContext actor,
            Execution execution,
            AgentDefinition definition,
            AgentVersion agentVersion,
            Checkpoint checkpoint,
            Consumer<String> checkpointRecorded,
            AiRun runtimeRun) {
        log.info("从检查点恢复执行: executionIdPresent={}", execution.getExecutionId() != null);
        if (execution.getStatus()
                == com.shiyu.ai.agent.implementation.execution.ExecutionStatus.CANCELLED) {
            return execution;
        }

        try {
            if (checkpoint == null
                    || !actor.tenantId().equals(checkpoint.getTenantId())
                    || !execution.getExecutionId().equals(checkpoint.getExecutionId())) {
                throw new IllegalStateException("检查点与当前执行不匹配");
            }
            Map<String, Object> result;
            if ("graph_final".equals(checkpoint.getNodeId())) {
                result = NodeActorContext.publicData(checkpoint.getState());
            } else {
                if (checkpoint.getNextNodeId() == null || agentVersion.getGraph() == null) {
                    throw new IllegalStateException("检查点缺少下一节点或 Agent 图");
                }
                var compiled = StateGraphBuilder.fromGraph(agentVersion.getGraph()).build(
                        CompileConfig.builder().checkpointSaver(new AgentGraphCheckpointSaver(
                                actor.tenantId(), execution.getExecutionId(),
                                checkpointManager, checkpointRecorded)).build());
                RunnableConfig config = RunnableConfig.builder()
                        .threadId(execution.getExecutionId())
                        .checkPointId(checkpoint.getCheckpointId()).build();
                result = NodeActorContext.publicData(compiled.invoke(
                        GraphInput.resume(NodeActorContext.bind(actor,
                                AgentExecutionLifecycle.withRuntime(
                                        AgentExecutionLifecycle.withExecution(Map.of(), execution),
                                        runtimeRun))), config)
                        .orElseThrow(() -> new IllegalStateException("图续跑返回空结果"))
                        .data());
            }

            if (!execution.awaitResumeOrCancellation()
                    || execution.getStatus()
                            != com.shiyu.ai.agent.implementation.execution.ExecutionStatus.RUNNING) {
                return execution;
            }
            if (execution.isSuperseded()) return execution;
            execution.complete(result);
        } catch (Exception e) {
            if (execution.getStatus()
                    == com.shiyu.ai.agent.implementation.execution.ExecutionStatus.CANCELLED) {
                return execution;
            }
            if (approvalRequired(e) != null) {
                execution.pause();
                return execution;
            }
            execution.fail("恢复执行异常: " + e.getMessage());
        }

        return execution;
    }

    static NodeApprovalRequiredException approvalRequired(Throwable error) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (current instanceof NodeApprovalRequiredException pending) return pending;
        }
        return null;
    }

    /** 带重试和超时的图执行 */
    public Map<String, Object> executeWithRetryAndTimeout(
            AgentDefinition definition, AgentVersion agentVersion, Map<String, Object> input)
            throws Exception {

        Callable<Map<String, Object>> task =
                () -> {
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
                retryConfig);
    }
}
