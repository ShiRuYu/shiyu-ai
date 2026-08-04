package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.cache.AgentLoader;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AgentService 实现 — 仅提供定义管理，执行统一走 AgentRuntime。
 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private final AgentCacheManager cacheManager;
    private final AgentLoader agentLoader;
    private final AgentAdminRepository agentAdminRepository;

    public AgentServiceImpl(AgentCacheManager cacheManager,
                            AgentLoader agentLoader,
                            AgentAdminRepository agentAdminRepository) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.agentAdminRepository = agentAdminRepository;
    }

    @Override
    public void registerAgent(AgentDefinition agentDefinition) {
        if (agentDefinition == null) {
            throw new IllegalArgumentException("AgentDefinition 不能为空");
        }
        String agentId = agentDefinition.getAgentId();
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        log.info("注册 Agent：agentId={}, name={}", agentId, agentDefinition.getName());
        cacheManager.put(agentDefinition);
        log.info("Agent 注册成功（缓存）：agentId={}", agentId);
    }

    @Override
    public AgentDefinition getAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        Long uid = currentUserId();
        AgentDefinition definition = cacheManager.get(uid, agentId);
        if (definition == null) {
            definition = cacheManager.getOrLoad(uid, agentId, agentLoader);
        }
        return definition;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unregisterAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        agentAdminRepository.deleteByAgentId(agentId);
        cacheManager.evict(agentId);
        log.info("Agent 已注销（DB 软删除 + 缓存清理）：agentId={}", agentId);
        return true;
    }

    @Override
    public boolean switchVersion(String agentId, String version) {
        log.info("切换 Agent 版本：agentId={}, targetVersion={}", agentId, version);
        AgentDefinition definition = getOrLoadAgent(agentId);
        if (definition == null) {
            log.warn("Agent 不存在，切换失败：agentId={}", agentId);
            return false;
        }
        if (definition.getVersion(version) == null) {
            log.warn("版本切换失败，版本不存在：agentId={}, version={}", agentId, version);
            return false;
        }
        definition.setCurrentVersion(version);
        log.info("版本切换成功：agentId={}, version={}", agentId, version);
        return true;
    }

    @Override
    public List<AgentDefinition> listAgents() {
        List<AgentDefinition> agents = cacheManager.listAll(currentUserId());
        log.debug("已注册 Agent 数量：{}", agents.size());
        return agents;
    }

    @Override
    public void evictRuntimeCache(String agentId) {
        cacheManager.evict(agentId);
        log.info("运行时缓存已清除: agentId={}", agentId);
    }

    // ==================== 内部方法 ====================

    private AgentDefinition getOrLoadAgent(String agentId) {
        Long uid = currentUserId();
        AgentDefinition definition = cacheManager.get(uid, agentId);
        if (definition != null) return definition;
        definition = cacheManager.getOrLoad(uid, agentId, agentLoader);
        if (definition == null) {
            throw new IllegalStateException("Agent 不存在：" + agentId);
        }
        return definition;
    }

    private static Long currentUserId() {
        try {
            return com.shiyu.ai.common.core.domain.UserContextHolder.getUserId();
        } catch (Exception e) {
            return 0L;
        }
    }
}
