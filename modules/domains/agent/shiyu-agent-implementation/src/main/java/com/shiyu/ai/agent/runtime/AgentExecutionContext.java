package com.shiyu.ai.agent.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Ephemeral execution state; durable recovery remains in Agent checkpoint tables. */
public class AgentExecutionContext {
    private final Cache<String, State> states = Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(2)).maximumSize(10_000).build();
    public void append(TenantId tenantId, String executionId, String role, String content) { if (content == null || content.isBlank()) return; states.get(key(tenantId, executionId), k -> new State()).messages.add(role + ": " + content); }
    public List<String> messages(TenantId tenantId, String executionId, int limit) { State state = states.getIfPresent(key(tenantId, executionId)); if (state == null) return List.of(); synchronized (state.messages) { int from = Math.max(0, state.messages.size() - Math.max(1, limit)); return List.copyOf(state.messages.subList(from, state.messages.size())); } }
    public Map<String,Object> variables(TenantId tenantId, String executionId) { return states.get(key(tenantId, executionId), k -> new State()).variables; }
    public void clear(TenantId tenantId, String executionId) { states.invalidate(key(tenantId, executionId)); }
    private static String key(TenantId tenantId, String executionId) { return java.util.Objects.requireNonNull(tenantId, "tenantId must not be null").value() + ":" + java.util.Objects.requireNonNull(executionId, "executionId must not be null"); }
    private static final class State { private final List<String> messages = new ArrayList<>(); private final Map<String,Object> variables = new ConcurrentHashMap<>(); }
}
