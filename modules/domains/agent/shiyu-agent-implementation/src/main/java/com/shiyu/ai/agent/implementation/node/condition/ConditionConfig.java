package com.shiyu.ai.agent.implementation.node.condition;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定义 Condition 基础设施或应用能力的配置项及装配规则。
 *
 * @author shiyu-ai
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConditionConfig extends NodeConfig {

    /** 条件表达式 */
    private String conditionExpression;

    /** 条件类型（默认：EXPRESSION） */
    @Builder.Default private String conditionType = "EXPRESSION";

    /** 真实分支（条件为 true 时的执行路径） */
    private String trueBranch;

    /** 默认分支（条件为 false 时的执行路径） */
    private String defaultBranch;

    /** 分支映射配置 */
    private String branchMappings;
}
