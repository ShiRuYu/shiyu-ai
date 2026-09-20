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
 * 管理 智能体 Cache 相关的运行时状态、注册信息或临时数据。
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
     * 执行 智能体 Cache 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentAdminRepository 用于完成本次业务处理的 agentAdminRepository 参数。
     * @param agentLoader 用于完成本次业务处理的 agentLoader 参数。
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
     * 查询 智能体 Cache 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @return 返回 智能体 Cache 相关操作生成的结果数据。
     */
    public AgentDefinition get(ActorContext actor, String agentId) {
        AgentDefinition scoped = cache.getIfPresent(key(actor, agentId));
        if (scoped != null) {
            return scoped;
        }
        return cache.getIfPresent(systemKey(agentId));
    }

    /**
     * 执行 智能体 Cache 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param agent 用于完成本次业务处理的 agent 参数。
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
     * 执行 智能体 Cache 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agent 用于完成本次业务处理的 agent 参数。
     */
    public void putSystem(AgentDefinition agent) {
        cache.put(systemKey(agent.getAgentId()), agent);
        log.debug("系统 Agent 缓存已写入: agentIdPresent={}", agent.getAgentId() != null);
    }

    /**
     * 查询 智能体 Cache 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param loader 用于完成本次业务处理的 loader 参数。
     * @return 返回 智能体 Cache 相关操作生成的结果数据。
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
     * 执行 智能体 Cache 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentId 用于定位agent的标识。
     */
    public void evict(String agentId) {
        cache.asMap().keySet().removeIf(k -> k.endsWith(":" + agentId));
        log.info("缓存已清除: agentIdPresent={}", agentId != null);
    }

    /**
     * 执行 智能体 Cache 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
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
     * 执行 智能体 Cache 相关业务操作，并维护必要的状态和协作关系。
     */
    public void evictAll() {
        cache.invalidateAll();
        log.info("全部缓存已清除");
    }

    /**
     * 执行 智能体 Cache 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @return 返回本次条件判断是否成立。
     */
    public boolean containsKey(ActorContext actor, String agentId) {
        return cache.getIfPresent(key(actor, agentId)) != null;
    }

    /**
     * 查询 智能体 Cache 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
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
     * 执行 智能体 Cache 相关业务数据，并返回处理结果。
     *
     * @return 返回 智能体 Cache 相关操作生成的结果数据。
     */
    public long estimatedSize() {
        cache.cleanUp();
        return cache.estimatedSize();
    }
}
