package com.shiyu.ai.aiagent.node;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 节点类型枚举
 * 定义 Agent 中可使用的各种组件类型
 */
@Getter
@AllArgsConstructor
public enum NodeType {

    // ==================== 通用节点（已有） ====================
    DEFAULT("DEFAULT", "默认节点", "基础节点类型"),
    INTENT("INTENT", "意图识别节点", "识别和处理用户意图"),
    RAG_RETRIEVAL("RAG_RETRIEVAL", "RAG 检索节点", "从知识库检索信息"),
    RAG_ENHANCEMENT("RAG_ENHANCEMENT", "RAG 增强节点", "对检索结果增强处理"),
    MEMORY_SHORT_TERM("MEMORY_SHORT_TERM", "短期记忆节点", "存储最近对话历史"),
    MEMORY_LONG_TERM("MEMORY_LONG_TERM", "长期记忆节点", "存储重要信息"),
    MEMORY_RETRIEVAL("MEMORY_RETRIEVAL", "记忆检索节点", "从记忆检索信息"),
    LLM_CALL("LLM_CALL", "LLM 调用节点", "调用大语言模型"),
    TOOL_CALL("TOOL_CALL", "工具调用节点", "调用外部工具"),
    CONDITION("CONDITION", "条件判断节点", "根据条件决定路径"),
    TRANSFORM("TRANSFORM", "数据转换节点", "数据格式转换"),
    OUTPUT_FORMAT("OUTPUT_FORMAT", "输出格式化节点", "格式化输出"),
    AGENT_CALL("AGENT_CALL", "Agent 调用节点", "调用其他 Agent"),

    // ==================== 教育域节点（新增） ====================
    ABILITY_QUERY("ABILITY_QUERY", "能力值查询节点", "查询学生 Bloom 能力值和知识点详情"),
    EDUCATION_TEACH("EDUCATION_TEACH", "教学讲解节点", "AI 个性化教学讲解"),
    EDUCATION_PRACTICE("EDUCATION_PRACTICE", "教育出题节点", "根据知识点和学生水平生成练习题"),
    SCORE_ANALYSIS("SCORE_ANALYSIS", "评分分析节点", "对练习结果评分并更新能力值"),
    REVIEW_SCHEDULE("REVIEW_SCHEDULE", "复习安排节点", "艾宾浩斯遗忘曲线复习安排"),
    PREREQ_CHECK("PREREQ_CHECK", "前置知识检查节点", "检测学生对目标知识点缺失的前置知识");

    private final String code;
    private final String name;
    private final String description;

    public static NodeType fromCode(String code) {
        if (code == null || code.isEmpty()) return DEFAULT;
        for (NodeType t : values()) {
            if (t.getCode().equals(code)) return t;
        }
        return DEFAULT;
    }

    public static NodeType fromCode(String code, NodeType defaultType) {
        if (code == null || code.isEmpty()) return defaultType;
        for (NodeType t : values()) {
            if (t.getCode().equals(code)) return t;
        }
        return defaultType;
    }
}
