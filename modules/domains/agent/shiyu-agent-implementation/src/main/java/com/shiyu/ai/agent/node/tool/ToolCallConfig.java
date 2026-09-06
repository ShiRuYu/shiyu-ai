package com.shiyu.ai.agent.node.tool;

import com.shiyu.ai.agent.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * 工具调用节点配置类
 * 用于调用外部工具或服务
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
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
    @Builder.Default
    private Long toolTimeout = 10000L;
    
    /**
     * 是否启用缓存（默认 false）
     */
    @Builder.Default
    private Boolean enableCache = false;
}
