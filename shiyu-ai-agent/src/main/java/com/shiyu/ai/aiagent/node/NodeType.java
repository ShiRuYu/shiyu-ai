package com.shiyu.ai.aiagent.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点类型枚举
 * 定义 Agent 中可使用的各种组件类型
 *
 * @author shiyu-ai
 * @date 2026-03-27
 */
@Getter
@AllArgsConstructor
public enum NodeType {
    
    /**
     * 默认节点
     */
    DEFAULT("DEFAULT", "默认节点", "基础节点类型，用于通用处理逻辑"),
    
    /**
     * 意图识别节点
     */
    INTENT("INTENT", "意图识别节点", "用于识别和处理用户的意图"),
    
    /**
     * RAG 检索节点
     */
    RAG_RETRIEVAL("RAG_RETRIEVAL", "RAG 检索节点", "用于从知识库中检索相关信息"),
    
    /**
     * RAG 增强节点
     */
    RAG_ENHANCEMENT("RAG_ENHANCEMENT", "RAG 增强节点", "用于对检索结果进行增强处理"),
    
    /**
     * 短期记忆节点
     */
    MEMORY_SHORT_TERM("MEMORY_SHORT_TERM", "短期记忆节点", "用于存储和管理最近的对话历史"),
    
    /**
     * 长期记忆节点
     */
    MEMORY_LONG_TERM("MEMORY_LONG_TERM", "长期记忆节点", "用于存储和管理重要信息和知识点"),
    
    /**
     * 记忆检索节点
     */
    MEMORY_RETRIEVAL("MEMORY_RETRIEVAL", "记忆检索节点", "用于从记忆中检索相关信息"),
    
    /**
     * LLM 调用节点
     */
    LLM_CALL("LLM_CALL", "LLM 调用节点", "用于调用大语言模型生成回复"),
    
    /**
     * 工具调用节点
     */
    TOOL_CALL("TOOL_CALL", "工具调用节点", "用于调用外部工具或服务"),
    
    /**
     * 条件判断节点
     */
    CONDITION("CONDITION", "条件判断节点", "用于根据条件决定执行路径"),
    
    /**
     * 数据转换节点
     */
    TRANSFORM("TRANSFORM", "数据转换节点", "用于数据格式转换或处理"),
    
    /**
     * 输出格式化节点
     */
    OUTPUT_FORMAT("OUTPUT_FORMAT", "输出格式化节点", "用于格式化最终输出结果"),
    
    /**
     * Agent 调用节点
     */
    AGENT_CALL("AGENT_CALL", "Agent 调用节点", "用于调用其他已注册的 Agent 执行子任务");
    
    /**
     * 节点类型代码
     */
    private final String code;
    
    /**
     * 节点类型名称
     */
    private final String name;
    
    /**
     * 节点类型描述
     */
    private final String description;
    
    /**
     * 根据代码获取节点类型
     *
     * @param code 节点类型代码
     * @return 节点类型枚举，未找到返回 DEFAULT
     */
    public static NodeType fromCode(String code) {
        if (code == null || code.isEmpty()) {
            return DEFAULT;
        }
        for (NodeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return DEFAULT;
    }
    
    /**
     * 根据代码获取节点类型
     *
     * @param code 节点类型代码
     * @param defaultType 默认类型
     * @return 节点类型枚举，未找到返回指定默认值
     */
    public static NodeType fromCode(String code, NodeType defaultType) {
        if (code == null || code.isEmpty()) {
            return defaultType;
        }
        for (NodeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return defaultType;
    }
}
