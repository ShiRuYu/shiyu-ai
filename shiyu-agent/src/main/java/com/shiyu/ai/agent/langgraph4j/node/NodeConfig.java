package com.shiyu.ai.agent.langgraph4j.node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 节点配置类
 * 用于配置节点的执行参数和行为
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConfig {
    
    /**
     * 节点唯一标识
     */
    @Builder.Default
    private String nodeId = "";
    
    /**
     * 节点名称
     */
    @Builder.Default
    private String nodeName = "";
    
    /**
     * 节点描述
     */
    @Builder.Default
    private String description = "";
    
    /**
     * 节点类型
     */
    @Builder.Default
    private NodeType nodeType = NodeType.DEFAULT;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 超时时间 (毫秒)
     */
    @Builder.Default
    private Long timeout = 30000L;
    
    /**
     * 重试次数
     */
    @Builder.Default
    private Integer retryCount = 0;
    
    /**
     * 重试间隔 (毫秒)
     */
    @Builder.Default
    private Long retryInterval = 1000L;
    
    /**
     * 自定义配置参数
     */
    @Builder.Default
    private Map<String, Object> properties = new java.util.HashMap<>();

    /**
     * 错误处理策略
     */
    @Builder.Default
    private String errorStrategy = "THROW";
    
    /**
     * 日志级别
     */
    @Builder.Default
    private String logLevel = "INFO";
}
