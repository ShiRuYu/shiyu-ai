package com.shiyu.ai.agent.node.transform;

import com.shiyu.ai.agent.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据转换节点配置类
 * 用于数据格式转换或处理
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TransformConfig extends NodeConfig {
    
    /**
     * 转换类型（默认：JSON_TO_XML）
     */
    private String transformType = "JSON_TO_XML";
    
    /**
     * 输入格式
     */
    private String inputFormat;
    
    /**
     * 输出格式
     */
    private String outputFormat;
    
    /**
     * 转换规则
     */
    private String transformationRules;
    
    /**
     * 模板字符串（用于模板转换）
     */
    private String template;
}
