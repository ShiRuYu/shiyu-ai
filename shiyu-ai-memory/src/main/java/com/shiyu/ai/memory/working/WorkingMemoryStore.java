package com.shiyu.ai.memory.working;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工作记忆 — 内存存储，会话级变量
 */
public class WorkingMemoryStore implements MemoryStore {

    private final ConcurrentHashMap<String, Map<String, Object>> workingMemory = new ConcurrentHashMap<>();

    public void setVariable(String sessionId, String key, Object value) {
        workingMemory.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>())
                .put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getVariable(String sessionId, String key) {
        Map<String, Object> vars = workingMemory.get(sessionId);
        if (vars == null) return null;
        return (T) vars.get(key);
    }

    public void clear(String sessionId) {
        workingMemory.remove(sessionId);
    }

    public Map<String, Object> getAllVariables(String sessionId) {
        return workingMemory.getOrDefault(sessionId, new ConcurrentHashMap<>());
    }

    @Override
    public void save(Memory memory) {
        if (memory.getSessionId() != null && memory.getMemoryKey() != null) {
            setVariable(memory.getSessionId(), memory.getMemoryKey(), memory.getContent());
        }
    }

    @Override
    public void saveBatch(List<Memory> memories) {
        memories.forEach(this::save);
    }

    @Override
    public List<Memory> query(MemoryQuery query) {
        Map<String, Object> vars = workingMemory.get(query.getSessionId());
        if (vars == null) return List.of();

        return vars.entrySet().stream()
                .limit(query.getTopK())
                .map(entry -> {
                    Memory mem = new Memory(MemoryType.WORKING, query.getSessionId(), "system",
                            entry.getValue() != null ? entry.getValue().toString() : "");
                    mem.setMemoryKey(entry.getKey());
                    mem.setCreatedAt(LocalDateTime.now());
                    return mem;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Memory queryById(String memoryId) {
        return null;
    }

    @Override
    public void delete(String memoryId) {
    }

    @Override
    public void deleteBySession(String sessionId) {
        workingMemory.remove(sessionId);
    }

    @Override
    public long count(MemoryQuery query) {
        Map<String, Object> vars = workingMemory.get(query.getSessionId());
        return vars != null ? vars.size() : 0;
    }
}
