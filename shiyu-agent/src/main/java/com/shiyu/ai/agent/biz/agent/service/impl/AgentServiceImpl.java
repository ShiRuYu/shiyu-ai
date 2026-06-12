package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.cache.AgentCacheManager;
import com.shiyu.ai.agent.biz.agent.cache.AgentLoader;
import com.shiyu.ai.agent.biz.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.biz.agent.domain.AgentVersion;
import com.shiyu.ai.agent.biz.agent.service.AgentService;
import com.shiyu.ai.common.core.domain.LoginContextHolder;
import com.shiyu.ai.common.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.streaming.StreamingOutput;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    private final Map<String, AgentDefinition> agentDefinitions = new ConcurrentHashMap<>();

    private final AgentCacheManager cacheManager;

    private final AgentLoader agentLoader;

    public AgentServiceImpl(AgentCacheManager cacheManager, AgentLoader agentLoader) {
        this.cacheManager = cacheManager;
        this.agentLoader = agentLoader;
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
        agentDefinitions.put(agentId, agentDefinition);
        log.info("Agent 注册成功：agentId={}", agentId);
    }

    @Override
    public AgentDefinition getAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        AgentDefinition definition = agentDefinitions.get(agentId);
        if (definition == null) {
            definition = loadFromCache(agentId);
        }
        return definition;
    }

    @Override
    public boolean unregisterAgent(String agentId) {
        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException("AgentId 不能为空");
        }
        AgentDefinition removed = agentDefinitions.remove(agentId);
        if (removed != null) {
            log.info("Agent 已注销：agentId={}", agentId);
            return true;
        }
        log.warn("Agent 不存在，注销失败：agentId={}", agentId);
        return false;
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
                agentId, agentVersion.getVersionNumber(), agentVersion.isCompiled());

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
                agentId, agentVersion.getVersionNumber(), agentVersion.isCompiled());

        return agentVersion.getGraph().executeStream(input)
                .filter(output -> output instanceof StreamingOutput<AgentState>)
                .map(output -> ((StreamingOutput<AgentState>) output).chunk())
                .filter(StringUtils::isNotEmpty)
                .map(chunk -> Map.<String, Object>of("content", chunk))
                .doOnNext(chunk -> log.trace("Graph 流式输出 token：agentId={}, content={}", agentId, chunk.get("content")))
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
        boolean success = definition.setCurrentVersion(version);
        if (success) {
            log.info("版本切换成功：agentId={}, version={}", agentId, version);
        } else {
            log.warn("版本切换失败：agentId={}, version={}", agentId, version);
        }
        return success;
    }

    @Override
    public List<AgentDefinition> listAgents() {
        List<AgentDefinition> agents = new ArrayList<>(agentDefinitions.values());
        log.debug("已注册 Agent 数量：{}", agents.size());
        return agents;
    }

    @Override
    public void evictRuntimeCache(String agentId) {
        agentDefinitions.remove(agentId);
        cacheManager.evictColumn(agentId);
        log.info("运行时缓存已清除: agentId={}", agentId);
    }

    private AgentDefinition getOrLoadAgent(String agentId) {
        AgentDefinition definition = agentDefinitions.get(agentId);
        if (definition != null) return definition;

        definition = loadFromCache(agentId);
        if (definition == null) {
            throw new IllegalStateException("Agent 不存在：" + agentId);
        }
        return definition;
    }

    private AgentDefinition loadFromCache(String agentId) {
        Long userId = getCurrentUserId();
        AgentDefinition definition = cacheManager.getOrLoad(userId, agentId, agentLoader);
        if (definition != null) {
            agentDefinitions.put(agentId, definition);
        }
        return definition;
    }

    private Long getCurrentUserId() {
        try {
            Long userId = LoginContextHolder.getUserId();
            return userId != null ? userId : 0L;
        } catch (Exception e) {
            log.debug("无法获取当前用户ID，使用默认值");
            return 0L;
        }
    }
}
