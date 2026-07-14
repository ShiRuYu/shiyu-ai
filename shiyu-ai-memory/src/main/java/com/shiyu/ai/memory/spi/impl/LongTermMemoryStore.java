package com.shiyu.ai.memory.spi.impl;

import com.shiyu.ai.dal.bo.memory.LongTermMemoryBO;
import com.shiyu.ai.dal.repository.LongTermMemoryRepository;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 长期记忆 — DB 持久化
 * 支持关键词搜索和重要性排序
 */
public class LongTermMemoryStore implements MemoryStore {

    private final LongTermMemoryRepository repository;

    public LongTermMemoryStore(LongTermMemoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Memory memory) {
        LongTermMemoryBO bo = new LongTermMemoryBO();
        bo.setUserId(memory.getUserId());
        bo.setAgentId(memory.getAgentId());
        bo.setCategory(memory.getCategory() != null ? memory.getCategory() : "general");
        bo.setMemoryKey(memory.getMemoryKey() != null ? memory.getMemoryKey() : UUID.randomUUID().toString());
        bo.setContent(memory.getContent());
        bo.setImportance(memory.getImportance());
        bo.setSource(memory.getSource());
        bo.setCreateTime(LocalDateTime.now());
        bo.setUpdateTime(LocalDateTime.now());
        repository.insert(bo);
    }

    @Override
    public void saveBatch(List<Memory> memories) {
        memories.forEach(this::save);
    }

    @Override
    public List<Memory> query(MemoryQuery query) {
        List<LongTermMemoryBO> results;
        String keyword = query.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            results = repository.searchByKeyword(keyword, query.getUserId(), query.getAgentId(), query.getTopK());
        } else {
            results = repository.selectTopByImportance(query.getUserId(), query.getAgentId(), query.getTopK());
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

    private Memory toMemory(LongTermMemoryBO bo) {
        Memory mem = new Memory(MemoryType.LONG_TERM, null, "system", bo.getContent());
        mem.setUserId(bo.getUserId());
        mem.setAgentId(bo.getAgentId());
        mem.setCategory(bo.getCategory());
        mem.setMemoryKey(bo.getMemoryKey());
        mem.setImportance(bo.getImportance() != null ? bo.getImportance() : 0.5);
        mem.setSource(bo.getSource());
        mem.setCreatedAt(bo.getCreateTime());
        return mem;
    }
}
