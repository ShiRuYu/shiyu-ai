package com.shiyu.ai.agent.implementation.service.impl;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.implementation.service.AgentService;
import com.shiyu.ai.kernel.context.ActorContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** AgentService 实现 — 仅提供定义管理，执行统一走 AgentRuntime。 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    /**
     * cacheManager 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentCacheManager cacheManager;
    /**
     * agentLoader 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentLoader agentLoader;
    /**
     * agentAdminRepository 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AgentAdminRepository agentAdminRepository;

    /**
     * {@code AgentServiceImpl} 创建并初始化当前类型实例。
     *
     * @param cacheManager 参数值，用于执行当前操作。
     * @param agentLoader 参数值，用于执行当前操作。
     * @param agentAdminRepository 参数值，用于执行当前操作。
     */
    public AgentServiceImpl(
            AgentCacheManager cacheManager,
            AgentLoader agentLoader,
            AgentAdminRepository agentAdminRepository) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.agentAdminRepository = agentAdminRepository;
    }

    /**
     * {@code registerAgent} 写入或更新当前模块中的业务数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentDefinition 参数值，用于执行当前操作。
     */
    @Override
    public void registerAgent(ActorContext actor, AgentDefinition agentDefinition) {
        if (agentDefinition == null) {
            throw new IllegalArgumentException("AgentDefinition 不能为空");
        }
        String agentId = agentDefinition.getAgentId();
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        log.info(
                "注册 Agent：agentIdPresent={}, namePresent={}",
                agentId != null,
                agentDefinition.getName() != null);
        cacheManager.put(actor, agentId, agentDefinition);
        log.info("Agent 注册成功（缓存）：agentIdPresent={}", agentId != null);
    }

    /**
     * {@code registerSystemAgent} 写入或更新当前模块中的业务数据。
     *
     * @param agentDefinition 参数值，用于执行当前操作。
     */
    @Override
    public void registerSystemAgent(AgentDefinition agentDefinition) {
        if (agentDefinition == null
                || agentDefinition.getAgentId() == null
                || agentDefinition.getAgentId().isBlank()) {
            throw new IllegalArgumentException("AgentDefinition 和 AgentId 不能为空");
        }
        cacheManager.putSystem(agentDefinition);
    }

    /**
     * {@code getAgent} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public AgentDefinition getAgent(ActorContext actor, String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        AgentDefinition definition = cacheManager.get(actor, agentId);
        if (definition == null) {
            definition = cacheManager.getOrLoad(actor, agentId, agentLoader);
        }
        return definition;
    }

    /**
     * {@code unregisterAgent} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unregisterAgent(ActorContext actor, String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        agentAdminRepository.deleteByAgentId(actor.tenantId(), agentId);
        cacheManager.evict(actor, agentId);
        log.info("Agent 已注销（DB 软删除 + 缓存清理）：agentIdPresent={}", agentId != null);
        return true;
    }

    /**
     * {@code switchVersion} 执行当前类型定义的业务操作。
     *
     * @param actor 参数值，用于执行当前操作。
     * @param agentId 参数值，用于执行当前操作。
     * @param version 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean switchVersion(ActorContext actor, String agentId, String version) {
        log.info(
                "切换 Agent 版本：agentIdPresent={}, targetVersionPresent={}",
                agentId != null,
                version != null);
        AgentDefinition definition = getOrLoadAgent(actor, agentId);
        if (definition == null) {
            log.warn("Agent 不存在，切换失败：agentIdPresent={}", agentId != null);
            return false;
        }
        if (definition.getVersion(version) == null) {
            log.warn(
                    "版本切换失败，版本不存在：agentIdPresent={}, versionPresent={}",
                    agentId != null,
                    version != null);
            return false;
        }
        definition.setCurrentVersion(version);
        log.info("版本切换成功：agentIdPresent={}, versionPresent={}", agentId != null, version != null);
        return true;
    }

    /**
     * {@code listAgents} 查询并返回当前操作所需的数据。
     *
     * @param actor 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public List<AgentDefinition> listAgents(ActorContext actor) {
        List<AgentDefinition> agents = cacheManager.listAll(actor);
        log.debug("已注册 Agent 数量：{}", agents.size());
        return agents;
    }

    /**
     * {@code evictRuntimeCache} 执行当前类型定义的业务操作。
     *
     * @param agentId 参数值，用于执行当前操作。
     */
    @Override
    public void evictRuntimeCache(String agentId) {
        cacheManager.evict(agentId);
        log.info("运行时缓存已清除: agentIdPresent={}", agentId != null);
    }

    // ==================== 内部方法 ====================

    private AgentDefinition getOrLoadAgent(ActorContext actor, String agentId) {
        AgentDefinition definition = cacheManager.get(actor, agentId);
        if (definition != null) return definition;
        definition = cacheManager.getOrLoad(actor, agentId, agentLoader);
        return definition;
    }
}
