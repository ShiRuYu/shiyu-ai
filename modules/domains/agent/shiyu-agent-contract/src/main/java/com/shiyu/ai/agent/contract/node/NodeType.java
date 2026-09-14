package com.shiyu.ai.agent.contract.node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 定义 Agent 节点类型及其配置和输出字段映射。
 */
public final class NodeType {

    public static final NodeType DEFAULT = builtIn("DEFAULT", "默认节点", "基础节点类型");
    public static final NodeType INTENT = builtIn("INTENT", "意图识别节点", "识别和处理用户意图");
    public static final NodeType RAG_RETRIEVAL = builtIn("RAG_RETRIEVAL", "RAG 检索节点", "从知识库检索信息");
    public static final NodeType RAG_ENHANCEMENT = builtIn("RAG_ENHANCEMENT", "RAG 增强节点", "对检索结果增强处理");
    public static final NodeType MEMORY_SHORT_TERM = builtIn("MEMORY_SHORT_TERM", "短期记忆节点", "存储最近对话历史");
    public static final NodeType MEMORY_LONG_TERM = builtIn("MEMORY_LONG_TERM", "长期记忆节点", "存储重要信息");
    public static final NodeType MEMORY_RETRIEVAL = builtIn("MEMORY_RETRIEVAL", "记忆检索节点", "从记忆检索信息");
    public static final NodeType LLM_CALL = builtIn("LLM_CALL", "LLM 调用节点", "调用大语言模型");
    public static final NodeType TOOL_CALL = builtIn("TOOL_CALL", "工具调用节点", "调用外部工具");
    public static final NodeType CONDITION = builtIn("CONDITION", "条件判断节点", "根据条件决定路径");
    public static final NodeType TRANSFORM = builtIn("TRANSFORM", "数据转换节点", "数据格式转换");
    public static final NodeType OUTPUT_FORMAT = builtIn("OUTPUT_FORMAT", "输出格式化节点", "格式化输出");
    public static final NodeType AGENT_CALL = builtIn("AGENT_CALL", "Agent 调用节点", "调用其他 Agent");

    private static final NodeType[] BUILT_INS = {
        DEFAULT,
        INTENT,
        RAG_RETRIEVAL,
        RAG_ENHANCEMENT,
        MEMORY_SHORT_TERM,
        MEMORY_LONG_TERM,
        MEMORY_RETRIEVAL,
        LLM_CALL,
        TOOL_CALL,
        CONDITION,
        TRANSFORM,
        OUTPUT_FORMAT,
        AGENT_CALL
    };

    private static final Map<String, NodeType> BUILT_INS_BY_CODE = indexBuiltIns();

    /**
     * 编码，表示当前对象中的对应属性。
     */
    private final String code;
    /**
     * 名称，表示当前对象中的对应属性。
     */
    private final String name;
    /**
     * 描述，表示当前对象中的对应属性。
     */
    private final String description;
    /**
     * builtIn 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final boolean builtIn;

    /**
     * 处理节点类型。
     *
     * @param code code 参数。
     *
     * @return 处理结果。
     */
    public NodeType(String code) {
        this(code, code, "扩展节点类型", false);
    }

    private NodeType(String code, String name, String description, boolean builtIn) {
        this.code = requireText(code, "节点类型编码");
        this.name = requireText(name, "节点类型名称");
        this.description = description == null ? "" : description;
        this.builtIn = builtIn;
    }

    private static NodeType builtIn(String code, String name, String description) {
        return new NodeType(code, name, description, true);
    }

    /**
     * 创建自定义节点类型。
     *
     * @param code code 参数。
     * @param name 名称。
     * @param description description 参数。
     *
     * @return 处理结果。
     */
    public static NodeType custom(String code, String name, String description) {
        return new NodeType(code, name, description, false);
    }

    /**
     * {@code getCode} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getCode() {
        return code;
    }

    /**
     * {@code getName} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getName() {
        return name;
    }

    /**
     * {@code getDescription} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@code isBuiltIn} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean isBuiltIn() {
        return builtIn;
    }

    /**
     * 返回节点类型编码名称。
     *
     * @return 处理结果。
     */
    public String name() {
        return code;
    }

    /**
     * 返回全部内置节点类型。
     *
     * @return 处理结果。
     */
    public static NodeType[] values() {
        return BUILT_INS.clone();
    }

    /**
     * 根据编码解析节点类型；未知编码将创建自定义节点类型。
     *
     * @param code code 参数。
     *
     * @return 处理结果。
     */
    public static NodeType fromCode(String code) {
        if (code == null || code.isBlank()) return DEFAULT;
        NodeType builtIn = BUILT_INS_BY_CODE.get(code);
        return builtIn != null ? builtIn : custom(code, code, "扩展节点类型");
    }

    /**
     * 根据编码解析节点类型；未知编码将创建自定义节点类型。
     *
     * @param code code 参数。
     * @param defaultType defaultType 参数。
     *
     * @return 处理结果。
     */
    public static NodeType fromCode(String code, NodeType defaultType) {
        if (code == null || code.isBlank()) return defaultType;
        NodeType builtIn = BUILT_INS_BY_CODE.get(code);
        return builtIn != null ? builtIn : defaultType;
    }

    private static Map<String, NodeType> indexBuiltIns() {
        Map<String, NodeType> index = new LinkedHashMap<>();
        for (NodeType type : BUILT_INS) {
            index.put(type.code, type);
        }
        return Map.copyOf(index);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + "不能为空");
        }
        return value;
    }

    /**
     * {@code equals} 执行当前类型定义的业务操作。
     *
     * @param other 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public boolean equals(Object other) {
        return this == other || other instanceof NodeType that && code.equals(that.code);
    }

    /**
     * {@code hashCode} 校验当前操作的输入或状态是否满足约束。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    /**
     * {@code toString} 将当前对象转换为目标表示形式。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public String toString() {
        return code;
    }
}
