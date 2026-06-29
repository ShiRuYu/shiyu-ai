package com.shiyu.ai.aiagent.service.impl;

import com.shiyu.ai.aiagent.cache.AgentCacheManager;
import com.shiyu.ai.aiagent.cache.AgentLoader;
import com.shiyu.ai.dal.repository.AgentAdminRepository;
import com.shiyu.ai.aiagent.AgentDefinition;
import com.shiyu.ai.aiagent.AgentVersion;
import com.shiyu.ai.aiagent.service.AgentService;
import com.shiyu.ai.aiagent.node.NodeFields;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.streaming.StreamingOutput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private final AgentCacheManager cacheManager;

    private final AgentLoader agentLoader;

    private final AgentAdminRepository agentAdminRepository;

    public AgentServiceImpl(AgentCacheManager cacheManager, AgentLoader agentLoader, AgentAdminRepository agentAdminRepository, AgentAdminRepository agentAdminRepository1) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
        this.agentAdminRepository = agentAdminRepository1;
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
        log.info("Agent 注册成功：agentId={}", agentId);
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
        // 1. DB 软删除
        agentAdminRepository.deleteByAgentId(agentId);
        // 2. 清缓存
        cacheManager.evict(agentId);
        log.info("Agent 已注销（DB 软删除 + 缓存清理）：agentId={}", agentId);
        return true;
    }

    @Override
    public Map<String, Object> execute(String agentId, Map<String, Object> input) throws Exception {
        return execute(agentId, null, input);
    }

    @Override
    public Map<String, Object> execute(String agentId, String version, Map<String, Object> input) throws Exception {
        log.info("开始执行 Agent：agentId={}, version={}, inputSize={}",
                agentId, version, input != null ? input.size() : 0);

        AgentDefinition definition = getOrLoadAgent(agentId);

        AgentVersion agentVersion = definition.getVersion(version);
        if (agentVersion == null) {
            throw new IllegalStateException("Agent 版本不存在：" +
                    (version != null ? version : definition.getCurrentVersion()));
        }

        log.info("获取到 Agent 版本：agentId={}, version={}, compiled={}",
                agentId, agentVersion.getVersionNumber());

        try {
            return agentVersion.getGraph().execute(input);
        } catch (GraphStateException e) {
            log.error("Graph 执行失败：agentId={}, version={}", agentId,
                    agentVersion.getVersionNumber(), e);
            throw new Exception("Graph 执行失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Flux<Map<String, Object>> executeStream(String agentId, Map<String, Object> input) throws Exception {
        return executeStream(agentId, null, input);
    }

    @Override
    public Flux<Map<String, Object>> executeStream(String agentId, String version, Map<String, Object> input) throws Exception {
        log.info("开始流式执行 Agent：agentId={}, version={}, inputSize={}",
                agentId, version, input != null ? input.size() : 0);

        AgentDefinition definition = getOrLoadAgent(agentId);

        AgentVersion agentVersion = definition.getVersion(version);
        if (agentVersion == null) {
            throw new IllegalStateException("Agent 版本不存在：" +
                    (version != null ? version : definition.getCurrentVersion()));
        }

        log.info("获取到 Agent 版本：agentId={}, version={}, compiled={}",
                agentId, agentVersion.getVersionNumber());

        return agentVersion.getGraph().executeStream(input)
                .filter(output -> output instanceof StreamingOutput<AgentState>)
                .map(output -> ((StreamingOutput<AgentState>) output).chunk())
                .filter(StringUtils::isNotEmpty)
                .map(chunk -> Map.<String, Object>of(NodeFields.FieldKey.CONTENT.key(), chunk))
                .doOnNext(chunk -> log.trace("Graph 流式输出 token：agentId={}, content={}", agentId, chunk.get(NodeFields.FieldKey.CONTENT.key())))
                .doOnComplete(() -> log.info("Graph 流式执行完成：agentId={}", agentId))
                .doOnError(e -> log.error("Graph 流式执行错误：agentId={}, error={}", agentId, e.getMessage()));
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
            return LoginContextHolder.getUserId();
        } catch (Exception e) {
            return 0L;
        }
    }
}
