package com.shiyu.ai.agent.contract.node;

import java.util.Set;

/**
 * 定义 Node Fields 相关的协作契约和调用边界。
 *
 * @see NodeType
 * @see NodeInput
 * @see NodeOutput
 */
public interface NodeFields {

    /** 该节点从 {@link NodeInput}（即 AgentState）中读取的输入字段 */
    Set<FieldKey> inputFields();

    /** 该节点写入 {@link NodeOutput} 的输出字段 */
    Set<FieldKey> outputFields();

    // ========== 全局字段键枚举 ==========

    /**
     * 定义 Field Key 可用的枚举值及其业务语义。
     */
    enum FieldKey {
        // ==================== 输入字段 ====================

        /** 用户的原始输入文本 / 查询文本 */
        QUERY("query"),
        /** 平台/提供商（如 SILICON_FLOW, OLLAMA） */
        PLATFORM("platform"),
        /** 模型名称 */
        MODEL("model"),
        /** 用户 ID */
        USER_ID("userId"),
        /**
         * 处理标识。
         *
         * @return 处理结果。
         */
        TENANT_ID("tenantId"),
        /** 聊天类型（SYNC / STREAM） */
        CHAT_TYPE("chatType"),
        /** 工具名称 */
        TOOL_NAME("toolName"),
        /** 工具类型 */
        TOOL_TYPE("toolType"),
        /**
         * 处理ids。
         *
         * @return 处理结果。
         */
        SPACE_IDS("spaceIds"),
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
        /**
         * 处理hits。
         *
         * @return 处理结果。
         */
        RETRIEVAL_HITS("retrievalHits"),
        /**
         * 处理citations。
         *
         * @return 处理结果。
         */
        CITATIONS("citations"),
        /**
         * 处理追踪。
         *
         * @return 处理结果。
         */
        RETRIEVAL_TRACE("retrievalTrace"),
        /**
         * 处理empty。
         *
         * @return 处理结果。
         */
        RETRIEVAL_EMPTY("retrievalEmpty"),
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

        // ==================== 记忆节点专属 ====================

        /** 会话唯一标识 */
        SESSION_ID("sessionId"),
        /** 会话历史文本 */
        CONVERSATION_HISTORY("conversationHistory"),
        /** 记忆键 */
        MEMORY_KEY("memoryKey"),
        /** 记忆内容 */
        MEMORY_CONTENT("memoryContent"),
        /** 分类 */
        CATEGORY("category"),
        /** 重要度分数 */
        IMPORTANCE("importance"),

        // ==================== Agent 调用节点专属 ====================

        /** 目标 Agent ID */
        TARGET_AGENT_ID("targetAgentId"),
        /** 目标版本号 */
        TARGET_VERSION("targetVersion"),
        /** 版本号 */
        VERSION("version"),
        /** 错误信息 */
        ERROR("error"),

        // ==================== RAG 增强节点专属 ====================

        /** 添加上下文标记 */
        ADD_CONTEXT("addContext"),
        /** 上下文窗口大小 */
        CONTEXT_WINDOW_SIZE("contextWindowSize"),
        /** 最大长度 */
        MAX_LENGTH("maxLength");

        /**
         * 键，表示当前对象中的对应属性。
         */
        private final String key;

        FieldKey(String key) {
            this.key = key;
        }

        /**
         * 返回字段键在 AgentState Map 中使用的字符串。
         *
         * @return 在 AgentState Map 中实际使用的字符串键
         */
        public String key() {
            return key;
        }

        /** 根据字符串键查找对应的枚举值 */
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
     * 定义 Default Fields 可用的枚举值及其业务语义。
     */
    enum DefaultFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Default Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of();
        }

