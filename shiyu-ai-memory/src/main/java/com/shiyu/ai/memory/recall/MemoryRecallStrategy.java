package com.shiyu.ai.memory.recall;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;

import java.util.List;

/**
 * 记忆召回策略接口
 */
public interface MemoryRecallStrategy {

    List<Memory> recall(MemoryQuery query);
}
