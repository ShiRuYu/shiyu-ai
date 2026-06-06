package com.shiyu.ai.agent.langgraph4j.node.agent;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Agent 调用节点配置类
 * 用于调用其他已注册的 Agent 执行子任务
 *
 * @author shiyu-ai
 * @date 2026-06-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentCallConfig extends NodeConfig {
    
    /**
     * 目标 Agent ID（必填）
     */
    private String targetAgentId;
    
    /**
     * 目标 Agent 版本（可选，为空则使用当前版本）
     */
    private String targetVersion;
    
    /**
     * 输入参数映射配置
     * key: 当前 Agent 的参数名
     * value: 目标 Agent 的参数名
     * 用于将当前 Agent 的参数映射给目标 Agent
     */
    private java.util.Map<String, String> inputMapping;
    
    /**
     * 输出结果键名（默认 "agentResult"）
     * 目标 Agent 的执行结果会存储在此键下
     */
    private String outputKey = "agentResult";
    
    /**
     * 超时时间（毫秒，默认 30000）
     */
    private Long agentTimeout = 30000L;
    
    /**
     * 是否异步执行（默认 false）
     */
    private Boolean async = false;
}
