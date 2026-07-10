package com.shiyu.ai.agent.lifecycle;

import com.shiyu.ai.agent.execution.ExecutionStatus;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Agent 状态机
 * 管理 Execution 状态的合法转换
 */
public class AgentStateMachine {

    private static final Map<ExecutionStatus, Set<ExecutionStatus>> TRANSITIONS = new EnumMap<>(ExecutionStatus.class);

    static {
        TRANSITIONS.put(ExecutionStatus.PENDING, Set.of(ExecutionStatus.RUNNING, ExecutionStatus.CANCELLED));
        TRANSITIONS.put(ExecutionStatus.RUNNING, Set.of(ExecutionStatus.COMPLETED, ExecutionStatus.FAILED, ExecutionStatus.PAUSED, ExecutionStatus.CANCELLED));
        TRANSITIONS.put(ExecutionStatus.PAUSED, Set.of(ExecutionStatus.RUNNING, ExecutionStatus.CANCELLED));
        TRANSITIONS.put(ExecutionStatus.COMPLETED, Set.of());
        TRANSITIONS.put(ExecutionStatus.FAILED, Set.of());
        TRANSITIONS.put(ExecutionStatus.CANCELLED, Set.of());
    }

    /**
     * 尝试状态转换
     * @param current 当前状态
     * @param target 目标状态
     * @throws IllegalStateException 如果转换非法
     */
    public static void transition(ExecutionStatus current, ExecutionStatus target) {
        Set<ExecutionStatus> allowed = TRANSITIONS.get(current);
        if (allowed == null || !allowed.contains(target)) {
            throw new IllegalStateException(
                String.format("非法状态转换: %s → %s", current, target)
            );
        }
    }

    /**
     * 检查状态转换是否合法
     */
    public static boolean canTransition(ExecutionStatus current, ExecutionStatus target) {
        Set<ExecutionStatus> allowed = TRANSITIONS.get(current);
        return allowed != null && allowed.contains(target);
    }
}