        /**
         * 执行 Default Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of();
        }
    }

    /**
     * 定义 Intent Fields 可用的枚举值及其业务语义。
     */
    enum IntentFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Intent Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.QUERY, FieldKey.AGENT_ID);
        }

        /**
         * 执行 Intent Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
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
                    FieldKey.NEXT_NODE);
        }
    }

    /**
     * 定义 Llm Call Fields 可用的枚举值及其业务语义。
     */
    enum LlmCallFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Llm Call Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.QUERY, FieldKey.PLATFORM, FieldKey.MODEL, FieldKey.CHAT_TYPE);
        }

        /**
         * 执行 Llm Call Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
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
                    FieldKey.STREAMING_GENERATOR);
        }
    }

    /**
     * 定义 工具 Call Fields 可用的枚举值及其业务语义。
     */
    enum ToolCallFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 工具 Call Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.TOOL_NAME, FieldKey.TOOL_RESULT);
        }

        /**
         * 执行 工具 Call Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.TOOL_NAME, FieldKey.TOOL_RESULT, FieldKey.TEXT, FieldKey.CACHE_HIT);
        }
    }

    /**
     * 定义 Rag Retrieval Fields 可用的枚举值及其业务语义。
     */
    enum RagRetrievalFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Rag Retrieval Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.QUERY, FieldKey.SPACE_IDS);
        }

        /**
         * 执行 Rag Retrieval Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.RETRIEVAL_HITS,
                    FieldKey.CITATIONS,
                    FieldKey.DOCUMENT_COUNT,
                    FieldKey.CONTEXT,
                    FieldKey.RETRIEVAL_EMPTY);
        }
    }

    /**
     * 定义 Rag Enhancement Fields 可用的枚举值及其业务语义。
     */
    enum RagEnhancementFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Rag Enhancement Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.DOCUMENTS,
                    FieldKey.CONTEXT,
                    FieldKey.ADD_CONTEXT,
                    FieldKey.CONTEXT_WINDOW_SIZE,
                    FieldKey.MAX_LENGTH);
        }

        /**
         * 执行 Rag Enhancement Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(
                    FieldKey.CONTEXT,
                    FieldKey.ENHANCED_DOCUMENTS,
                    FieldKey.ENHANCED_COUNT,
                    FieldKey.ENHANCEMENT_STRATEGY);
        }
    }

    /**
     * 定义 Short Term 记忆 Fields 可用的枚举值及其业务语义。
     */
    enum ShortTermMemoryFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Short Term 记忆 Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.SESSION_ID, FieldKey.AGENT_ID, FieldKey.QUERY, FieldKey.CONTENT);
        }

        /**
         * 执行 Short Term 记忆 Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.CONVERSATION_HISTORY, FieldKey.MESSAGES);
        }
    }

    /**
     * 定义 Long Term 记忆 Fields 可用的枚举值及其业务语义。
     */
    enum LongTermMemoryFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Long Term 记忆 Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.USER_ID,
                    FieldKey.AGENT_ID,
                    FieldKey.SESSION_ID,
                    FieldKey.MEMORY_KEY,
                    FieldKey.MEMORY_CONTENT,
                    FieldKey.CATEGORY,
                    FieldKey.IMPORTANCE);
        }

        /**
         * 执行 Long Term 记忆 Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.MEMORY_KEY, FieldKey.IMPORTANCE);
        }
    }

    /**
     * 定义 记忆 Retrieval Fields 可用的枚举值及其业务语义。
     */
    enum MemoryRetrievalFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 记忆 Retrieval Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.QUERY,
                    FieldKey.RETRIEVAL_SCOPE,
                    FieldKey.TOP_K,
                    FieldKey.SIMILARITY_THRESHOLD,
                    FieldKey.SESSION_ID,
                    FieldKey.USER_ID,
                    FieldKey.AGENT_ID);
        }

        /**
         * 执行 记忆 Retrieval Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.MEMORIES, FieldKey.MEMORY_COUNT, FieldKey.MEMORY_CONTEXT);
        }
    }

    /**
     * 定义 Condition Fields 可用的枚举值及其业务语义。
     */
    enum ConditionFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Condition Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            // 条件节点可读取任意输入字段，这里列出已知的静态依赖
            return Set.of(
                    FieldKey.INTENT_CODE,
                    FieldKey.CONDITION_EXPRESSION,
                    FieldKey.CONDITION_TYPE,
                    FieldKey.TRUE_BRANCH,
                    FieldKey.DEFAULT_BRANCH);
        }

        /**
         * 执行 Condition Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.CONDITION_RESULT, FieldKey.NEXT_NODE, FieldKey.BRANCH);
        }
    }

    /**
     * 定义 Transform Fields 可用的枚举值及其业务语义。
     */
    enum TransformFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Transform Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.INPUT,
                    FieldKey.DATA,
                    FieldKey.CONTENT,
                    FieldKey.TEXT,
                    FieldKey.QUERY,
                    FieldKey.TRANSFORM_TYPE);
        }

        /**
         * 执行 Transform Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.TRANSFORMED_DATA, FieldKey.MESSAGES);
        }
    }

    /**
     * 定义 Output Format Fields 可用的枚举值及其业务语义。
     */
    enum OutputFormatFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 Output Format Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(
                    FieldKey.CONTENT,
                    FieldKey.RESPONSE,
                    FieldKey.RESULT,
                    FieldKey.OUTPUT,
                    FieldKey.ANSWER,
                    FieldKey.MESSAGES);
        }

        /**
         * 执行 Output Format Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.FORMATTED_CONTENT, FieldKey.MESSAGES);
        }
    }

    /**
     * 定义 智能体 Call Fields 可用的枚举值及其业务语义。
     */
    enum AgentCallFields implements NodeFields {
        INSTANCE;

        /**
         * 执行 智能体 Call Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> inputFields() {
            return Set.of(FieldKey.AGENT_ID, FieldKey.QUERY);
        }

        /**
         * 执行 智能体 Call Fields 相关业务数据，并返回处理结果。
         *
         * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
         */
        @Override
        public Set<FieldKey> outputFields() {
            return Set.of(FieldKey.RESULT, FieldKey.CONTENT);
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
        return switch (nodeType.getCode()) {
            case "DEFAULT" -> DefaultFields.INSTANCE;
            case "INTENT" -> IntentFields.INSTANCE;
            case "RAG_RETRIEVAL" -> RagRetrievalFields.INSTANCE;
            case "RAG_ENHANCEMENT" -> RagEnhancementFields.INSTANCE;
            case "MEMORY_SHORT_TERM" -> ShortTermMemoryFields.INSTANCE;
            case "MEMORY_LONG_TERM" -> LongTermMemoryFields.INSTANCE;
            case "MEMORY_RETRIEVAL" -> MemoryRetrievalFields.INSTANCE;
            case "LLM_CALL" -> LlmCallFields.INSTANCE;
            case "TOOL_CALL" -> ToolCallFields.INSTANCE;
            case "CONDITION" -> ConditionFields.INSTANCE;
            case "TRANSFORM" -> TransformFields.INSTANCE;
            case "OUTPUT_FORMAT" -> OutputFormatFields.INSTANCE;
            case "AGENT_CALL" -> AgentCallFields.INSTANCE;
            default -> DefaultFields.INSTANCE;
        };
    }
}
