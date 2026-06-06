package com.shiyu.ai.agent.langgraph4j.node;

import java.util.Set;

/**
 * 节点字段 Schema 接口
 * <p>
 * 每个节点类型对应一个枚举实现，定义该节点在 AgentState/NodeInput/NodeOutput
 * 中读写の所有字段键。集中管理避免了魔法字符串散落在各节点实现中。
 * <p>
 * 当新增节点或修改节点输入/输出时，必须同步更新此处对应的字段定义。
 *
 * @see NodeType
 * @see NodeInput
 * @see NodeOutput
 */
public interface NodeFields {

    /**
     * 该节点从 {@link NodeInput}（即 AgentState）中读取的输入字段
     */
    Set<FieldKey> inputFields();

    /**
     * 该节点写入 {@link NodeOutput} 的输出字段
     */
    Set<FieldKey> outputFields();

    // ========== 全局字段键枚举 ==========

    /**
     * 全局统一字段键，集中管理所有节点在 Map 中使用的字段名。
     * 每个枚举值包含一个 {@link #key()} 字符串，即实际在 Map 中使用的键。
     * 所有节点统一引用此枚举，避免拼写错误和字段名不一致。
     */
    enum FieldKey {
        // ==================== 输入字段 ====================

        /** 用户的原始输入文本 / 查询文本 */
        QUERY("query"),
        /** 平台/提供商（如 SILICON_FLOW, OLLAMA） */
        PLATFORM("platform"),
        /** 模型名称 */
        MODEL("model"),
        /** 聊天类型（SYNC / STREAM） */
        CHAT_TYPE("chatType"),
        /** 工具名称 */
        TOOL_NAME("toolName"),
        /** 工具类型 */
        TOOL_TYPE("toolType"),
        /** 知识库 ID */
        KNOWLEDGE_BASE_ID("knowledgeBaseId"),
        /** 记忆检索范围 */
        RETRIEVAL_SCOPE("retrievalScope"),
        /** Top-K 检索数量 */
        TOP_K("topK"),
        /** 相似度阈值 */
        SIMILARITY_THRESHOLD("similarityThreshold"),
        /** 输入数据（通用） */
        INPUT("input"),
        /** 数据内容（通用） */
        DATA("data"),
        /** 文本内容 */
        TEXT("text"),
        /** 响应内容 */
        RESPONSE("response"),
        /** 结果内容 */
        RESULT("result"),
        /** 输出内容（通用） */
        OUTPUT("output"),
        /** 答案文本 */
        ANSWER("answer"),

        // ==================== 输出字段 ====================

        /** 识别到的意图代码 */
        INTENT_CODE("intentCode"),
        /** 意图名称 */
        INTENT_NAME("intentName"),
        /** 置信度 */
        CONFIDENCE("confidence"),
        /** 意图槽位映射 */
        SLOTS("slots"),
        /** 下一个要执行的节点 ID */
        NEXT_NODE("nextNode"),
        /** 检索到的文档列表 */
        DOCUMENTS("documents"),
        /** 文档数量 */
        DOCUMENT_COUNT("documentCount"),
        /** 上下文文本（RAG / 记忆的上下文拼接结果） */
        CONTEXT("context"),
        /** 记忆条目列表 */
        MEMORIES("memories"),
        /** 记忆数量 */
        MEMORY_COUNT("memoryCount"),
        /** 记忆上下文拼接文本 */
        MEMORY_CONTEXT("memoryContext"),
        /** 工具执行结果 */
        TOOL_RESULT("toolResult"),
        /** 条件判断结果布尔值 */
        CONDITION_RESULT("conditionResult"),
        /** 条件分支标识（"true"/"false"） */
        BRANCH("branch"),
        /** 格式化后的输出内容 */
        FORMATTED_CONTENT("formattedContent"),
        /** 转换后的数据 */
        TRANSFORMED_DATA("transformedData"),
        /** LLM 流式生成器对象 */
        STREAMING_CHAT_GENERATOR("streamingChatGenerator"),
        /** 是否流式 */
        STREAM("stream"),
        /** 使用的平台（输出） */
        PLATFORM_OUTPUT("platform"),
        /** 使用的模型（输出） */
        MODEL_OUTPUT("model"),
        /** 生成的文本内容（输出） */
        CONTENT("content"),
        /** 消息列表（输出/输入） */
        MESSAGES("messages"),

        // ==================== RAG 增强节点专属 ====================

        /** 增强后的文档列表 */
        ENHANCED_DOCUMENTS("enhanced_documents"),
        /** 增强后的文档数量 */
        ENHANCED_COUNT("enhanced_count"),
        /** 使用的增强策略名称 */
        ENHANCEMENT_STRATEGY("enhancement_strategy"),

