package com.shiyu.ai.agent.biz.agent.cache;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.shiyu.ai.agent.biz.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.biz.agent.repository.AgentAdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AgentCacheManager {

    private final Table<Long, String, AgentDefinition> cache = HashBasedTable.create();

    public AgentDefinition get(Long userId, String agentId) {
        synchronized (cache) {
            return cache.get(userId, agentId);
        }
    }

    public void put(Long userId, String agentId, AgentDefinition agent) {
        synchronized (cache) {
            cache.put(userId, agentId, agent);
            log.debug("缓存已写入: userId={}, agentId={}", userId, agentId);
        }
    }

    public AgentDefinition getOrLoad(Long userId, String agentId, AgentLoader loader) {
        synchronized (cache) {
            AgentDefinition cached = cache.get(userId, agentId);
            if (cached != null) {
                log.debug("缓存命中: userId={}, agentId={}", userId, agentId);
                return cached;
            }
        }
        AgentDefinition loaded = loader.loadFromDb(userId, agentId);
        if (loaded != null) {
            synchronized (cache) {
                cache.put(userId, agentId, loaded);
            }
            log.info("缓存加载: userId={}, agentId={}", userId, agentId);
        }
        return loaded;
    }

    public void evictColumn(String agentId) {
        synchronized (cache) {
            cache.column(agentId).clear();
            log.info("缓存列已清除: agentId={}", agentId);
        }
    }

    public void evict(Long userId, String agentId) {
        synchronized (cache) {
            cache.remove(userId, agentId);
            log.debug("缓存已清除: userId={}, agentId={}", userId, agentId);
        }
    }

    public void evictAll() {
        synchronized (cache) {
            cache.clear();
            log.info("全部缓存已清除");
        }
    }
}
