package com.shiyu.ai.agent.implementation.node.memory;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定义 Short Term 记忆 基础设施或应用能力的配置项及装配规则。
 *
 * @author shiyu-ai
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ShortTermMemoryConfig extends NodeConfig {

    /** 最大消息数量（默认 10） */
    @Builder.Default private Integer maxMessages = 10;

    /** 是否启用滑动窗口（默认 true） */
    @Builder.Default private Boolean enableSlidingWindow = true;

    /** 消息过期时间（毫秒，默认 3600000） */
    @Builder.Default private Long messageExpiryTime = 3600000L;
}
