package com.shiyu.ai.agent.config;

import com.google.common.collect.Maps;
import com.shiyu.ai.agent.builder.AgentBuilder;
import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.node.DefaultNode;
import com.shiyu.ai.agent.node.llm.LlmCallNode;
import com.shiyu.ai.agent.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.node.rag.RagRetrievalNode;
import com.shiyu.ai.agent.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.node.rag.RagEnhancementNode;
import com.shiyu.ai.agent.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.node.tool.ToolCallNode;
import com.shiyu.ai.agent.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.node.intent.IntentNode;
import com.shiyu.ai.agent.node.intent.IntentConfig;
import com.shiyu.ai.agent.node.intent.IntentDefinition;
import com.shiyu.ai.agent.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.node.intent.IntentType;
import com.shiyu.ai.agent.node.output.OutputFormatNode;
import com.shiyu.ai.agent.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.model.chat.ChatEngine;
import com.shiyu.ai.model.adapter.ModelManager;
import com.shiyu.ai.knowledge.rag.RagService;
import com.shiyu.ai.tool.ToolService;
import com.shiyu.ai.agent.service.IntentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Agent 启动配置
 * <p>
 * 应用启动时自动创建并注册示例 Agent:
 * <ul>
 *   <li>simple-assistant — 基础 LLM 问答</li>
 *   <li>rag-knowledge-agent — 知识库 RAG 检索问答</li>
 *   <li>smart-agent — 意图识别 + 条件路由（闲聊/RAG/工具）</li>
 * </ul>
 */
@Slf4j
@Component
public class AgentStartupConfig implements ApplicationRunner {

    private final AgentService agentService;
    private final ChatEngine chatEngine;
    private final ModelManager modelManager;
    private final RagService ragService;
    private final ToolService toolService;
    private final IntentService intentService;

    public AgentStartupConfig(AgentService agentService, ChatEngine chatEngine, ModelManager modelManager,
                              RagService ragService, ToolService toolService,
                              IntentService intentService) {
        this.agentService = agentService;
        this.chatEngine = chatEngine;
        this.modelManager = modelManager;
        this.ragService = ragService;
        this.toolService = toolService;
        this.intentService = intentService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Agent 已改为懒加载模式，示例 Agent 数据已通过 SQL 种子数据初始化，启动时不再预加载");
    }

    // ======================== Agent 1: 基础 LLM 问答 ========================

    private void createSimpleAssistantAgent() {
        log.info("创建 Agent: simple-assistant");
        try {
            AgentBuilder builder = new AgentBuilder();
            LlmCallNode llmNode = LlmCallNode.builder()
                    .chatEngine(chatEngine)
                    .modelManager(modelManager)
                    .config(LlmCallConfig.builder()
                            .nodeName("LLM 回答")
                            .defaultPrompt("你是一个智能助手，请友好地回答用户的问题。")
                            .stream(false)
                            .build())
                    .build();

            builder.agentId("simple-assistant")
                    .name("简单助手")
                    .description("基础 LLM 问答助手，直接调用大模型回答用户问题")
                    .version("v1.0.0")
                    .versionDescription("初始版本")
                    .addNode("llm", llmNode)
                    .setStartNode("llm")
                    .setEndNode("llm")
                    .buildAndRegister(agentService);

            log.info("Agent simple-assistant 创建成功");
        } catch (Exception e) {
            log.error("创建 simple-assistant 失败", e);
        }
    }

    // ======================== Agent 2: 知识库 RAG 检索问答 ========================

