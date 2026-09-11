package com.shiyu.ai.agent.implementation.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.implementation.domain.model.AgentDefBO;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.kernel.context.ActorContext;

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
        this.cache =
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .recordStats()
                        .build();
    }

    private String key(ActorContext actor, String agentId) {
        Objects.requireNonNull(actor, "actor must not be null");
        return "tenant:"
                + actor.tenantId().value()
                + ":user:"
                + actor.userId().value()
                + ":"
                + agentId;
    }

    private String systemKey(String agentId) {
        return "system:" + agentId;
    }

    public AgentDefinition get(ActorContext actor, String agentId) {
        AgentDefinition scoped = cache.getIfPresent(key(actor, agentId));
        if (scoped != null) {
            return scoped;
        }
        return cache.getIfPresent(systemKey(agentId));
    }

    public void put(ActorContext actor, String agentId, AgentDefinition agent) {
        cache.put(key(actor, agentId), agent);
        log.debug(
                "租户 Agent 缓存已写入: tenantSelected={}, userPresent={}, agentIdPresent={}",
                actor.tenantId() != null,
                actor.userId() != null,
                agentId != null);
    }

    public void putSystem(AgentDefinition agent) {
        cache.put(systemKey(agent.getAgentId()), agent);
        log.debug("系统 Agent 缓存已写入: agentIdPresent={}", agent.getAgentId() != null);
    }

    public AgentDefinition getOrLoad(ActorContext actor, String agentId, AgentLoader loader) {
        String k = key(actor, agentId);
        AgentDefinition cached = cache.getIfPresent(k);
        if (cached != null) {
            log.debug(
                    "缓存命中: tenantSelected={}, userPresent={}, agentIdPresent={}",
                    actor.tenantId() != null,
                    actor.userId() != null,
                    agentId != null);
            return cached;
        }
        AgentDefinition system = cache.getIfPresent(systemKey(agentId));
        if (system != null) {
            return system;
        }
        AgentDefinition loaded = loader.loadFromDb(actor, agentId);
        if (loaded != null) {
            cache.put(k, loaded);
            log.info(
                    "缓存加载: tenantSelected={}, userPresent={}, agentIdPresent={}",
                    actor.tenantId() != null,
                    actor.userId() != null,
                    agentId != null);
        }
        return loaded;
    }

    public void evict(String agentId) {
        cache.asMap().keySet().removeIf(k -> k.endsWith(":" + agentId));
        log.info("缓存已清除: agentIdPresent={}", agentId != null);
    }

    public void evict(ActorContext actor, String agentId) {
        cache.invalidate(key(actor, agentId));
        log.debug(
                "缓存已清除: tenantSelected={}, userPresent={}, agentIdPresent={}",
                actor.tenantId() != null,
                actor.userId() != null,
                agentId != null);
    }

    public void evictAll() {
        cache.invalidateAll();
        log.info("全部缓存已清除");
    }

    public boolean containsKey(ActorContext actor, String agentId) {
        return cache.getIfPresent(key(actor, agentId)) != null;
    }

    public List<AgentDefinition> listAll(ActorContext actor) {
        List<AgentDefBO> activeDefs = agentAdminRepository.selectAllActive(actor.tenantId());
        return activeDefs.stream()
                .map(
                        def -> {
                            String agentId = def.getAgentId();
                            AgentDefinition cached = get(actor, agentId);
                            if (cached != null) return cached;
                            return getOrLoad(actor, agentId, agentLoader);
                        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public long estimatedSize() {
        cache.cleanUp();
        return cache.estimatedSize();
    }
}
