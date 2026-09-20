package com.shiyu.ai.agent.implementation.node.tool;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定义 工具 Call 基础设施或应用能力的配置项及装配规则。
 *
 * @author shiyu-ai
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ToolCallConfig extends NodeConfig {

    /** 工具名称 */
    private String toolName;

    /** 工具类型 */
    private String toolType;

    /** 超时时间（毫秒，默认 10000） */
    @Builder.Default private Long toolTimeout = 10000L;

    /** 是否启用缓存（默认 false） */
    @Builder.Default private Boolean enableCache = false;
}
