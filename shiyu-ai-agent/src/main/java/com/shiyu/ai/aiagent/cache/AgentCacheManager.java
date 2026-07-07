package com.shiyu.ai.aiagent.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.aiagent.AgentDefinition;
import com.shiyu.ai.dal.repository.agent.AgentAdminRepository;
import com.shiyu.ai.aiagent.bo.AgentDefBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AgentCacheManager {

    private final Cache<String, AgentDefinition> cache;

    private final AgentAdminRepository agentAdminRepository;

    private final AgentLoader agentLoader;

    public AgentCacheManager(AgentAdminRepository agentAdminRepository, AgentLoader agentLoader) {
        this.agentAdminRepository = agentAdminRepository;
        this.agentLoader = agentLoader;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    private String key(Long userId, String agentId) {
        return (userId != null ? userId : 0L) + ":" + agentId;
    }

    public AgentDefinition get(Long userId, String agentId) {
        return cache.getIfPresent(key(userId, agentId));
    }

    public void put(Long userId, String agentId, AgentDefinition agent) {
        cache.put(key(userId, agentId), agent);
        log.debug("缓存已写入: userId={}, agentId={}", userId, agentId);
    }

    public void put(AgentDefinition agent) {
        cache.put(key(0L, agent.getAgentId()), agent);
        log.debug("缓存已写入: agentId={}", agent.getAgentId());
    }

    public AgentDefinition getOrLoad(Long userId, String agentId, AgentLoader loader) {
        String k = key(userId, agentId);
        AgentDefinition cached = cache.getIfPresent(k);
        if (cached != null) {
            log.debug("缓存命中: userId={}, agentId={}", userId, agentId);
            return cached;
        }
        AgentDefinition loaded = loader.loadFromDb(userId, agentId);
        if (loaded != null) {
            cache.put(k, loaded);
            log.info("缓存加载: userId={}, agentId={}", userId, agentId);
        }
        return loaded;
    }

    public void evict(String agentId) {
        cache.asMap().keySet().removeIf(k -> k.endsWith(":" + agentId));
        log.info("缓存已清除: agentId={}", agentId);
    }

    public void evict(Long userId, String agentId) {
        cache.invalidate(key(userId, agentId));
        log.debug("缓存已清除: userId={}, agentId={}", userId, agentId);
    }

    public void evictAll() {
        cache.invalidateAll();
        log.info("全部缓存已清除");
    }

    public boolean containsKey(Long userId, String agentId) {
        return cache.getIfPresent(key(userId, agentId)) != null;
    }

    public List<AgentDefinition> listAll(Long userId) {
        List<AgentDefBO> activeDefs = agentAdminRepository.selectAllActive();
        return activeDefs.stream()
                .map(def -> {
                    String agentId = def.getAgentId();
                    AgentDefinition cached = cache.getIfPresent(key(userId, agentId));
                    if (cached != null) return cached;
                    return getOrLoad(userId, agentId, agentLoader);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public long estimatedSize() {
        cache.cleanUp();
        return cache.estimatedSize();
    }
}
