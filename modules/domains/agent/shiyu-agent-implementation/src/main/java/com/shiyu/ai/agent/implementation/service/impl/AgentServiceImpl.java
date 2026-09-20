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

/**
 * 提供 智能体 的查询、创建、更新及调用服务，协调业务变更和领域协作。
 */
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
     * 执行 智能体 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param cacheManager 用于完成本次业务处理的 cacheManager 参数。
     * @param agentLoader 用于完成本次业务处理的 agentLoader 参数。
     * @param agentAdminRepository 用于完成本次业务处理的 agentAdminRepository 参数。
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
     * 创建或保存 智能体 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentDefinition 用于完成本次业务处理的 agentDefinition 参数。
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
     * 创建或保存 智能体 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentDefinition 用于完成本次业务处理的 agentDefinition 参数。
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
     * 查询 智能体 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @return 返回 智能体 相关操作生成的结果数据。
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
     * 执行 智能体 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param class 用于完成本次业务处理的 class 参数。
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
     * 执行 智能体 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @param agentId 用于定位agent的标识。
     * @param version 用于完成本次业务处理的 version 参数。
     * @return 返回本次条件判断是否成立。
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
     * 查询 智能体 相关业务数据，并返回处理结果。
     *
     * @param actor 当前操作主体上下文，用于确定租户、用户和访问权限。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<AgentDefinition> listAgents(ActorContext actor) {
        List<AgentDefinition> agents = cacheManager.listAll(actor);
        log.debug("已注册 Agent 数量：{}", agents.size());
        return agents;
    }

    /**
     * 执行 智能体 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param agentId 用于定位agent的标识。
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
