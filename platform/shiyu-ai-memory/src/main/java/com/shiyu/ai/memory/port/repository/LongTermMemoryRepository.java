package com.shiyu.ai.memory.port.repository;

import com.shiyu.ai.memory.domain.model.LongTermMemoryBO;
import java.util.List;

public interface LongTermMemoryRepository {
    void insert(LongTermMemoryBO bo);
    List<LongTermMemoryBO> searchByKeyword(String keyword, Long userId, String agentId, int topK);
    List<LongTermMemoryBO> searchByCategory(String category, Long userId, String agentId, int topK);
    List<LongTermMemoryBO> selectAllByUser(Long userId, String agentId);
    List<LongTermMemoryBO> selectTopByImportance(Long userId, String agentId, int topK);
    int update(LongTermMemoryBO memory);
    void deleteById(Long id);
}
