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

/**
 * {@code AgentCacheManager} 承载智能体模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Slf4j
@Component
public class AgentCacheManager {

    /**
     * cache 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final Cache<String, AgentDefinition> cache;

    /**
     * agentAdminRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentAdminRepository agentAdminRepository;

    /**
     * agentLoader 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentLoader agentLoader;

    /**
     * {@code AgentCacheManager} 创建并初始化当前类型实例。
     *
     * @param agentAdminRepository 参数值，用于执行当前操作。
     * @param agentLoader 参数值，用于执行当前操作。
     */
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

    /**
     * {@code get} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public AgentDefinition get(ActorContext actor, String agentId) {
        AgentDefinition scoped = cache.getIfPresent(key(actor, agentId));
        if (scoped != null) {
            return scoped;
        }
        return cache.getIfPresent(systemKey(agentId));
    }

    /**
     * {@code put} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param agent 参数值，用于执行当前操作。
     */
    public void put(ActorContext actor, String agentId, AgentDefinition agent) {
        cache.put(key(actor, agentId), agent);
        log.debug(
                "租户 Agent 缓存已写入: tenantSelected={}, userPresent={}, agentIdPresent={}",
                actor.tenantId() != null,
                actor.userId() != null,
                agentId != null);
    }

    /**
     * {@code putSystem} 执行当前类型定义的业务操作。
     *
     * @param agent 参数值，用于执行当前操作。
     */
    public void putSystem(AgentDefinition agent) {
        cache.put(systemKey(agent.getAgentId()), agent);
        log.debug("系统 Agent 缓存已写入: agentIdPresent={}", agent.getAgentId() != null);
    }

    /**
     * {@code getOrLoad} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param loader 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code evict} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     */
    public void evict(String agentId) {
        cache.asMap().keySet().removeIf(k -> k.endsWith(":" + agentId));
        log.info("缓存已清除: agentIdPresent={}", agentId != null);
    }

    /**
     * {@code evict} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     */
    public void evict(ActorContext actor, String agentId) {
        cache.invalidate(key(actor, agentId));
        log.debug(
                "缓存已清除: tenantSelected={}, userPresent={}, agentIdPresent={}",
                actor.tenantId() != null,
                actor.userId() != null,
                agentId != null);
    }

    /**
     * {@code evictAll} 执行当前类型定义的业务操作。
     */
    public void evictAll() {
        cache.invalidateAll();
        log.info("全部缓存已清除");
    }

    /**
     * {@code containsKey} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean containsKey(ActorContext actor, String agentId) {
        return cache.getIfPresent(key(actor, agentId)) != null;
    }

    /**
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
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

    /**
     * {@code estimatedSize} 执行当前类型定义的业务操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public long estimatedSize() {
        cache.cleanUp();
        return cache.estimatedSize();
    }
}
