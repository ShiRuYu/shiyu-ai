package com.shiyu.ai.memory.spi;

import java.util.List;

/**
 * 记忆存储 SPI 接口
 */
public interface MemoryStore {

    void save(Memory memory);

    void saveBatch(List<Memory> memories);

    List<Memory> query(MemoryQuery query);

    Memory queryById(String memoryId);

    void delete(String memoryId);

    void deleteBySession(String sessionId);

    long count(MemoryQuery query);
}