    private void createRagKnowledgeAgent() {
        log.info("创建 Agent: rag-knowledge-agent");
        try {
            // RAG 检索节点
            RagRetrievalNode ragRetrieval = RagRetrievalNode.builder()
                    .ragService(ragService)
                    .config(RagRetrievalConfig.builder()
                            .nodeName("知识库检索")
                            .topK(5)
                            .similarityThreshold(0.6)
                            .build())
                    .build();

            // RAG 增强节点
            RagEnhancementNode ragEnhance = RagEnhancementNode.builder()
                    .config(RagEnhancementConfig.builder()
                            .nodeName("检索增强")
                            .enhancementStrategy("SUMMARIZATION")
                            .contextWindowSize(3)
                            .maxLength(2000)
                            .addContext(true)
                            .build())
                    .build();

            // LLM 调用节点（带 RAG 上下文）
            LlmCallNode llmNode = LlmCallNode.builder()
                    .chatEngine(chatEngine)
                    .modelManager(modelManager)
                    .config(LlmCallConfig.builder()
                            .nodeName("LLM 回答")
                            .promptTemplate("基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {query}")
                            .stream(false)
                            .build())
                    .build();

            // 输出格式化
            OutputFormatNode outputNode = OutputFormatNode.builder()
                    .config(OutputFormatConfig.builder()
                            .nodeName("格式化输出")
                            .outputFormat("TEXT")
                            .prettyPrint(true)
                            .build())
                    .build();

            AgentBuilder builder = new AgentBuilder();
            builder.agentId("rag-knowledge-agent")
                    .name("知识库问答")
                    .description("基于文档知识库的 RAG 检索问答，支持检索、增强、生成流程")
                    .version("v1.0.0")
                    .versionDescription("初始版本 - 支持知识库检索问答")
                    .addNode("input", DefaultNode.builder().build())
                    .addNode("rag_retrieval", ragRetrieval)
                    .addNode("rag_enhance", ragEnhance)
                    .addNode("llm", llmNode)
                    .addNode("output", outputNode)
                    .addEdge("input", "rag_retrieval")
                    .addEdge("rag_retrieval", "rag_enhance")
                    .addEdge("rag_enhance", "llm")
                    .addEdge("llm", "output")
                    .setStartNode("input")
                    .setEndNode("output")
                    .buildAndRegister(agentService);

            log.info("Agent rag-knowledge-agent 创建成功");
        } catch (Exception e) {
            log.error("创建 rag-knowledge-agent 失败", e);
        }
    }

    // ======================== Agent 3: 智能路由 Agent ========================

