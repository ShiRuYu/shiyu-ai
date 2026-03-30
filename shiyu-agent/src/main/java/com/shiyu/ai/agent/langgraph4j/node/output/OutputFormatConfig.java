package com.shiyu.ai.agent.langgraph4j.node.output;

import com.shiyu.ai.agent.langgraph4j.node.NodeConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 输出格式化节点配置类
 * 用于格式化最终输出结果
 *
 * @author shiyu-ai
 * @date 2026-03-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OutputFormatConfig extends NodeConfig {
    
    /**
     * 输出格式（默认：TEXT）
     */
    private String outputFormat = "TEXT";
    
    /**
     * 模板内容
     */
    private String template;
    
    /**
     * 是否美化输出（默认 false）
     */
    private Boolean prettyPrint = false;
    
    /**
     * 包含的元数据字段
     */
    private String[] includeMetadata;
}
