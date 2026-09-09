package com.shiyu.ai.agent.implementation.node;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.implementation.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.implementation.node.agent.AgentCallNode;
import com.shiyu.ai.agent.implementation.node.condition.ConditionConfig;
import com.shiyu.ai.agent.implementation.node.condition.ConditionNode;
import com.shiyu.ai.agent.implementation.node.intent.IntentConfig;
import com.shiyu.ai.agent.implementation.node.intent.IntentNode;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.agent.implementation.node.memory.LongTermMemoryConfig;
import com.shiyu.ai.agent.implementation.node.memory.LongTermMemoryNode;
import com.shiyu.ai.agent.implementation.node.memory.MemoryRetrievalConfig;
import com.shiyu.ai.agent.implementation.node.memory.MemoryRetrievalNode;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryConfig;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.agent.implementation.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.implementation.node.output.OutputFormatNode;
import com.shiyu.ai.agent.implementation.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.implementation.node.rag.RagEnhancementNode;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalNode;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallNode;
import com.shiyu.ai.agent.implementation.node.transform.TransformConfig;
import com.shiyu.ai.agent.implementation.node.transform.TransformNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Registry of node type metadata and pure fallback creators. */
@Slf4j
final class NodeTypeRegistry {
    private final Map<NodeType, CreatorInfo<?>> creators = new ConcurrentHashMap<>();

    NodeTypeRegistry() {
        registerDefaults();
    }

    <T extends NodeConfig> void register(NodeType nodeType, Class<T> configClass,
                                         NodeFactory.NodeCreator<T> nodeCreator) {
        if (nodeType == null || configClass == null || nodeCreator == null) {
            throw new IllegalArgumentException("节点类型、配置类型和创建器不能为空");
        }
        creators.put(nodeType, new CreatorInfo<>(configClass, nodeCreator));
        log.info("已注册节点类型：codePresent={}, namePresent={}",
                nodeType.getCode() != null, nodeType.getName() != null);
    }

    CreatorInfo<?> get(NodeType nodeType) {
        return creators.get(nodeType);
    }

    private void registerDefaults() {
        register(NodeType.DEFAULT, NodeConfig.class, config -> DefaultNode.builder().config(config).build());
        register(NodeType.RAG_ENHANCEMENT, RagEnhancementConfig.class,
                config -> RagEnhancementNode.builder().config(config).build());
        register(NodeType.CONDITION, ConditionConfig.class,
                config -> ConditionNode.builder().config(config).build());
        register(NodeType.TRANSFORM, TransformConfig.class,
                config -> TransformNode.builder().config(config).build());
        register(NodeType.OUTPUT_FORMAT, OutputFormatConfig.class,
                config -> OutputFormatNode.builder().config(config).build());

        register(NodeType.INTENT, IntentConfig.class, config -> IntentNode.builder().config(config).build());
        register(NodeType.RAG_RETRIEVAL, RagRetrievalConfig.class,
                config -> RagRetrievalNode.builder().config(config).build());
        register(NodeType.MEMORY_SHORT_TERM, ShortTermMemoryConfig.class,
                config -> ShortTermMemoryNode.builder().config(config).build());
        register(NodeType.MEMORY_LONG_TERM, LongTermMemoryConfig.class,
                config -> LongTermMemoryNode.builder().config(config).build());
        register(NodeType.MEMORY_RETRIEVAL, MemoryRetrievalConfig.class,
                config -> MemoryRetrievalNode.builder().config(config).build());
        register(NodeType.LLM_CALL, LlmCallConfig.class,
                config -> LlmCallNode.builder().config(config).build());
        register(NodeType.TOOL_CALL, ToolCallConfig.class,
                config -> ToolCallNode.builder().config(config).build());
        register(NodeType.AGENT_CALL, AgentCallConfig.class,
                config -> AgentCallNode.builder().config(config).build());

        register(NodeType.ABILITY_QUERY, NodeConfig.class, config -> unsupportedBeanNode());
        register(NodeType.EDUCATION_TEACH, NodeConfig.class, config -> unsupportedBeanNode());
        register(NodeType.EDUCATION_PRACTICE, NodeConfig.class, config -> unsupportedBeanNode());
        register(NodeType.SCORE_ANALYSIS, NodeConfig.class, config -> unsupportedBeanNode());
        register(NodeType.REVIEW_SCHEDULE, NodeConfig.class, config -> unsupportedBeanNode());
        register(NodeType.PREREQ_CHECK, NodeConfig.class, config -> unsupportedBeanNode());
    }

    private BaseNode unsupportedBeanNode() {
        throw new UnsupportedOperationException("节点通过 Bean NodeCreator 创建，请联系开发人员检查 Spring Bean 注入");
    }

    record CreatorInfo<T extends NodeConfig>(Class<T> configClass, NodeFactory.NodeCreator<T> nodeCreator) {
    }
}