    private void createSmartAgent() {
        log.info("创建 Agent: smart-agent");
        try {
            // --- 注册 smart-agent 的自定义意图定义到工厂 ---
            IntentDefinitionFactory.register("smart-agent", "general", IntentDefinition.builder()
                    .code(IntentType.CHITCHAT.getCode()).name(IntentType.CHITCHAT.getName())
                    .description(IntentType.CHITCHAT.getDescription())
                    .category("general").priority(50).confidenceThreshold(0.75)
                    .examples(new String[]{"你好", "最近怎么样", "今天天气不错", "你在干什么", "聊聊天吧"})
                    .targetNode("llm_chat").enabled(true).build());

            IntentDefinitionFactory.register("smart-agent", "general", IntentDefinition.builder()
                    .code(IntentType.QUESTION.getCode()).name("知识查询")
                    .description("查询知识库信息")
                    .category("general").priority(60).confidenceThreshold(0.8)
                    .examples(new String[]{"什么是RAG", "Shiyu AI 是什么"})
                    .targetNode("rag_retrieval").enabled(true).build());

            IntentDefinitionFactory.register("smart-agent", "general", IntentDefinition.builder()
                    .code(IntentType.CALCULATOR.getCode()).name(IntentType.CALCULATOR.getName())
                    .description(IntentType.CALCULATOR.getDescription())
                    .category("general").priority(70).confidenceThreshold(0.85)
                    .examples(new String[]{"查询北京天气", "计算 1+2*3"})
                    .requireSlotFilling(true)
                    .slots(Maps.newHashMap(Map.of("expression", "数学表达式")))
                    .targetNode("tool_call_calculator").enabled(true).build());

            IntentDefinitionFactory.register("smart-agent", "general", IntentDefinition.builder()
                    .code(IntentType.WEATHER.getCode()).name(IntentType.WEATHER.getName())
                    .description(IntentType.WEATHER.getDescription())
                    .category("general").priority(65).confidenceThreshold(0.8)
                    .examples(new String[]{"北京天气怎么样", "上海今天冷吗"})
                    .requireSlotFilling(true)
                    .slots(Maps.newHashMap(Map.of("city", "城市名称", "date", "日期（可选）")))
                    .parameterMapping(Maps.newHashMap(Map.of("city", "location")))
                    .slotDefaults(Maps.newHashMap(Map.of("unit", "celsius")))
                    .targetNode("tool_call_weather").enabled(true).build());

            // --- 意图识别节点 ---
            IntentNode intentNode = IntentNode.builder()
                    .intentService(intentService)
                    .config(IntentConfig.builder()
                            .nodeName("意图识别")
                            .category("general")
                            .build())
                    .build();

            // --- LLM 闲聊节点 ---
            LlmCallNode chatNode = LlmCallNode.builder()
                    .chatEngine(chatEngine)
                    .modelManager(modelManager)
                    .config(LlmCallConfig.builder()
                            .nodeName("闲聊回答")
                            .defaultPrompt("你是一个友好的 AI 助手，请用轻松自然的语气和用户聊天。")
                            .stream(false)
                            .build())
                    .build();

            // --- RAG 检索节点 ---
            RagRetrievalNode ragRetrieval = RagRetrievalNode.builder()
                    .ragService(ragService)
                    .config(RagRetrievalConfig.builder()
                            .nodeName("知识库检索")
                            .topK(3)
                            .build())
                    .build();

            // --- RAG 增强节点 ---
            RagEnhancementNode ragEnhance = RagEnhancementNode.builder()
                    .config(RagEnhancementConfig.builder()
                            .nodeName("检索增强")
                            .enhancementStrategy("SUMMARIZATION")
                            .contextWindowSize(3)
                            .build())
                    .build();

            // --- LLM 回答节点（RAG 结果上生成） ---
            LlmCallNode ragLlmNode = LlmCallNode.builder()
                    .chatEngine(chatEngine)
                    .modelManager(modelManager)
                    .config(LlmCallConfig.builder()
                            .nodeName("RAG 回答")
                            .promptTemplate("基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {query}")
                            .stream(false)
                            .build())
                    .build();

            // --- 天气工具节点 ---
            ToolCallNode weatherTool = ToolCallNode.builder()
                    .toolService(toolService)
                    .config(ToolCallConfig.builder()
                            .nodeName("天气查询工具")
                            .toolName(IntentType.WEATHER.getCode())
                            .enableCache(true)
                            .build())
                    .build();

            // --- 计算器工具节点 ---
            ToolCallNode calcTool = ToolCallNode.builder()
                    .toolService(toolService)
                    .config(ToolCallConfig.builder()
                            .nodeName("计算器工具")
                            .toolName(IntentType.CALCULATOR.getCode())
                            .enableCache(true)
                            .build())
                    .build();

            // --- 工具结果 LLM 回答节点 ---
            LlmCallNode toolLlmNode = LlmCallNode.builder()
                    .chatEngine(chatEngine)
                    .modelManager(modelManager)
                    .config(LlmCallConfig.builder()
                            .nodeName("工具结果回答")
                            .promptTemplate("以下是工具执行结果，请用自然语言回复用户。\n\n工具结果: {toolResult}\n\n用户问题: {query}")
                            .stream(false)
                            .build())
                    .build();

            // --- 输出格式化节点 ---
            OutputFormatNode outputNode = OutputFormatNode.builder()
                    .config(OutputFormatConfig.builder()
                            .nodeName("格式化输出")
                            .outputFormat("TEXT")
                            .prettyPrint(true)
                            .build())
                    .build();

            // --- 用 AgentBuilder 组装 ---
            AgentBuilder builder = new AgentBuilder();
            builder.agentId("smart-agent")
                    .name("智能路由助手")
                    .description("支持意图识别、RAG 知识检索、工具调用、闲聊的全功能智能助手")
                    .version("v1.0.0")
                    .versionDescription("初始版本 - 全功能智能路由")

                    // 节点
                    .addNode("intent", intentNode)
                    .addNode("llm_chat", chatNode)
                    .addNode("rag_retrieval", ragRetrieval)
                    .addNode("rag_enhance", ragEnhance)
                    .addNode("rag_llm", ragLlmNode)
                    .addNode("tool_call_weather", weatherTool)
                    .addNode("tool_call_calculator", calcTool)
                    .addNode("tool_llm", toolLlmNode)
                    .addNode("output", outputNode)

                    // 意图 → 条件路由（由 IntentDefinitionFactory 驱动）
                    // 新增意图时只需注册 IntentDefinition，路由自动适配
                    .addConditionalEdge("intent", "llm_chat",
                            IntentDefinitionFactory.buildRoutingPredicates(
                                    "smart-agent", "general"))

                    // RAG 链路
                    .addEdge("rag_retrieval", "rag_enhance")
                    .addEdge("rag_enhance", "rag_llm")
                    .addEdge("rag_llm", "output")

                    // 闲聊 & 工具 → 输出
                    .addEdge("llm_chat", "output")
                    .addEdge("tool_call_weather", "tool_llm")
                    .addEdge("tool_call_calculator", "tool_llm")
                    .addEdge("tool_llm", "output")

                    .setStartNode("intent")
                    .setEndNode("output")
                    .buildAndRegister(agentService);

            log.info("Agent smart-agent 创建成功");
        } catch (Exception e) {
            log.error("创建 smart-agent 失败", e);
        }
    }
}
