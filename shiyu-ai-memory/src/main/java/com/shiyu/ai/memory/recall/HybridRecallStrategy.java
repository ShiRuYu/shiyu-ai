package com.shiyu.ai.memory.recall;

import com.shiyu.ai.memory.spi.Memory;
import com.shiyu.ai.memory.spi.MemoryQuery;
import com.shiyu.ai.memory.spi.MemoryStore;
import com.shiyu.ai.memory.spi.MemoryType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 混合召回策略
 * 从多个 MemoryStore 中召回，按重要性排序去重
 */
public class HybridRecallStrategy implements MemoryRecallStrategy {

    private final List<MemoryStore> stores;

    public HybridRecallStrategy(List<MemoryStore> stores) {
        this.stores = stores;
    }

    @Override
    public List<Memory> recall(MemoryQuery query) {
        Set<String> seen = new HashSet<>();
        List<Memory> results = new ArrayList<>();

        for (MemoryStore store : stores) {
            List<Memory> batch = store.query(query);
            for (Memory mem : batch) {
                String key = mem.getSessionId() + ":" + mem.getContent();
                if (seen.add(key)) {
                    results.add(mem);
                }
            }
        }

        // 按重要性排序
        results.sort((a, b) -> Double.compare(b.getImportance(), a.getImportance()));

        // 截取 topK
        return results.stream().limit(query.getTopK()).collect(Collectors.toList());
    }
}
