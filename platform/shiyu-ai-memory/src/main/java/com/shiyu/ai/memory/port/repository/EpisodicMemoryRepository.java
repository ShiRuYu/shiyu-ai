package com.shiyu.ai.memory.port.repository;

import com.shiyu.ai.memory.domain.model.EpisodicMemoryBO;
import java.util.List;

public interface EpisodicMemoryRepository {
    void insert(EpisodicMemoryBO bo);
    List<EpisodicMemoryBO> selectByAgentId(String agentId, int limit);
    List<EpisodicMemoryBO> selectByUserId(Long userId, int limit);
}
