package com.shiyu.ai.agent.node.tool;

import com.shiyu.ai.agent.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工具调用节点配置类
 * 用于调用外部工具或服务
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ToolCallConfig extends NodeConfig {
    
    /**
     * 工具名称
     */
    private String toolName;
    
    /**
     * 工具类型
     */
    private String toolType;
    
    /**
     * 超时时间（毫秒，默认 10000）
     */
    private Long toolTimeout = 10000L;
    
    /**
     * 是否启用缓存（默认 false）
     */
    private Boolean enableCache = false;
}
