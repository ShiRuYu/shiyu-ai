package com.shiyu.ai.agent.langgraph4j.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 节点配置类
 * 用于配置节点的执行参数和行为
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class NodeConfig {

    /** 节点唯一标识 */
    private String nodeId = "";

    /** 节点名称 */
    private String nodeName = "";

    /** 节点描述 */
    private String description = "";

    /** 节点类型 */
    private NodeType nodeType = NodeType.DEFAULT;

    /** 是否启用 */
    private Boolean enabled = true;

    /** 超时时间 (毫秒) */
    private Long timeout = 30000L;

    /** 重试次数 */
    private Integer retryCount = 0;

    /** 重试间隔 (毫秒) */
    private Long retryInterval = 1000L;

    /** 自定义配置参数 */
    private Map<String, Object> properties = new HashMap<>();

    /** 错误处理策略 */
    private String errorStrategy = "THROW";

    /** 日志级别 */
    private String logLevel = "INFO";
}
