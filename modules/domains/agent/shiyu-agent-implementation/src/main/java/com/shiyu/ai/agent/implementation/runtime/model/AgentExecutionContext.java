package com.shiyu.ai.agent.implementation.runtime.model;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现 智能体 Execution 相关的业务处理、协作逻辑或基础设施能力。
 */
public class AgentExecutionContext {
    private final Cache<String, State> states =
            Caffeine.newBuilder()
                    .expireAfterAccess(Duration.ofHours(2))
                    .maximumSize(10_000)
                    .build();

    /**
     * 执行 智能体 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @param role 用于完成本次业务处理的 role 参数。
     * @param content 用于完成本次业务处理的 content 参数。
     */
    public void append(TenantId tenantId, String executionId, String role, String content) {
        if (content == null || content.isBlank()) return;
        states.get(key(tenantId, executionId), k -> new State())
                .messages
                .add(role + ": " + content);
    }

    /**
     * 执行 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<String> messages(TenantId tenantId, String executionId, int limit) {
        State state = states.getIfPresent(key(tenantId, executionId));
        if (state == null) return List.of();
        synchronized (state.messages) {
            int from = Math.max(0, state.messages.size() - Math.max(1, limit));
            return List.copyOf(state.messages.subList(from, state.messages.size()));
        }
    }

    /**
     * 执行 智能体 Execution 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     * @return 返回 智能体 Execution 相关操作生成的结果数据。
     */
    public Map<String, Object> variables(TenantId tenantId, String executionId) {
        return states.get(key(tenantId, executionId), k -> new State()).variables;
    }

    /**
     * 删除或移除 智能体 Execution 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param executionId 用于定位execution的标识。
     */
    public void clear(TenantId tenantId, String executionId) {
        states.invalidate(key(tenantId, executionId));
    }

    private static String key(TenantId tenantId, String executionId) {
        return java.util.Objects.requireNonNull(tenantId, "tenantId must not be null").value()
                + ":"
                + java.util.Objects.requireNonNull(executionId, "executionId must not be null");
    }

    /**
     * 表示 State 相关流程中的状态、关系或执行数据。
     */
    private static final class State {
        private final List<String> messages = new ArrayList<>();
        private final Map<String, Object> variables = new ConcurrentHashMap<>();
    }
}
