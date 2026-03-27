package com.shiyu.ai.agent.node;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.state.AgentState;

import java.util.Map;

/**
 * 基础节点抽象类
 * 所有节点的父类，提供统一的执行框架
 * 
 * 执行流程:
 * 1. beforeExecute - 执行前处理
 * 2. processParameters - 参数处理
 * 3. doExecute - 执行核心逻辑
 * 4. afterExecute - 执行后处理
 */
@Setter
@Getter
@Slf4j
public abstract class BaseNode implements NodeAction<AgentState> {

    protected NodeConfig config;
    
    /**
     * 构造函数
     */
    public BaseNode() {
        this.config = NodeConfig.builder().build();
    }
    
    /**
     * 构造函数
     * @param config 节点配置
     */
    public BaseNode(NodeConfig config) {
        this.config = config;
    }
    
    public Map<String, Object> apply(AgentState state) throws Exception {
        try {
            // 1. 执行前处理
            beforeExecute(state);
            
            // 2. 参数处理
            NodeInput input = processParameters(state);
            
            // 3. 执行核心逻辑
            NodeOutput output = doExecute(input);
            
            // 4. 执行后处理
            afterExecute(state, output);
            
            // 返回结果 Map
            return output.toMap();
            
        } catch (Exception e) {
            log.error("节点执行失败：{}", config.getNodeName(), e);
            throw e; // 直接抛出异常，由框架处理
        }
    }
    
    /**
     * 执行前处理
     * @param state 当前状态
     */
    protected void beforeExecute(AgentState state) {
        log.info("开始执行节点：{}", config.getNodeName());
        
        // 记录日志
        if ("DEBUG".equalsIgnoreCase(config.getLogLevel())) {
            log.debug("节点配置：{}", config);
        }
    }
    
    /**
     * 参数处理
     * @param state 当前状态
     * @return NodeInput 处理后的输入参数
     */
    protected NodeInput processParameters(AgentState state) {
        log.debug("处理节点参数：{}", config.getNodeName());
        
        return NodeInput.fromMap(state.data());
    }
    
    /**
     * 执行核心逻辑 (由子类实现)
     * @param input 处理后的输入参数
     * @return NodeOutput 执行结果
     * @throws Exception 执行异常
     */
    protected abstract NodeOutput doExecute(NodeInput input) throws Exception;
    
    /**
     * 执行后处理
     * @param state 当前状态
     * @param output 执行结果
     */
    protected void afterExecute(AgentState state, NodeOutput output) {
        log.info("节点执行完成：{}", config.getNodeName());
    }
    
    /**
     * 异常处理
     * @param state 当前状态
     * @param e 异常
     * @return Map<String, Object>
     */
    protected Map<String, Object> handleException(AgentState state, Exception e) {
        String errorStrategy = config.getErrorStrategy();
        
        switch (errorStrategy) {
            case "IGNORE":
                log.warn("忽略异常，继续执行：{}", e.getMessage());
                return java.util.Collections.emptyMap();
                
            case "DEFAULT":
                log.warn("使用默认值处理异常：{}", e.getMessage());
                return createDefaultResult();
                
            case "THROW":
            default:
                log.error("抛出异常：{}", e.getMessage(), e);
                throw new RuntimeException("节点执行失败：" + config.getNodeName(), e);
        }
    }
    
    /**
     * 创建默认结果
     * @return Map<String, Object>
     */
    protected Map<String, Object> createDefaultResult() {
        return java.util.Map.of(
            "error", "使用默认值处理",
            "status", "DEFAULT_APPLIED"
        );
    }

}
