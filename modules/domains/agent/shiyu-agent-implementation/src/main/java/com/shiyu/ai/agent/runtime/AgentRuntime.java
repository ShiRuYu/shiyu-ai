package com.shiyu.ai.agent.runtime;

import com.shiyu.ai.agent.execution.Execution;
import com.shiyu.ai.agent.execution.ExecutionStatus;
import com.shiyu.ai.kernel.context.ActorContext;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Agent 运行时接口
 * 提供执行生命周期管理：暂停/恢复/取消/检查点
 */
public interface AgentRuntime {

    /**
     * 同步执行 Agent
     */
    Execution execute(ActorContext actor, String agentId, Map<String, Object> input);

    /**
     * 按版本同步执行 Agent
     */
    Execution execute(ActorContext actor, String agentId, String version, Map<String, Object> input);

    /**
     * 流式执行 Agent
     */
    Flux<Map<String, Object>> executeStream(ActorContext actor, String agentId, Map<String, Object> input);

    /**
     * 按版本流式执行 Agent
     */
    Flux<Map<String, Object>> executeStream(ActorContext actor, String agentId, String version, Map<String, Object> input);

    /**
     * 暂停执行
     */
    void pause(ActorContext actor, String executionId);

    /**
     * 恢复执行
     */
    Execution resume(ActorContext actor, String executionId);

    /**
     * 取消执行
     */
    void cancel(ActorContext actor, String executionId);

    /**
     * 查询执行状态
     */
    ExecutionStatus getStatus(ActorContext actor, String executionId);

    /**
     * 获取执行详情
     */
    Execution getExecution(ActorContext actor, String executionId);

    /**
     * 查询 Agent 执行历史
     */
    List<Execution> getHistory(ActorContext actor, String agentId, int limit);

    /**
     * 查询用户执行历史
     */
    List<Execution> getUserHistory(ActorContext actor, Long userId, int limit);
}
