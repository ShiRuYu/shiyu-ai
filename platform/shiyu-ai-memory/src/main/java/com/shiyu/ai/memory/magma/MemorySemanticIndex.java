package com.shiyu.ai.memory.magma;

import java.util.List;

public interface MemorySemanticIndex {
    void upsert(MemoryEvent event);
    List<MemoryPath> search(MemoryQuery query, int limit);
    void delete(String eventId);
    void rebuild(String namespace);
}
