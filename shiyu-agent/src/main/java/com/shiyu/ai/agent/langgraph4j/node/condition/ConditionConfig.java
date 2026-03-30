package com.shiyu.ai.agent.langgraph4j.node.condition;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 条件判断节点配置类
 * 用于根据条件决定执行路径
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConditionConfig extends NodeConfig {
    
    /**
     * 条件表达式
     */
    private String conditionExpression;
    
    /**
     * 条件类型（默认：EXPRESSION）
     */
    private String conditionType = "EXPRESSION";
    
    /**
     * 真实分支（条件为 true 时的执行路径）
     */
    private String trueBranch;
    
    /**
     * 默认分支（条件为 false 时的执行路径）
     */
    private String defaultBranch;
    
    /**
     * 分支映射配置
     */
    private String branchMappings;
}
