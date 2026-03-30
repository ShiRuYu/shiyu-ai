package com.shiyu.ai.agent.langgraph4j.node.memory;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短期记忆节点配置类
 * 用于存储和管理最近的对话历史
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShortTermMemoryConfig extends NodeConfig {
    
    /**
     * 最大消息数量（默认 10）
     */
    private Integer maxMessages = 10;
    
    /**
     * 是否启用滑动窗口（默认 true）
     */
    private Boolean enableSlidingWindow = true;
    
    /**
     * 消息过期时间（毫秒，默认 3600000）
     */
    private Long messageExpiryTime = 3600000L;
}
