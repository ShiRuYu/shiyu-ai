package com.shiyu.ai.agent.biz.agent.service.impl;

import com.shiyu.ai.agent.biz.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.biz.agent.domain.AgentVersion;
import com.shiyu.ai.agent.biz.agent.service.AgentService;
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

/**
 * Agent Service 实现类
 * 提供 Agent 定义管理、版本控制和执行能力
 */
@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    /**
     * Agent 定义存储（内存缓存）
     * key: agentId, value: AgentDefinition
     */
    private final Map<String, AgentDefinition> agentDefinitions = new ConcurrentHashMap<>();

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
            log.warn("Agent 不存在：agentId={}", agentId);
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
        } else {
            log.warn("Agent 不存在，注销失败：agentId={}", agentId);
            return false;
        }
    }

    @Override
    public Map<String, Object> execute(String agentId, Map<String, Object> input) throws Exception {
        // 使用当前版本执行
        return execute(agentId, null, input);
    }

    @Override
    public Map<String, Object> execute(String agentId, String version, Map<String, Object> input) throws Exception {
        log.info("开始执行 Agent：agentId={}, version={}, inputSize={}",
                agentId, version, input != null ? input.size() : 0);

        // 1. 从 Agent 定义中获取版本
        AgentDefinition definition = getAgent(agentId);
        if (definition == null) {
            throw new IllegalStateException("Agent 不存在：" + agentId);
        }

        AgentVersion agentVersion = definition.getVersion(version);
        if (agentVersion == null) {
            throw new IllegalStateException("Agent 版本不存在：" +
                    (version != null ? version : definition.getCurrentVersion()));
        }

        log.info("获取到 Agent 版本：agentId={}, version={}, compiled={}",
                agentId, agentVersion.getVersionNumber(), agentVersion.isCompiled());

        // 2. 通过 Graph 的 execute 方法执行
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

        // 1. 从 Agent 定义中获取版本
        AgentDefinition definition = getAgent(agentId);
        if (definition == null) {
            throw new IllegalStateException("Agent 不存在：" + agentId);
        }

        AgentVersion agentVersion = definition.getVersion(version);
        if (agentVersion == null) {
            throw new IllegalStateException("Agent 版本不存在：" +
                    (version != null ? version : definition.getCurrentVersion()));
        }

        log.info("获取到 Agent 版本：agentId={}, version={}, compiled={}",
                agentId, agentVersion.getVersionNumber(), agentVersion.isCompiled());

        // 2. 通过 Graph 的 executeStream 方法获取流式数据，过滤 StreamingOutput 逐 token 输出
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

        AgentDefinition definition = getAgent(agentId);
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
}
