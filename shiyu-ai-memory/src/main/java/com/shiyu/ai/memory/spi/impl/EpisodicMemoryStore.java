package com.shiyu.ai.memory.spi.impl;

import com.shiyu.ai.memory.domain.model.EpisodicMemoryBO;
import com.shiyu.ai.memory.port.repository.EpisodicMemoryRepository;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 情景记忆 — DB 持久化
 *
 * <p>存储 Agent 任务执行经历，包括任务类型、描述、结果等。
 * 工作记忆（WorkingMemory）中的变量可在 Agent 执行结束时
 * 由 {@code ConsolidationPipeline} 写入此 Store。</p>
 */
public class EpisodicMemoryStore implements MemoryStore {

    private final EpisodicMemoryRepository repository;

    public EpisodicMemoryStore(EpisodicMemoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Memory memory) {
        EpisodicMemoryBO bo = new EpisodicMemoryBO();
        bo.setExecutionId(memory.getMemoryId() != null ? memory.getMemoryId() : UUID.randomUUID().toString());
        bo.setAgentId(memory.getAgentId());
        bo.setUserId(memory.getUserId());
        bo.setSessionId(memory.getSessionId());
        bo.setTaskType(memory.getCategory());
        bo.setTaskDescription(memory.getContent());
        bo.setStatus(com.shiyu.ai.memory.domain.enums.EpisodicMemoryStatus.SUCCESS.getCode());
        bo.setCreateTime(LocalDateTime.now());
        repository.insert(bo);
    }

    @Override
    public void saveBatch(List<Memory> memories) {
        memories.forEach(this::save);
    }

    @Override
    public List<Memory> query(MemoryQuery query) {
        List<EpisodicMemoryBO> results;
        if (query.getAgentId() != null) {
            results = repository.selectByAgentId(query.getAgentId(), query.getTopK());
        } else if (query.getUserId() != null) {
            results = repository.selectByUserId(query.getUserId(), query.getTopK());
        } else {
            return List.of();
        }
        return results.stream().map(this::toMemory).collect(Collectors.toList());
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
    }

    @Override
    public long count(MemoryQuery query) {
        return 0;
    }

    private Memory toMemory(EpisodicMemoryBO bo) {
        Memory mem = new Memory(MemoryType.EPISODIC, bo.getSessionId(), "system", bo.getTaskDescription());
        mem.setMemoryId(bo.getExecutionId());
        mem.setUserId(bo.getUserId());
        mem.setAgentId(bo.getAgentId());
        mem.setCategory(bo.getTaskType());
        mem.setMemoryKey(bo.getExecutionId());
        mem.setCreatedAt(bo.getCreateTime());
        if (bo.getResultSummary() != null) {
            mem.getMetadata().put("resultSummary", bo.getResultSummary());
        }
        if (bo.getStatus() != null) {
            mem.getMetadata().put("status", bo.getStatus());
        }
        if (bo.getDurationMs() != null) {
            mem.getMetadata().put("durationMs", bo.getDurationMs());
        }
        return mem;
    }
}
