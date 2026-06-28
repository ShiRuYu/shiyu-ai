package com.shiyu.ai.aiagent.node.memory;

import com.shiyu.ai.aiagent.node.NodeConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.Builder;

/**
 * 短期记忆节点配置类
 * 用于存储和管理最近的对话历史
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShortTermMemoryConfig extends NodeConfig {
    
    /**
     * 最大消息数量（默认 10）
     */
    @Builder.Default
    private Integer maxMessages = 10;
    
    /**
     * 是否启用滑动窗口（默认 true）
     */
    @Builder.Default
    private Boolean enableSlidingWindow = true;
    
    /**
     * 消息过期时间（毫秒，默认 3600000）
     */
    @Builder.Default
    private Long messageExpiryTime = 3600000L;
}
