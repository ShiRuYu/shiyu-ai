package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.domain.AgentVersion;
import com.shiyu.ai.agent.service.AgentService;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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
        
        // 2. 从版本中拿到 graph 进行编译
        CompiledGraph<AgentState> compiledGraph;
        try {
            compiledGraph = agentVersion.getOrCompileGraph();
        } catch (GraphStateException e) {
            log.error("Graph 编译失败：agentId={}, version={}", agentId, 
                    agentVersion.getVersionNumber(), e);
            throw new Exception("Graph 编译失败：" + e.getMessage(), e);
        }
        
        // 3. 执行编译后的 graph
        try {
            log.info("开始执行 CompiledGraph: agentId={}, version={}", agentId, 
                    agentVersion.getVersionNumber());
            
            // 执行图并获取结果 (返回 Optional<AgentState>)
            var resultOptional = compiledGraph.invoke(input);
            
            // 从 Optional 中获取结果
            var result = resultOptional.orElseThrow(() -> 
                new IllegalStateException("Agent 执行返回空结果：" + agentId));
            
            log.info("Agent 执行完成：agentId={}, version={}", agentId, 
                    agentVersion.getVersionNumber());
            
            // 返回结果数据
            return result.data();
            
        } catch (Exception e) {
            log.error("Agent 执行失败：agentId={}, version={}", agentId, 
                    agentVersion.getVersionNumber(), e);
            throw new Exception("Agent 执行失败：" + e.getMessage(), e);
        }
    }
    
    @Override
    public Flux<Map<String, Object>> executeStream(String agentId, Map<String, Object> input) throws Exception {
        log.info("开始流式执行 Agent：agentId={}", agentId);
        
        return Flux.defer(() -> {
            try {
                Map<String, Object> result = execute(agentId, input);
                return Flux.just(result);
            } catch (Exception e) {
                log.error("流式执行失败：agentId={}", agentId, e);
                return Flux.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
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
