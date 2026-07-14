package com.shiyu.ai.memory.spi.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.dal.bo.memory.ConversationMessageBO;
import com.shiyu.ai.dal.repository.ConversationMessageRepository;
import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 短期记忆 — DB + Caffeine 缓存
 * 存储对话消息，支持滑动窗口压缩
 */
public class ShortTermMemoryStore implements MemoryStore {

    private static final int DEFAULT_WINDOW_SIZE = 10;

    private final ConversationMessageRepository repository;
    private final Cache<String, List<Memory>> messageCache;
    private final int windowSize;

    public ShortTermMemoryStore(ConversationMessageRepository repository) {
        this(repository, DEFAULT_WINDOW_SIZE);
    }

    public ShortTermMemoryStore(ConversationMessageRepository repository, int windowSize) {
        this.repository = repository;
        this.windowSize = windowSize;
        this.messageCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public void save(Memory memory) {
        ConversationMessageBO bo = new ConversationMessageBO();
        bo.setSessionId(memory.getSessionId());
        bo.setUserId(memory.getUserId());
        bo.setAgentId(memory.getAgentId());
        bo.setRole(memory.getRole());
        bo.setContent(memory.getContent());
        bo.setCreateTime(LocalDateTime.now());
        repository.insert(bo);
        // 使缓存失效
        messageCache.invalidate(memory.getSessionId());
    }

    @Override
    public void saveBatch(List<Memory> memories) {
        memories.forEach(this::save);
    }

    @Override
    public List<Memory> query(MemoryQuery query) {
        String sessionId = query.getSessionId();
        if (sessionId == null) return List.of();

        return messageCache.get(sessionId, k -> {
            List<ConversationMessageBO> messages = repository.selectRecentBySession(sessionId, query.getTopK());
            return messages.stream().map(msg -> {
                Memory mem = new Memory(MemoryType.SHORT_TERM, sessionId, msg.getRole(), msg.getContent());
                mem.setUserId(msg.getUserId());
                mem.setAgentId(msg.getAgentId());
                mem.setCreatedAt(msg.getCreateTime());
                return mem;
            }).collect(Collectors.toList());
        });
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
        repository.deleteBySession(sessionId);
        messageCache.invalidate(sessionId);
    }

    @Override
    public long count(MemoryQuery query) {
        if (query.getSessionId() == null) return 0;
        return repository.countBySession(query.getSessionId());
    }

    /**
     * 获取滑动窗口内的消息数
     */
    public int getWindowSize() {
        return windowSize;
    }
}
