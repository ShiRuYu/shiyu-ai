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
 * 承载 Agent 执行过程中的租户、会话和运行时上下文。
 */
public class AgentExecutionContext {
    private final Cache<String, State> states =
            Caffeine.newBuilder()
                    .expireAfterAccess(Duration.ofHours(2))
                    .maximumSize(10_000)
                    .build();

    /**
     * {@code append} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     * @param role 参数值，用于执行当前操作。
     * @param content 参数值，用于执行当前操作。
     */
    public void append(TenantId tenantId, String executionId, String role, String content) {
        if (content == null || content.isBlank()) return;
        states.get(key(tenantId, executionId), k -> new State())
                .messages
                .add(role + ": " + content);
    }

    /**
     * {@code messages} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     * @param limit 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code variables} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public Map<String, Object> variables(TenantId tenantId, String executionId) {
        return states.get(key(tenantId, executionId), k -> new State()).variables;
    }

    /**
     * {@code clear} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param executionId 参数值，用于执行当前操作。
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
     * {@code State} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
     */
    private static final class State {
        private final List<String> messages = new ArrayList<>();
        private final Map<String, Object> variables = new ConcurrentHashMap<>();
    }
}
