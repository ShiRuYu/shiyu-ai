package com.shiyu.ai.agent.langgraph4j.node;

import java.util.Set;

/**
 * 节点字段 Schema 接口
 * <p>
 * 每个节点类型对应一个枚举实现，定义该节点在 AgentState/NodeInput/NodeOutput
 * 中读写的所有字段键。集中管理避免了魔法字符串散落在各节点实现中。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 校验输入是否齐全
 * NodeFields fields = NodeFields.forType(nodeType);
 * for (NodeFields.FieldKey key : fields.inputFields()) {
 *     if (!input.hasParameter(key.key())) {
 *         throw new IllegalArgumentException("缺少必需输入字段: " + key);
 *     }
 * }
 * }</pre>
 *
 * @author shiyu-ai
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
     * <p>
     * 每个枚举值包含一个 {@link #key()} 字符串，即实际在 Map 中使用的键。
     * 所有节点统一引用此枚举，避免拼写错误和字段名不一致。
     */
    enum FieldKey {
        // ==================== 输入字段 ====================

        /**
         * 用户的原始输入文本
         */
        USER_INPUT("userInput"),
        /**
         * 查询文本（语义同 USER_INPUT，用于不同的上下文）
         */
        QUERY("query"),
        /**
         * 拼接好的 Prompt 文本
         */
        PROMPT("prompt"),
        /**
         * 平台/提供商（如 SILICON_FLOW, OLLAMA）
         */
        PLATFORM("platform"),
        /**
         * 模型名称
         */
        MODEL("model"),
        /**
         * 聊天类型（SYNC / STREAM）
         */
        CHAT_TYPE("chatType"),
        /**
         * 工具名称
         */
        TOOL_NAME("toolName"),
        /**
         * 工具类型
         */
        TOOL_TYPE("toolType"),
        /**
         * 知识库 ID
         */
        KNOWLEDGE_BASE_ID("knowledgeBaseId"),
        /**
         * 记忆检索范围
         */
        RETRIEVAL_SCOPE("retrievalScope"),
        /**
         * Top-K 检索数量
         */
        TOP_K("topK"),
        /**
         * 相似度阈值
         */
        SIMILARITY_THRESHOLD("similarityThreshold"),
        /**
         * 输入数据（通用）
         */
        INPUT("input"),
        /**
         * 数据内容（通用）
         */
        DATA("data"),
        /**
         * 文本内容
         */
        TEXT("text"),
        /**
         * 响应内容
         */
        RESPONSE("response"),
        /**
         * 结果内容
         */
        RESULT("result"),
        /**
         * 输出内容（通用）
         */
        OUTPUT("output"),
        /**
         * 答案文本
         */
        ANSWER("answer"),

        // ==================== 输出字段 ====================

        /**
         * 识别到的意图代码
         */
        INTENT_CODE("intentCode"),
        /**
         * 意图名称
         */
        INTENT_NAME("intentName"),
        /**
         * 置信度
         */
        CONFIDENCE("confidence"),
        /**
         * 意图槽位映射
         */
        SLOTS("slots"),
        /**
         * 下一个要执行的节点 ID
         */
        NEXT_NODE("nextNode"),
        /**
         * 检索到的文档列表
         */
        DOCUMENTS("documents"),
        /**
         * 文档数量
         */
        DOCUMENT_COUNT("documentCount"),
        /**
         * 上下文文本（RAG / 记忆的上下文拼接结果）
         */
        CONTEXT("context"),
        /**
         * 记忆条目列表
         */
        MEMORIES("memories"),
        /**
         * 记忆数量
         */
        MEMORY_COUNT("memoryCount"),
        /**
         * 记忆上下文拼接文本
         */
        MEMORY_CONTEXT("memoryContext"),
        /**
         * 工具执行结果
         */
        TOOL_RESULT("toolResult"),
        /**
         * 条件判断结果布尔值
         */
        CONDITION_RESULT("conditionResult"),
        /**
         * 条件分支标识（"true"/"false"）
         */
        BRANCH("branch"),
        /**
         * 格式化后的输出内容
         */
        FORMATTED_CONTENT("formattedContent"),
        /**
         * 转换后的数据
         */
        TRANSFORMED_DATA("transformedData"),
        /**
         * LLM 流式生成器对象
         */
        STREAMING_CHAT_GENERATOR("streamingChatGenerator"),
        /**
         * 是否流式
         */
        STREAM("stream"),
        /**
         * 使用的平台（输出）
         */
        PLATFORM_OUTPUT("platform"),
        /**
         * 使用的模型（输出）
         */
        MODEL_OUTPUT("model"),
        /**
         * 生成的文本内容（输出）
         */
        CONTENT("content"),
        /**
         * 消息列表（输出/输入）
         */
        MESSAGES("messages");

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
     * 输入：userInput / query
     * 输出：intentCode, intentName, confidence, slots, nextNode
     */
    enum IntentFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.USER_INPUT, FieldKey.QUERY);
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.INTENT_CODE,
                    FieldKey.INTENT_NAME,
                    FieldKey.CONFIDENCE,
                    FieldKey.SLOTS,
                    FieldKey.NEXT_NODE
            );
        }
    }

    /**
     * LLM 调用节点
     * <p>
     * 输入：prompt / query / userInput, platform, model, chatType
     * 输出：content, platform, model, messages, streamingChatGenerator, stream, chatType
     */
    enum LlmCallFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.PROMPT,
                    FieldKey.QUERY,
                    FieldKey.USER_INPUT,
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
                    FieldKey.CHAT_TYPE
            );
        }
    }

    /**
     * 工具调用节点
     * <p>
     * 输入：toolName
     * 输出：toolName, toolResult
     */
    enum ToolCallFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.TOOL_NAME);
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.TOOL_NAME, FieldKey.TOOL_RESULT);
        }
    }

    /**
     * RAG 检索节点
     * <p>
     * 输入：query / userInput, knowledgeBaseId
     * 输出：documents, documentCount, context
     */
    enum RagRetrievalFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.QUERY,
                    FieldKey.USER_INPUT,
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
     * 输入：documents, context（来自上一个 RAG 检索节点）
     * 输出：（无特殊输出字段）
     */
    enum RagEnhancementFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.DOCUMENTS, FieldKey.CONTEXT);
        }

        @Override
        public Set<FieldKey> outputFields() {
            return Set.of();
        }
    }

    /**
     * 短期记忆节点
     * <p>
     * 存储和管理最近的对话历史
     * 输入/输出：（无特殊 map 字段，操作独立存储）
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
     * 输入/输出：（无特殊 map 字段，操作独立存储）
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
     * 输入：query / userInput, retrievalScope, topK, similarityThreshold
     * 输出：memories, memoryCount, memoryContext
     */
    enum MemoryRetrievalFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.QUERY,
                    FieldKey.USER_INPUT,
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
     * 输入：根据 conditionExpression 动态读取任意字段 + intentCode（INTENT 模式）
     * 输出：conditionResult, nextNode, branch
     */
    enum ConditionFields implements NodeFields {
        INSTANCE;

        @Override
        public Set<FieldKey> inputFields() {
            // 条件节点可读取任意输入字段，这里列出已知的静态依赖
            return Set.of(FieldKey.INTENT_CODE);
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
     * 输入：input / data / content / text / query / userInput
     * 输出：transformedData, messages
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
                    FieldKey.USER_INPUT
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
     * 输入：content / response / result / output / answer / messages
     * 输出：formattedContent, messages
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
        };
    }
}
