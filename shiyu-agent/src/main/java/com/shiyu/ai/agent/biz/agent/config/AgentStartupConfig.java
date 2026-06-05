package com.shiyu.ai.agent.biz.agent.config;

import com.google.common.collect.Maps;
import com.shiyu.ai.agent.biz.agent.builder.AgentBuilder;
import com.shiyu.ai.agent.biz.agent.domain.AgentDefinition;
import com.shiyu.ai.agent.langgraph4j.node.DefaultNode;
import com.shiyu.ai.agent.langgraph4j.node.llm.LlmCallNode;
import com.shiyu.ai.agent.langgraph4j.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagRetrievalNode;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagRetrievalConfig;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagEnhancementNode;
import com.shiyu.ai.agent.langgraph4j.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.langgraph4j.node.tool.ToolCallNode;
import com.shiyu.ai.agent.langgraph4j.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentNode;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentConfig;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinition;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.langgraph4j.node.intent.IntentType;
import com.shiyu.ai.agent.langgraph4j.node.output.OutputFormatNode;
import com.shiyu.ai.agent.langgraph4j.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.biz.agent.service.AgentService;
import com.shiyu.ai.agent.biz.agent.service.Lc4jService;
import com.shiyu.ai.agent.biz.agent.service.RagService;
import com.shiyu.ai.agent.biz.agent.service.ToolService;
import com.shiyu.ai.agent.biz.agent.service.IntentService;
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
    private final Lc4jService lc4jService;
    private final RagService ragService;
    private final ToolService toolService;
    private final IntentService intentService;

    public AgentStartupConfig(AgentService agentService, Lc4jService lc4jService,
                              RagService ragService, ToolService toolService,
                              IntentService intentService) {
        this.agentService = agentService;
        this.lc4jService = lc4jService;
        this.ragService = ragService;
        this.toolService = toolService;
        this.intentService = intentService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化示例 Agent...");

        createSimpleAssistantAgent();
        createRagKnowledgeAgent();
        createSmartAgent();

        log.info("示例 Agent 初始化完成");
    }

    // ======================== Agent 1: 基础 LLM 问答 ========================

    private void createSimpleAssistantAgent() {
        log.info("创建 Agent: simple-assistant");
        try {
            AgentBuilder builder = new AgentBuilder();
            LlmCallNode llmNode = LlmCallNode.builder()
                    .lc4jService(lc4jService)
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
                    .lc4jService(lc4jService)
                    .config(LlmCallConfig.builder()
                            .nodeName("LLM 回答")
                            .promptTemplate("基于以下检索到的文档内容回答用户问题。\n\n{context}\n\n用户问题: {userInput}")
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
                    .code(IntentType.TASK.getCode()).name(IntentType.TASK.getName())
                    .description(IntentType.TASK.getDescription())
                    .category("general").priority(70).confidenceThreshold(0.85)
                    .examples(new String[]{"查询北京天气", "计算 1+2*3"})
                    .requireSlotFilling(true)
                    .slots(Maps.newHashMap(Map.of("expression", "数学表达式")))
                    .targetNode("tool_call").toolName("calculator").enabled(true).build());

            IntentDefinitionFactory.register("smart-agent", "general", IntentDefinition.builder()
                    .code(IntentType.WEATHER.getCode()).name("天气查询")
                    .description("查询天气信息")
                    .category("general").priority(65).confidenceThreshold(0.8)
                    .examples(new String[]{"北京天气怎么样", "上海今天冷吗"})
                    .requireSlotFilling(true)
                    .slots(Maps.newHashMap(Map.of("city", "城市名称", "date", "日期（可选）")))
                    .parameterMapping(Maps.newHashMap(Map.of("city", "location")))
                    .slotDefaults(Maps.newHashMap(Map.of("unit", "celsius")))
                    .targetNode("tool_call").toolName("weather").enabled(true).build());

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
                    .lc4jService(lc4jService)
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
                    .lc4jService(lc4jService)
                    .config(LlmCallConfig.builder()
                            .nodeName("RAG 回答")
                            .promptTemplate("基于以下检索到的文档回答用户问题。\n\n{context}\n\n用户问题: {userInput}")
                            .stream(false)
                            .build())
                    .build();

            // --- 工具调用节点 ---
            ToolCallNode toolNode = ToolCallNode.builder()
                    .toolService(toolService)
                    .config(ToolCallConfig.builder()
                            .nodeName("工具执行")
                            .enableCache(true)
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
                    .addNode("tool_call", toolNode)
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
                    .addEdge("tool_call", "output")

                    .setStartNode("intent")
                    .setEndNode("output")
                    .buildAndRegister(agentService);

            log.info("Agent smart-agent 创建成功");
        } catch (Exception e) {
            log.error("创建 smart-agent 失败", e);
        }
    }
}