        // ==================== 工具调用节点专属 ====================

        /** 缓存命中标记 */
        CACHE_HIT("cache_hit"),

        // ==================== 意图节点专属 ====================

        /** agentId */
        AGENT_ID("agentId"),
        /** Slot → 工具参数名的映射（从 IntentDefinition 传递） */
        PARAMETER_MAPPING("parameterMapping"),
        /** Slot 默认值（从 IntentDefinition 传递） */
        SLOT_DEFAULTS("slotDefaults"),
        /** Slot 定义 schema（从 IntentDefinition 传递，key 集用作必填校验） */
        SLOT_DEFINITIONS("slotDefinitions"),

        // ==================== LLM 调用节点专属 ====================

        /** 流式生成器内部对象 */
        STREAMING_GENERATOR("_streaming_generator"),

        // ==================== 条件节点专属 ====================

        /** 条件表达式 */
        CONDITION_EXPRESSION("conditionExpression"),
        /** 条件类型 */
        CONDITION_TYPE("conditionType"),
        /** 真分支 */
        TRUE_BRANCH("trueBranch"),
        /** 默认分支 */
        DEFAULT_BRANCH("defaultBranch"),

        // ==================== 转换节点专属 ====================

        /** 转换类型 */
        TRANSFORM_TYPE("transformType"),

        // ==================== RAG 增强节点专属 ====================

        /** 添加上下文标记 */
        ADD_CONTEXT("addContext"),
        /** 上下文窗口大小 */
        CONTEXT_WINDOW_SIZE("contextWindowSize"),
        /** 最大长度 */
        MAX_LENGTH("maxLength");

        private final String key;

        FieldKey(String key) {
            this.key = key;
        }

        /**
         * @return 在 AgentState Map 中实际使用的字符串键
         */
        public String key() {
            return key;
        }

        /**
         * 根据字符串键查找对应的枚举值
         */
        public static FieldKey fromKey(String key) {
            for (FieldKey fk : values()) {
                if (fk.key.equals(key)) {
                    return fk;
                }
            }
            return null;
        }
    }

    // ========== 各节点类型的字段定义 ==========

