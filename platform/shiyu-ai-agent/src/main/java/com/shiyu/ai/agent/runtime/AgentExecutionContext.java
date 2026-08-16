package com.shiyu.ai.agent.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Ephemeral execution state; durable recovery remains in Agent checkpoint tables. */
public class AgentExecutionContext {
    private final Cache<String, State> states = Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(2)).maximumSize(10_000).build();
    public void append(long tenantId, String executionId, String role, String content) { if (content == null || content.isBlank()) return; states.get(tenantId + ":" + executionId, k -> new State()).messages.add(role + ": " + content); }
    public List<String> messages(long tenantId, String executionId, int limit) { State state = states.getIfPresent(tenantId + ":" + executionId); if (state == null) return List.of(); synchronized (state.messages) { int from = Math.max(0, state.messages.size() - Math.max(1, limit)); return List.copyOf(state.messages.subList(from, state.messages.size())); } }
    public Map<String,Object> variables(long tenantId, String executionId) { return states.get(tenantId + ":" + executionId, k -> new State()).variables; }
    public void clear(long tenantId, String executionId) { states.invalidate(tenantId + ":" + executionId); }
    private static final class State { private final List<String> messages = new ArrayList<>(); private final Map<String,Object> variables = new ConcurrentHashMap<>(); }
}
