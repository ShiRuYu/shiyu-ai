package com.shiyu.ai.agent.implementation.node.transform;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定义 Transform 基础设施或应用能力的配置项及装配规则。
 *
 * @author shiyu-ai
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransformConfig extends NodeConfig {

    /** 转换类型（默认：JSON_TO_XML） */
    @Builder.Default private String transformType = "JSON_TO_XML";

    /** 输入格式 */
    private String inputFormat;

    /** 输出格式 */
    private String outputFormat;

    /** 转换规则 */
    private String transformationRules;

    /** 模板字符串（用于模板转换） */
    private String template;
}