    /**
     * 默认节点 - 无特殊字段要求
     */
    enum DefaultFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of();
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of();
        }
    }

    /**
     * 意图识别节点
     * <p>
     * 输入: query
     * 输出: intentCode, intentName, confidence, slots, nextNode
     */
    enum IntentFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.QUERY, FieldKey.AGENT_ID);
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.INTENT_CODE,
                    FieldKey.INTENT_NAME,
                    FieldKey.CONFIDENCE,
                    FieldKey.SLOTS,
                    FieldKey.PARAMETER_MAPPING,
                    FieldKey.SLOT_DEFAULTS,
                    FieldKey.SLOT_DEFINITIONS,
                    FieldKey.NEXT_NODE
            );
        }
    }

    /**
     * LLM 调用节点
     * <p>
     * 输入: query, platform, model, chatType
     * 输出: content, platform, model, messages, streamingChatGenerator, stream, chatType
     */
    enum LlmCallFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.QUERY,
                    FieldKey.PLATFORM,
                    FieldKey.MODEL,
                    FieldKey.CHAT_TYPE
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.CONTENT,
                    FieldKey.PLATFORM_OUTPUT,
                    FieldKey.MODEL_OUTPUT,
                    FieldKey.MESSAGES,
                    FieldKey.STREAMING_CHAT_GENERATOR,
                    FieldKey.STREAM,
                    FieldKey.CHAT_TYPE,
                    FieldKey.STREAMING_GENERATOR
            );
        }
    }

    /**
     * 工具调用节点
     * <p>
     * 输入: toolName, toolResult（前序工具的结果，用于工具链）
     * 输出: toolName, toolResult, text, cacheHit
     */
    enum ToolCallFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.TOOL_NAME, FieldKey.TOOL_RESULT);
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.TOOL_NAME,
                    FieldKey.TOOL_RESULT,
                    FieldKey.TEXT,
                    FieldKey.CACHE_HIT
            );
        }
    }

    /**
     * RAG 检索节点
     * <p>
     * 输入: query, knowledgeBaseId
     * 输出: documents, documentCount, context
     */
    enum RagRetrievalFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.QUERY,
                    FieldKey.KNOWLEDGE_BASE_ID
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.DOCUMENTS,
                    FieldKey.DOCUMENT_COUNT,
                    FieldKey.CONTEXT
            );
        }
    }

    /**
     * RAG 增强节点
     * <p>
     * 输入: documents, context（来自上一个 RAG 检索节点）
     * 输出: context, enhancedDocuments, enhancedCount, enhancementStrategy
     */
    enum RagEnhancementFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.DOCUMENTS,
                    FieldKey.CONTEXT,
                    FieldKey.ADD_CONTEXT,
                    FieldKey.CONTEXT_WINDOW_SIZE,
                    FieldKey.MAX_LENGTH
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.CONTEXT,
                    FieldKey.ENHANCED_DOCUMENTS,
                    FieldKey.ENHANCED_COUNT,
                    FieldKey.ENHANCEMENT_STRATEGY
            );
        }
    }

    /**
     * 短期记忆节点
     * <p>
     * 存储和管理最近的对话历史
     */
    enum ShortTermMemoryFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of();
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of();
        }
    }

    /**
     * 长期记忆节点
     * <p>
     * 存储和管理重要信息和知识点
     */
    enum LongTermMemoryFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of();
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of();
        }
    }

    /**
     * 记忆检索节点
     * <p>
     * 输入: query, retrievalScope, topK, similarityThreshold
     * 输出: memories, memoryCount, memoryContext
     */
    enum MemoryRetrievalFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.QUERY,
                    FieldKey.RETRIEVAL_SCOPE,
                    FieldKey.TOP_K,
                    FieldKey.SIMILARITY_THRESHOLD
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.MEMORIES,
                    FieldKey.MEMORY_COUNT,
                    FieldKey.MEMORY_CONTEXT
            );
        }
    }

    /**
     * 条件判断节点
     * <p>
     * 输入: 根据 conditionExpression 动态读取任意字段 + intentCode（INTENT 模式）
     * 输出: conditionResult, nextNode, branch
     */
    enum ConditionFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            // 条件节点可读取任意输入字段，这里列出已知的静态依赖
            return Set.of(
                    FieldKey.INTENT_CODE,
                    FieldKey.CONDITION_EXPRESSION,
                    FieldKey.CONDITION_TYPE,
                    FieldKey.TRUE_BRANCH,
                    FieldKey.DEFAULT_BRANCH
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.CONDITION_RESULT,
                    FieldKey.NEXT_NODE,
                    FieldKey.BRANCH
            );
        }
    }

    /**
     * 数据转换节点
     * <p>
     * 输入: input / data / content / text / query
     * 输出: transformedData, messages
     */
    enum TransformFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.INPUT,
                    FieldKey.DATA,
                    FieldKey.CONTENT,
                    FieldKey.TEXT,
                    FieldKey.QUERY,
                    FieldKey.TRANSFORM_TYPE
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.TRANSFORMED_DATA,
                    FieldKey.MESSAGES
            );
        }
    }

    /**
     * 输出格式化节点
     * <p>
     * 输入: content / response / result / output / answer / messages
     * 输出: formattedContent, messages
     */
    enum OutputFormatFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.CONTENT,
                    FieldKey.RESPONSE,
                    FieldKey.RESULT,
                    FieldKey.OUTPUT,
                    FieldKey.ANSWER,
                    FieldKey.MESSAGES
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.FORMATTED_CONTENT,
                    FieldKey.MESSAGES
            );
        }
    }

    /**
     * Agent 调用节点
     * <p>
     * 输入: agentId, query
     * 输出: result, content
     */
    enum AgentCallFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.AGENT_ID,
                    FieldKey.QUERY
            );
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.RESULT,
                    FieldKey.CONTENT
            );
        }
    }

    // ========== 工厂方法 ==========

    /**
     * 根据 {@link NodeType} 获取对应的字段定义
     *
     * @param nodeType 节点类型
     * @return 字段定义实例
     */
    static NodeFields forType(NodeType nodeType) {
        if (nodeType == null) {
            return DefaultFields.INSTANCE;
        }
        return switch (nodeType) {
            case DEFAULT -> DefaultFields.INSTANCE;
            case INTENT -> IntentFields.INSTANCE;
            case RAG_RETRIEVAL -> RagRetrievalFields.INSTANCE;
            case RAG_ENHANCEMENT -> RagEnhancementFields.INSTANCE;
            case MEMORY_SHORT_TERM -> ShortTermMemoryFields.INSTANCE;
            case MEMORY_LONG_TERM -> LongTermMemoryFields.INSTANCE;
            case MEMORY_RETRIEVAL -> MemoryRetrievalFields.INSTANCE;
            case LLM_CALL -> LlmCallFields.INSTANCE;
            case TOOL_CALL -> ToolCallFields.INSTANCE;
            case CONDITION -> ConditionFields.INSTANCE;
            case TRANSFORM -> TransformFields.INSTANCE;
            case OUTPUT_FORMAT -> OutputFormatFields.INSTANCE;
            case AGENT_CALL -> AgentCallFields.INSTANCE;
        };
    }
}
