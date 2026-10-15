package com.shiyu.ai.agent.implementation.node;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.contract.node.*;
import com.shiyu.ai.agent.contract.node.NodeFields.FieldKey;
import com.shiyu.ai.agent.contract.node.NodeApprovalRequiredException;
import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunContext;
import com.shiyu.ai.agent.contract.runtime.AiRunSource;
import com.shiyu.ai.agent.implementation.domain.model.IntentDefBO;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.implementation.node.agent.AgentCallNode;
import com.shiyu.ai.agent.implementation.node.condition.ConditionConfig;
import com.shiyu.ai.agent.implementation.node.condition.ConditionNode;
import com.shiyu.ai.agent.implementation.node.intent.IntentConfig;
import com.shiyu.ai.agent.implementation.node.intent.IntentDefinitionFactory;
import com.shiyu.ai.agent.implementation.node.intent.IntentNode;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallConfig;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.agent.implementation.node.memory.LongTermMemoryNode;
import com.shiyu.ai.agent.implementation.node.memory.ShortTermMemoryNode;
import com.shiyu.ai.agent.implementation.node.output.OutputFormatConfig;
import com.shiyu.ai.agent.implementation.node.output.OutputFormatNode;
import com.shiyu.ai.agent.implementation.node.rag.RagEnhancementConfig;
import com.shiyu.ai.agent.implementation.node.rag.RagEnhancementNode;
import com.shiyu.ai.agent.implementation.node.rag.RagRetrievalNode;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallNode;
import com.shiyu.ai.agent.implementation.node.transform.TransformConfig;
import com.shiyu.ai.agent.implementation.node.transform.TransformNode;
import com.shiyu.ai.agent.implementation.runtime.model.AgentExecutionContext;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolExecutionPipeline;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiAppRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryAiRunRepository;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.service.IntentService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.knowledge.contract.api.KnowledgeRetrievalService;
import com.shiyu.ai.knowledge.contract.model.KnowledgeCitation;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalHit;
import com.shiyu.ai.knowledge.contract.model.KnowledgeRetrievalResult;
import com.shiyu.ai.memory.contract.api.MemoryIngestionPort;
import com.shiyu.ai.memory.contract.api.MemoryQueryPort;
import com.shiyu.ai.memory.contract.model.ConfirmationPolicy;
import com.shiyu.ai.memory.contract.model.MemoryEvent;
import com.shiyu.ai.memory.contract.model.MemoryEventStatus;
import com.shiyu.ai.memory.contract.model.MemoryPath;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatResponse;
import com.shiyu.ai.model.contract.model.ChatType;
import com.shiyu.ai.tooling.contract.api.ToolService;
import com.shiyu.ai.tooling.contract.api.ToolService.ToolExecutionContext;

import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 智能体 Node Execution 相关功能、边界条件、异常路径和协作行为。
 */
class AgentNodeExecutionTest {

    private static final ActorContext TRUSTED_ACTOR =
            new ActorContext(new TenantId(1L), new UserId(2L), false);

    @Test
    void llmRequestUsesTrustedActorAfterEarlierNodeChangesScalarIdentity() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        when(engine.chat(any())).thenReturn(ChatResponse.builder().success(true)
                .content("ok").platform("p").model("m").build());
        LlmCallNode node = LlmCallNode.builder().chatEngine(engine).build();
        Map<String, Object> forged = Map.of(
                NodeActorContext.KEY, TRUSTED_ACTOR,
                "tenantId", 99L, "userId", 88L, "query", "hello");

        assertTrue(invoke(node, NodeInput.fromMap(forged)).isSuccess());

        verify(engine).chat(argThat(request -> request.getTenantId() == 1L
                && request.getUserId() == 2L));
    }

    @Test
    void toolPipelineUsesTrustedActorAndRejectsForgedRunOwner() throws Exception {
        ToolService tools = mock(ToolService.class);
        ToolExecutionPipeline pipeline = mock(ToolExecutionPipeline.class);
        ToolCallNode node = ToolCallNode.builder().toolService(tools)
                .executionPipeline(pipeline)
                .config(ToolCallConfig.builder().toolName("search").build()).build();
        when(pipeline.executeByIdWithContext(any(), any(), anyLong(), any(), any()))
                .thenReturn(new ToolExecutionPipeline.Result(
                        ToolExecutionPipeline.Result.Status.COMPLETED, "ok", null));
        Map<String, Object> forged = Map.of(
                NodeActorContext.KEY, TRUSTED_ACTOR,
                "tenantId", 99L, "userId", 88L, "__aiRunId", "run-1");

        assertTrue(invoke(node, NodeInput.fromMap(forged)).isSuccess());
        verify(pipeline).executeByIdWithContext(
                eq("run-1"), eq(new TenantId(1L)), eq(2L), any(), any());

        assertFalse(invoke(node, NodeInput.fromMap(Map.of(
                "tenantId", 1L, "userId", 2L, "__aiRunId", "run-2"))).isSuccess());
        verify(pipeline, never()).executeById(eq("run-2"), any(), anyLong(), any(), any());

        AiRun foreign = new AiRuntimeService().startRun(
                new AiRunContext(new TenantId(99L), 88L, null, null, null, null, null, null, Map.of()),
                AiRunSource.AGENT, "agent", "model", "prompt");
        assertFalse(invoke(node, NodeInput.fromMap(Map.of(
                NodeActorContext.KEY, TRUSTED_ACTOR, "_aiRun", foreign))).isSuccess());
        verify(pipeline, never()).execute(eq(foreign), any(), any());
    }

    @Test
    void approvedToolCallPassesApprovalIdToContextAwareAdapter() throws Exception {
        AtomicReference<ToolExecutionContext> received = new AtomicReference<>();
        ToolService tools = new ToolService() {
            @Override
            public ToolExecutionResult execute(String toolName, Map<String, Object> parameters) {
                return new ToolExecutionResult(false, null, "execution context was not passed");
            }

            @Override
            public ToolExecutionResult execute(
                    String toolName,
                    Map<String, Object> parameters,
                    ToolExecutionContext context) {
                received.set(context);
                return new ToolExecutionResult(true, "done", null);
            }

            @Override
            public boolean requiresApproval(String toolName) {
                return true;
            }
        };
        AiRuntimeService runtime = new AiRuntimeService(
                new InMemoryAiRunRepository(), new InMemoryAiAppRepository());
        ToolApprovalService approvals =
                new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolExecutionPipeline pipeline = new ToolExecutionPipeline(runtime, approvals);
        AiRun run = runtime.startRun(
                new AiRunContext(new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                AiRunSource.AGENT, "agent-1", "model", "write");
        ToolCallNode node = ToolCallNode.builder()
                .toolService(tools)
                .executionPipeline(pipeline)
                .config(ToolCallConfig.builder().toolName("external.write").build())
                .build();

        NodeApprovalRequiredException pending = assertThrows(NodeApprovalRequiredException.class,
                () -> invoke(node, trustedInput(Map.of("_aiRun", run, "path", "a"))));
        approvals.decide(pending.approvalId(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);

        NodeOutput output = invoke(node, trustedInput(Map.of(
                "_aiRun", run, "_toolApprovalId", pending.approvalId(), "path", "a")));

        assertTrue(output.isSuccess());
        assertNotNull(received.get());
        assertEquals(pending.approvalId(), received.get().operationId());
    }

    @Test
    void llmNodeHandlesSyncStreamTemplatesMessagesAndInvalidContext() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        LlmCallNode sync =
                LlmCallNode.builder()
                        .chatEngine(engine)
                        .config(
                                LlmCallConfig.builder()
                                        .nodeName("sync")
                                        .promptTemplate("hello {name}")
                                        .build())
                        .build();
        when(engine.chat(any()))
                .thenReturn(
                        ChatResponse.builder()
                                .success(true)
                                .content("answer")
                                .platform("p")
                                .model("m")
                                .build());
        NodeOutput result =
                invoke(
                        sync,
                        trustedInput(Map.of("name", "world")));
        assertTrue(result.isSuccess());
        assertEquals("answer", result.getData("content", null));
        verify(engine).chat(any());

        when(engine.chat(any()))
                .thenReturn(ChatResponse.builder().success(false).errorMessage("down").build());
        NodeOutput failed =
                invoke(sync, trustedInput(Map.of("query", "q")));
        assertFalse(failed.isSuccess());
        assertEquals("LLM 调用失败，请稍后重试", failed.getMsg());
        assertFalse(failed.getMsg().contains("down"));

        LlmCallNode stream =
                LlmCallNode.builder()
                        .chatEngine(engine)
                        .config(LlmCallConfig.builder().nodeName("stream").stream(true).build())
                        .build();
        when(engine.stream(any()))
                .thenReturn(
                        Flux.just(
                                ChatResponse.builder()
                                        .eventType("REASONING_DELTA")
                                        .reasoningContent("think")
                                        .build(),
                                ChatResponse.builder()
                                        .eventType("DELTA")
                                        .content("hello")
                                        .build()));
        NodeOutput streamed =
                invoke(
                        stream,
                        trustedInput(Map.of("chatType", ChatType.STREAM)));
        assertTrue(streamed.isSuccess());
        assertTrue(streamed.getData("stream", false));
        assertNotNull(streamed.getData("_streaming_generator", null));

        NodeOutput invalid = invoke(sync, NodeInput.fromMap(Map.of("query", "q")));
        assertFalse(invalid.isSuccess());
        assertEquals("LLM 调用节点执行失败，请稍后重试", invalid.getMsg());
        verify(engine, times(2)).chat(any());
    }

    @Test
    void conditionNodeEvaluatesExpressionScriptIntentAndFallback() throws Exception {
        ConditionNode node =
                ConditionNode.builder()
                        .config(
                                ConditionConfig.builder()
                                        .conditionExpression("enabled")
                                        .trueBranch("yes")
                                        .defaultBranch("no")
                                        .build())
                        .build();
        NodeOutput expression = invoke(node, NodeInput.fromMap(Map.of("enabled", true)));
        assertTrue(expression.isSuccess());
        assertEquals("yes", expression.getData("nextNode", null));

        NodeOutput comparison =
                invoke(
                        node,
                        NodeInput.fromMap(
                                Map.of("conditionExpression", "score == 10", "score", 10)));
        assertTrue(comparison.getData("conditionResult", false));

        NodeOutput script =
                invoke(
                        node,
                        NodeInput.fromMap(
                                Map.of(
                                        "conditionType",
                                        "SCRIPT",
                                        "conditionExpression",
                                        "#score > 0.5",
                                        "score",
                                        0.8)));
        assertTrue(script.getData("conditionResult", false));

        NodeOutput intent =
                invoke(
                        node,
                        NodeInput.fromMap(
                                Map.of(
                                        "conditionType",
                                        "INTENT",
                                        "conditionExpression",
                                        "weather",
                                        "intentCode",
                                        "weather")));
        assertTrue(intent.getData("conditionResult", false));

        NodeOutput fallback =
                invoke(
                        node,
                        NodeInput.fromMap(
                                Map.of(
                                        "conditionType",
                                        "SCRIPT",
                                        "conditionExpression",
                                        "#broken[",
                                        "defaultBranch",
                                        "fallback")));
        assertTrue(fallback.isSuccess());
        assertEquals("fallback", fallback.getData("nextNode", null));
    }

    @Test
    void toolNodeMapsSlotsDefaultsAndPropagatesToolFailures() throws Exception {
        ToolService tools = mock(ToolService.class);
        ToolCallNode node =
                ToolCallNode.builder()
                        .toolService(tools)
                        .config(
                                ToolCallConfig.builder()
                                        .toolName("search")
                                        .toolType("NORMAL")
                                        .build())
                        .build();
        when(tools.execute(eq("search"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(true, "ok", null));
        NodeOutput result =
                invoke(
                        node,
                        NodeInput.fromMap(
                                Map.of(
                                        "slots", Map.of("q", "java"),
                                        "parameterMapping", Map.of("q", "query"),
                                        "slotDefaults", Map.of("limit", "5"),
                                        "slotDefinitions", Map.of("q", "string", "limit", "number"),
                                        "_internal", "ignored")));
        assertTrue(result.isSuccess());
        assertEquals("ok", result.getData("toolResult", null));
        verify(tools)
                .execute(
                        eq("search"),
                        argThat(m -> "java".equals(m.get("query")) && "5".equals(m.get("limit"))));

        when(tools.execute(eq("search"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(false, null, "denied"));
        NodeOutput failed = invoke(node, NodeInput.fromMap(Map.of("q", "x")));
        assertFalse(failed.isSuccess());
        assertEquals("工具调用失败，请稍后重试", failed.getMsg());
        assertFalse(failed.getMsg().contains("denied"));

        ToolCallNode unnamed =
                ToolCallNode.builder()
                        .toolService(tools)
                        .config(ToolCallConfig.builder().toolName(" ").build())
                        .build();
        NodeOutput missing = invoke(unnamed, NodeInput.fromMap(Map.of()));
        assertFalse(missing.isSuccess());
        assertTrue(missing.getMsg().contains("工具名称"));

        Map<String, Object> edgeInput = new LinkedHashMap<>();
        edgeInput.put("slots", "not-a-map");
        edgeInput.put("q", "");
        edgeInput.put("parameterMapping", Map.of("q", "q"));
        edgeInput.put("slotDefaults", Map.of("q", "fallback", "limit", "10"));
        edgeInput.put("slotDefinitions", Map.of("q", "string", "missing", "string"));
        edgeInput.put("toolName", "metadata");
        edgeInput.put("toolType", "metadata");
        edgeInput.put("intentCode", "metadata");
        when(tools.execute(eq("search"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(true, "edge", null));
        NodeOutput edge = invoke(node, NodeInput.fromMap(edgeInput));
        assertTrue(edge.isSuccess());
        verify(tools)
                .execute(
                        eq("search"),
                        argThat(
                                m ->
                                        "".equals(m.get("q"))
                                                && "10".equals(m.get("limit"))
                                                && "not-a-map".equals(m.get("slots"))));

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("toolName", "ignored");
        metadata.put("toolType", "ignored");
        metadata.put("intentCode", "ignored");
        metadata.put("intentName", "ignored");
        metadata.put("confidence", "ignored");
        metadata.put("parameterMapping", Map.of());
        metadata.put("slotDefaults", Map.of("limit", "25"));
        metadata.put("slotDefinitions", Map.of("query", "string", "empty", "string"));
        metadata.put("query", null);
        metadata.put("empty", " ");
        when(tools.execute(eq("search"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(true, "metadata-edge", null));
        NodeOutput metadataEdge = invoke(node, NodeInput.fromMap(metadata));
        assertTrue(metadataEdge.isSuccess());
        verify(tools)
                .execute(
                        eq("search"),
                        argThat(
                                m ->
                                        m.containsKey("query")
                                                && " ".equals(m.get("empty"))
                                                && "25".equals(m.get("limit"))
                                                && !m.containsKey("intentName")
                                                && !m.containsKey("confidence")));
    }

    @Test
    void ragNodesEnforceAccessContextAndCoverEnhancementStrategies() throws Exception {
        ActorContext actor = new ActorContext(new TenantId(1L), new UserId(2L), false);
        KnowledgeRetrievalService retrieval = mock(KnowledgeRetrievalService.class);
        RagRetrievalNode retrievalNode =
                RagRetrievalNode.builder().retrievalService(retrieval).build();
        NodeOutput blank = invoke(retrievalNode, NodeInput.fromMap(Map.of("query", " ")));
        assertFalse(blank.isSuccess());
        assertThrows(
                IllegalStateException.class,
                () -> invoke(retrievalNode, NodeInput.fromMap(Map.of("query", "q"))));

        KnowledgeRetrievalHit hit =
                new KnowledgeRetrievalHit(
                        3L,
                        4L,
                        5L,
                        6L,
                        7L,
                        "Doc",
                        "content",
                        "highlight",
                        8,
                        "§1",
                        0.4,
                        0.6,
                        0.7,
                        0.8);
        KnowledgeCitation citation =
                new KnowledgeCitation("c1", 3L, 4L, 5L, 6L, 7L, "Doc", 8, "§1", "excerpt");
        when(retrieval.retrieve(any()))
                .thenReturn(
                        new KnowledgeRetrievalResult(
                                true, List.of(hit), List.of(citation), "context", null));
        NodeOutput retrieved =
                invoke(
                        retrievalNode,
                        NodeInput.fromMap(Map.of("query", "q", "__knowledgeAccessContext", actor)));
        assertTrue(retrieved.isSuccess());
        assertEquals(1, retrieved.getData("documentCount", 0));
        List<?> documentOutput = retrieved.getData("documents", List.of());
        assertEquals("Doc", ((Map<?, ?>) documentOutput.get(0)).get("title"));

        RagEnhancementNode enhancement =
                RagEnhancementNode.builder()
                        .config(
                                RagEnhancementConfig.builder()
                                        .contextWindowSize(1)
                                        .maxLength(100)
                                        .similarityThreshold(0.5)
                                        .build())
                        .build();
        List<Map<String, Object>> docs =
                List.of(
                        Map.of("content", "low", "score", 0.2),
                        Map.of("content", "high", "score", 0.9));
        NodeOutput summary =
                invoke(enhancement, NodeInput.fromMap(Map.of("documents", docs, "context", "old")));
        assertTrue(summary.isSuccess());
        assertEquals(1, summary.getData("enhanced_count", 0));
        assertTrue(summary.getData("context", "").contains("high"));

        NodeOutput filtered =
                invoke(
                        enhancement,
                        NodeInput.fromMap(
                                Map.of(
                                        "documents",
                                        docs,
                                        "enhancement_strategy",
                                        "FILTER",
                                        "similarityThreshold",
                                        0.8)));
        assertEquals(1, filtered.getData("enhanced_count", 0));
        NodeOutput reranked =
                invoke(
                        enhancement,
                        NodeInput.fromMap(
                                Map.of(
                                        "documents",
                                        docs,
                                        "enhancement_strategy",
                                        "RE_RANK",
                                        "contextWindowSize",
                                        2)));
        assertEquals("RE_RANK", reranked.getData("enhancement_strategy", null));
        NodeOutput empty = invoke(enhancement, NodeInput.fromMap(Map.of("context", "original")));
        assertTrue(empty.isSuccess());
        assertEquals("original", empty.getData("context", null));
        RagEnhancementNode noContextOnEmpty =
                RagEnhancementNode.builder()
                        .config(RagEnhancementConfig.builder().addContext(false).build())
                        .build();
        NodeOutput skippedWithoutContext =
                invoke(
                        noContextOnEmpty,
                        NodeInput.fromMap(Map.of("documents", List.of(), "context", "ignored")));
        assertTrue(skippedWithoutContext.isSuccess());
        assertNull(skippedWithoutContext.getData("context", null));
        Map<String, Object> nullDocuments = new LinkedHashMap<>();
        nullDocuments.put("documents", null);
        nullDocuments.put("addContext", null);
        nullDocuments.put("context", "kept");
        NodeOutput nullDocs = invoke(enhancement, NodeInput.fromMap(nullDocuments));
        assertTrue(nullDocs.isSuccess());
        assertEquals("kept", nullDocs.getData("context", null));

        Map<String, Object> sparseDoc = new LinkedHashMap<>();
        sparseDoc.put("content", null);
        sparseDoc.put("score", "not-a-number");
        Map<String, Object> sparseInput = new LinkedHashMap<>();
        sparseInput.put("documents", List.of(sparseDoc));
        sparseInput.put("enhancementStrategy", null);
        sparseInput.put("contextWindowSize", 0);
        sparseInput.put("maxLength", 10);
        sparseInput.put("addContext", false);
        NodeOutput sparse =
                invoke(
                        RagEnhancementNode.builder()
                                .config(
                                        RagEnhancementConfig.builder()
                                                .enhancementStrategy(null)
                                                .addContext(true)
                                                .build())
                                .build(),
                        NodeInput.fromMap(sparseInput));
        assertTrue(sparse.isSuccess());
        assertFalse(sparse.getData("context", null) != null);

        NodeOutput noContext =
                invoke(
                        enhancement,
                        NodeInput.fromMap(
                                Map.of("documents", docs, "addContext", false, "maxLength", 10)));
        assertTrue(noContext.isSuccess());
        assertNull(noContext.getData("context", null));

        Map<String, Object> longDoc = new LinkedHashMap<>();
        longDoc.put("content", "012345678901234567890123456789");
        longDoc.put("score", "0.75");
        NodeOutput truncated =
                invoke(
                        RagEnhancementNode.builder()
                                .config(
                                        RagEnhancementConfig.builder()
                                                .enhancementStrategy("FILTER")
                                                .maxLength(20)
                                                .similarityThreshold(0.5)
                                                .build())
                                .build(),
                        NodeInput.fromMap(
                                Map.of(
                                        "documents",
                                        List.of(longDoc),
                                        "enhancement_strategy",
                                        "FILTER")));
        assertTrue(truncated.isSuccess());
        assertTrue(truncated.getData("context", "").contains("截断"));

        Map<String, Object> summaryLongDoc = new LinkedHashMap<>();
        summaryLongDoc.put("content", "abcdefghijklmnopqrstuvwxyz");
        summaryLongDoc.put("score", 0.9);
        NodeOutput summaryTruncated =
                invoke(
                        RagEnhancementNode.builder()
                                .config(
                                        RagEnhancementConfig.builder()
                                                .enhancementStrategy("SUMMARIZATION")
                                                .maxLength(20)
                                                .contextWindowSize(1)
                                                .build())
                                .build(),
                        NodeInput.fromMap(
                                Map.of(
                                        "documents",
                                        List.of(summaryLongDoc),
                                        "enhancement_strategy",
                                        "SUMMARIZATION")));
        assertTrue(summaryTruncated.isSuccess());
        assertTrue(summaryTruncated.getData("context", "").contains("截断"));

        NodeOutput malformed =
                invoke(
                        enhancement,
                        NodeInput.fromMap(
                                Map.of("documents", java.util.Collections.singletonList(null))));
        assertFalse(malformed.isSuccess());

        RagEnhancementConfig nullConfig =
                RagEnhancementConfig.builder()
                        .enhancementStrategy(null)
                        .addContext(null)
                        .contextWindowSize(null)
                        .maxLength(null)
                        .similarityThreshold(null)
                        .build();
        NodeOutput fallbackConfig =
                invoke(
                        RagEnhancementNode.builder().config(nullConfig).build(),
                        NodeInput.fromMap(Map.of("documents", docs)));
        assertTrue(fallbackConfig.isSuccess());
    }

    @Test
    void transformAndOutputNodesCoverFormatBranchesAndFallbacks() throws Exception {
        TransformNode upper =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("UPPERCASE").build())
                        .build();
        assertEquals(
                "HELLO",
                invoke(upper, NodeInput.fromMap(Map.of("input", "hello")))
                        .getData("transformedData", null));
        TransformNode json =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("JSON_TO_MAP").build())
                        .build();
        assertEquals(
                "1",
                String.valueOf(
                        ((Map<?, ?>)
                                        invoke(json, NodeInput.fromMap(Map.of("data", "{\"a\":1}")))
                                                .getData("transformedData", Map.of()))
                                .get("a")));
        TransformNode template =
                TransformNode.builder()
                        .config(
                                TransformConfig.builder()
                                        .transformType("TEMPLATE")
                                        .template("value={input}")
                                        .build())
                        .build();
        assertEquals(
                "value=x",
                invoke(template, NodeInput.fromMap(Map.of("text", "x")))
                        .getData("transformedData", null));
        TransformNode unknown =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("UNKNOWN").build())
                        .build();
        assertEquals(
                "raw",
                invoke(unknown, NodeInput.fromMap(Map.of("query", "raw")))
                        .getData("transformedData", null));

        OutputFormatNode markdown =
                OutputFormatNode.builder()
                        .config(OutputFormatConfig.builder().outputFormat("MARKDOWN").build())
                        .build();
        assertTrue(
                invoke(markdown, NodeInput.fromMap(Map.of("content", "answer")))
                        .getData("formattedContent", "")
                        .startsWith("## 回复"));
        OutputFormatNode jsonOutput =
                OutputFormatNode.builder()
                        .config(
                                OutputFormatConfig.builder()
                                        .outputFormat("JSON")
                                        .prettyPrint(true)
                                        .build())
                        .build();
        assertTrue(
                invoke(jsonOutput, NodeInput.fromMap(Map.of("response", "{\"ok\":true}")))
                        .getData("formattedContent", "")
                        .contains("ok"));
        OutputFormatNode xml =
                OutputFormatNode.builder()
                        .config(OutputFormatConfig.builder().outputFormat("XML").build())
                        .build();
        assertTrue(
                invoke(xml, NodeInput.fromMap(Map.of("result", "r")))
                        .getData("formattedContent", "")
                        .contains("<response>"));
        OutputFormatNode templateOutput =
                OutputFormatNode.builder()
                        .config(OutputFormatConfig.builder().template("Result: {content}").build())
                        .build();
        assertEquals(
                "Result: done",
                invoke(templateOutput, NodeInput.fromMap(Map.of("answer", "done")))
                        .getData("formattedContent", null));
    }

    @Test
    void transformAndOutputNodesRejectMalformedDataAndUseAllFallbackFormats() throws Exception {
        TransformNode lower =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("LOWERCASE").build())
                        .build();
        assertEquals(
                "hello",
                invoke(lower, NodeInput.fromMap(Map.of("data", "HELLO")))
                        .getData("transformedData", null));
        TransformNode trim =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("TRIM").build())
                        .build();
        assertEquals(
                "value",
                invoke(trim, NodeInput.fromMap(Map.of("content", "  value  ")))
                        .getData("transformedData", null));
        TransformNode mapJson =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("MAP_TO_JSON").build())
                        .build();
        assertTrue(
                String.valueOf(
                                invoke(mapJson, NodeInput.fromMap(Map.of("query", "{bad")))
                                        .getData("transformedData", ""))
                        .contains("data"));
        TransformNode invalidJson =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType("JSON_TO_MAP").build())
                        .build();
        assertTrue(
                ((Map<?, ?>)
                                invoke(invalidJson, NodeInput.fromMap(Map.of("input", "{bad")))
                                        .getData("transformedData", Map.of()))
                        .isEmpty());
        TransformNode defaultType =
                TransformNode.builder()
                        .config(TransformConfig.builder().transformType(null).build())
                        .build();
        assertEquals(
                "raw",
                invoke(defaultType, NodeInput.fromMap(Map.of("query", "raw")))
                        .getData("transformedData", null));

        for (String format : List.of("HTML", "PLAIN_TEXT", "UNKNOWN")) {
            OutputFormatNode node =
                    OutputFormatNode.builder()
                            .config(OutputFormatConfig.builder().outputFormat(format).build())
                            .build();
            String output =
                    invoke(node, NodeInput.fromMap(Map.of("messages", "  hello   world ")))
                            .getData("formattedContent", "");
            assertFalse(output.isEmpty());
        }
        OutputFormatNode invalidJsonOutput =
                OutputFormatNode.builder()
                        .config(OutputFormatConfig.builder().outputFormat("JSON").build())
                        .build();
        assertTrue(
                invoke(invalidJsonOutput, NodeInput.fromMap(Map.of("content", "\"bad")))
                        .getData("formattedContent", "")
                        .contains("result"));
        OutputFormatNode pretty =
                OutputFormatNode.builder()
                        .config(OutputFormatConfig.builder().prettyPrint(true).build())
                        .build();
        assertEquals(
                "hello world",
                invoke(pretty, NodeInput.fromMap(Map.of("content", " hello   world ")))
                        .getData("formattedContent", null));
    }

    @Test
    void memoryNodesStoreConversationAndRetrieveDurableEvents() throws Exception {
        AgentExecutionContext context = new AgentExecutionContext();
        ShortTermMemoryNode shortTerm =
                ShortTermMemoryNode.builder().memoryService(context).build();
        assertTrue(
                shortTerm.getRequiredInputs().stream().anyMatch(p -> p.name().equals("tenantId")));
        assertTrue(shortTerm.getRequiredInputs().stream().anyMatch(p -> p.name().equals("userId")));
        NodeOutput skipped = invoke(shortTerm, NodeInput.fromMap(Map.of("sessionId", "")));
        assertFalse(skipped.isSuccess());
        assertFalse(invoke(shortTerm, NodeInput.fromMap(Map.of("sessionId", "s1"))).isSuccess());
        assertFalse(
                invoke(shortTerm, NodeInput.fromMap(Map.of("sessionId", "s1", "userId", 0L)))
                        .isSuccess());
        assertFalse(
                invoke(
                                shortTerm,
                                NodeInput.fromMap(
                                        Map.of(
                                                "sessionId",
                                                "s1",
                                                "userId",
                                                2L,
                                                "query",
                                                "hello",
                                                "content",
                                                "hi")))
                        .isSuccess());
        NodeOutput stored =
                invoke(
                        shortTerm,
                        NodeInput.fromMap(
                                Map.of(
                                        "sessionId",
                                        "s1",
                                        "tenantId",
                                        1L,
                                        "userId",
                                        2L,
                                        "query",
                                        "hello",
                                        "content",
                                        "hi")));
        assertTrue(stored.isSuccess());
        assertTrue(stored.getData("conversationHistory", "").contains("user: hello"));
        NodeOutput otherTenant =
                invoke(
                        shortTerm,
                        NodeInput.fromMap(
                                Map.of(
                                        "sessionId",
                                        "s1",
                                        "tenantId",
                                        2L,
                                        "userId",
                                        2L,
                                        "query",
                                        "other",
                                        "content",
                                        "tenant response")));
        assertTrue(otherTenant.isSuccess());
        assertTrue(otherTenant.getData("conversationHistory", "").contains("user: other"));
        assertFalse(otherTenant.getData("conversationHistory", "").contains("user: hello"));

        MemoryIngestionPort ingestion = mock(MemoryIngestionPort.class);
        LongTermMemoryNode longTerm = LongTermMemoryNode.builder().memoryService(ingestion).build();
        assertTrue(invoke(longTerm, NodeInput.fromMap(Map.of("memoryContent", ""))).isSuccess());
        assertFalse(
                invoke(
                                longTerm,
                                NodeInput.fromMap(
                                        Map.of(
                                                "userId",
                                                2L,
                                                "sessionId",
                                                "s1",
                                                "memoryKey",
                                                "preference",
                                                "memoryContent",
                                                "likes math",
                                                "importance",
                                                0.9)))
                        .isSuccess());
        assertTrue(
                invoke(
                                longTerm,
                                NodeInput.fromMap(
                                        Map.of(
                                                "tenantId",
                                                1L,
                                                "userId",
                                                2L,
                                                "sessionId",
                                                "s1",
                                                "memoryKey",
                                                "preference",
                                                "memoryContent",
                                                "likes math",
                                                "importance",
                                                0.9)))
                        .isSuccess());
        verify(ingestion).ingest(any());

        MemoryQueryPort query = mock(MemoryQueryPort.class);
        MemoryEvent event =
                new MemoryEvent(
                        "e1",
                        new com.shiyu.ai.kernel.context.TenantId(1L),
                        "agent",
                        "USER",
                        "2",
                        "FACT",
                        "likes math",
                        java.time.Instant.now(),
                        "AGENT",
                        "s1",
                        Map.of(),
                        0.8,
                        0.9,
                        MemoryEventStatus.ACTIVE,
                        ConfirmationPolicy.REQUIRED,
                        java.time.Instant.now(),
                        java.time.Instant.now());
        when(query.retrieve(any())).thenReturn(List.of(new MemoryPath(event, 0.95, List.of())));
        com.shiyu.ai.agent.implementation.node.memory.MemoryRetrievalNode retrieval =
                com.shiyu.ai.agent.implementation.node.memory.MemoryRetrievalNode.builder()
                        .memoryService(query)
                        .build();
        assertFalse(
                invoke(retrieval, NodeInput.fromMap(Map.of("query", "math", "userId", 2L)))
                        .isSuccess());
        NodeOutput memories =
                invoke(
                        retrieval,
                        NodeInput.fromMap(Map.of("query", "math", "tenantId", 1L, "userId", 2L)));
        assertTrue(memories.isSuccess());
        assertEquals(1, memories.getData("memoryCount", 0));
        assertEquals("likes math", memories.getData("memoryContext", null));
    }

    @Test
    void agentCallAndIntentNodesMapInputsAndHandleRecognitionBranches() throws Exception {
        AgentRuntime runtime = mock(AgentRuntime.class);
        ActorContext authenticated =
                new ActorContext(new TenantId(1L), new UserId(2L), false);
        AgentCallConfig config = new AgentCallConfig();
        config.setTargetAgentId("child");
        config.setTargetVersion("v1");
        config.setInputMapping(Map.of("query", "prompt"));
        config.setOutputKey("childOutput");
        AgentCallNode call = AgentCallNode.builder().config(config).agentRuntime(runtime).build();
        Execution execution = new Execution("child", "v1", Map.of());
        execution.start();
        execution.complete(Map.of("answer", "ok"));
        when(runtime.execute(any(), eq("child"), eq("v1"), any())).thenReturn(execution);
        NodeOutput called =
                invoke(
                        call,
                        NodeInput.fromMap(Map.of("query", "hello", "tenantId", 1L, "userId", 2L,
                                "__knowledgeAccessContext", authenticated)));
        assertTrue(called.isSuccess());
        assertEquals(Map.of("answer", "ok"), called.getData("childOutput", Map.of()));
        verify(runtime)
                .execute(
                        any(),
                        eq("child"),
                        eq("v1"),
                        argThat(m -> "hello".equals(m.get("prompt"))));
        assertFalse(
                invoke(call, NodeInput.fromMap(Map.of("query", "hello", "tenantId", 1L)))
                        .isSuccess());
        assertFalse(
                invoke(call, NodeInput.fromMap(Map.of("query", "hello", "tenantId", 1L,
                        "userId", 2L))).isSuccess());

        AgentCallConfig passthroughConfig = new AgentCallConfig();
        passthroughConfig.setTargetAgentId("child");
        passthroughConfig.setTargetVersion(" ");
        AgentCallNode passthrough =
                AgentCallNode.builder().agentRuntime(runtime).config(passthroughConfig).build();
        when(runtime.execute(any(), eq("child"), any())).thenReturn(execution);
        Map<String, Object> passthroughInput = new LinkedHashMap<>();
        passthroughInput.put("targetAgentId", "child");
        passthroughInput.put("targetVersion", " ");
        passthroughInput.put("query", "hello");
        passthroughInput.put("tenantId", 1L);
        passthroughInput.put("userId", 2L);
        passthroughInput.put("__knowledgeAccessContext", authenticated);
        AgentCallConfig sparseConfig = new AgentCallConfig();
        sparseConfig.setTargetAgentId("child");
        sparseConfig.setInputMapping(Map.of("missing", "childMissing", "query", "prompt"));
        AgentCallNode sparseCall =
                AgentCallNode.builder().config(sparseConfig).agentRuntime(runtime).build();
        NodeOutput sparseCalled = invoke(sparseCall, NodeInput.fromMap(passthroughInput));
        assertTrue(sparseCalled.isSuccess());
        verify(runtime)
                .execute(
                        any(),
                        eq("child"),
                        argThat(
                                m ->
                                        "hello".equals(m.get("prompt"))
                                                && !m.containsKey("childMissing")));
        assertTrue(invoke(passthrough, NodeInput.fromMap(passthroughInput)).isSuccess());

        IntentService intents = mock(IntentService.class);
        IntentNode intent =
                IntentNode.builder()
                        .intentService(intents)
                        .config(IntentConfig.builder().category("support").build())
                        .build();
        assertDoesNotThrow(() -> IntentDefinitionFactory.getAll("missing-agent"));
        assertFalse(invoke(intent, NodeInput.fromMap(Map.of())).isSuccess());
        when(intents.recognize(eq("default"), eq("support"), eq("help"), any(), any()))
                .thenReturn(
                        new IntentService.IntentRecognitionResult(
                                true, "HELP", "Help", 0.95, Map.of("topic", "login"), null));
        IntentDefBO helpDefinition = new IntentDefBO();
        helpDefinition.setAgentId("default");
        helpDefinition.setCategory("support");
        helpDefinition.setCode("HELP");
        helpDefinition.setName("Help");
        helpDefinition.setParameterMapping(Map.of("topic", "query"));
        helpDefinition.setSlotDefaults(Map.of("language", "zh"));
        helpDefinition.setSlots(Map.of("topic", "topic text"));
        helpDefinition.setEnabled(true);
        IntentDefinitionFactory.reloadFromDb(List.of(helpDefinition));
        NodeOutput recognized = invoke(intent, NodeInput.fromMap(Map.of("query", "help")));
        assertTrue(recognized.isSuccess());
        assertEquals("HELP", recognized.getData("intentCode", null));
        assertEquals(Map.of("topic", "query"), recognized.getData("parameterMapping", Map.of()));
        assertEquals(Map.of("language", "zh"), recognized.getData("slotDefaults", Map.of()));
        assertEquals(
                Map.of("topic", "topic text"), recognized.getData("slotDefinitions", Map.of()));
        when(intents.recognize(any(), any(), any(), any(), any()))
                .thenReturn(
                        new IntentService.IntentRecognitionResult(
                                false, "UNKNOWN", null, 0.1, Map.of(), "no match"));
        assertFalse(invoke(intent, NodeInput.fromMap(Map.of("query", "???"))).isSuccess());
        when(intents.recognize(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("recognizer down"));
        NodeOutput recognitionError = invoke(intent, NodeInput.fromMap(Map.of("query", "error")));
        assertFalse(recognitionError.isSuccess());
        assertEquals("ERROR", recognitionError.getData("intentCode", null));
        assertEquals("意图识别失败，请稍后重试", recognitionError.getMsg());
        assertFalse(recognitionError.getMsg().contains("recognizer down"));

        IntentService noCodeService = mock(IntentService.class);
        IntentNode defaultConfigIntent = IntentNode.builder().intentService(noCodeService).build();
        when(noCodeService.recognize(any(), any(), any(), any(), any()))
                .thenReturn(
                        new IntentService.IntentRecognitionResult(
                                true, null, null, null, null, null));
        NodeOutput noCode =
                invoke(defaultConfigIntent, NodeInput.fromMap(Map.of("query", "hello")));
        assertTrue(noCode.isSuccess());
        assertNull(noCode.getData("intentCode", null));

        IntentDefBO emptyDefinition = new IntentDefBO();
        emptyDefinition.setAgentId("default");
        emptyDefinition.setCode("EMPTY");
        emptyDefinition.setName("");
        emptyDefinition.setEnabled(true);
        IntentDefinitionFactory.reloadFromDb(List.of(emptyDefinition));
        when(noCodeService.recognize(any(), any(), any(), any(), any()))
                .thenReturn(
                        new IntentService.IntentRecognitionResult(
                                true, "EMPTY", "", 0.5, Map.of(), null));
        NodeOutput emptyDefinitionOutput =
                invoke(
                        defaultConfigIntent,
                        NodeInput.fromMap(Map.of("query", "empty", "agentId", "default")));
        assertTrue(emptyDefinitionOutput.isSuccess());
        assertEquals("EMPTY", emptyDefinitionOutput.getData("intentCode", null));
        IntentDefinitionFactory.reloadFromDb(List.of());
    }

    @Test
    void toolNodeCoversPipelineApprovalCompletionAndBuilderValidation() throws Exception {
        ToolService tools = mock(ToolService.class);
        assertThrows(IllegalStateException.class, () -> ToolCallNode.builder().build());
        AiRuntimeService runtime = new AiRuntimeService();
        AiRun run =
                runtime.startRun(
                        new AiRunContext(
                                new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                        AiRunSource.AGENT,
                        "agent",
                        "model",
                        "prompt");
        ToolExecutionPipeline pipeline = mock(ToolExecutionPipeline.class);
        com.shiyu.ai.agent.implementation.runtime.model.ToolApproval approval =
                mock(com.shiyu.ai.agent.implementation.runtime.model.ToolApproval.class);
        when(approval.id()).thenReturn("approval-1");
        when(pipeline.executeWithContext(eq(run), any(), any()))
                .thenReturn(
                        new ToolExecutionPipeline.Result(
                                ToolExecutionPipeline.Result.Status.APPROVAL_REQUIRED,
                                null,
                                approval));
        ToolCallNode node =
                ToolCallNode.builder()
                        .toolService(tools)
                        .executionPipeline(pipeline)
                        .config(
                                ToolCallConfig.builder()
                                        .toolName("search")
                                        .toolType("HIGH")
                                        .build())
                        .build();
        NodeApprovalRequiredException pending = assertThrows(NodeApprovalRequiredException.class,
                () -> invoke(node, trustedInput(Map.of("_aiRun", run, "q", "x"))));
        assertEquals("approval-1", pending.approvalId());

        when(pipeline.executeWithContext(eq(run), any(), any()))
                .thenReturn(
                        new ToolExecutionPipeline.Result(
                                ToolExecutionPipeline.Result.Status.COMPLETED, "answer", approval));
        NodeOutput completed = invoke(node, trustedInput(Map.of("_aiRun", run, "q", "x")));
        assertTrue(completed.isSuccess());
        assertEquals("answer", completed.getData("toolResult", null));

        when(pipeline.executeByIdWithContext(
                eq(run.id()), eq(new TenantId(1L)), eq(2L), any(), any()))
                .thenReturn(
                        new ToolExecutionPipeline.Result(
                                ToolExecutionPipeline.Result.Status.COMPLETED, "by-id", approval));
        NodeOutput byId =
                invoke(
                        node,
                        trustedInput(Map.of("__aiRunId", run.id())));
        assertTrue(byId.isSuccess());
        assertEquals("by-id", byId.getData("toolResult", null));
    }

    @Test
    void highRiskToolFailsClosedWithoutTrackedRuntimeOrApprovalPipeline() throws Exception {
        ToolService tools = mock(ToolService.class);
        ToolCallNode withoutRun = ToolCallNode.builder()
                .toolService(tools)
                .executionPipeline(mock(ToolExecutionPipeline.class))
                .config(ToolCallConfig.builder().toolName("filesystem.write")
                        .toolType("HIGH").build())
                .build();
        ToolCallNode withoutPipeline = ToolCallNode.builder()
                .toolService(tools)
                .config(ToolCallConfig.builder().toolName("filesystem.write")
                        .toolType("HIGH_RISK").build())
                .build();

        assertFalse(invoke(withoutRun, NodeInput.fromMap(Map.of("path", "a"))).isSuccess());
        assertFalse(invoke(withoutPipeline,
                trustedInput(Map.of("__aiRunId", "run-1", "path", "a"))).isSuccess());
        verifyNoInteractions(tools);
    }

    @Test
    void externalToolCannotBypassApprovalByDeclaringNormalNodeType() throws Exception {
        ToolService tools = mock(ToolService.class);
        when(tools.requiresApproval("external.write")).thenReturn(true);
        when(tools.execute(eq("external.write"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(true, "written", null));
        ToolCallNode node = ToolCallNode.builder()
                .toolService(tools)
                .config(ToolCallConfig.builder().toolName("external.write")
                        .toolType("NORMAL").build())
                .build();

        assertFalse(invoke(node, trustedInput(Map.of("path", "a"))).isSuccess());
        verify(tools, never()).execute(eq("external.write"), any());

        AiRun run = new AiRuntimeService().startRun(
                new AiRunContext(new TenantId(1), 2, null, null, null, null, null, null, Map.of()),
                AiRunSource.AGENT, "agent", "model", "prompt");
        ToolExecutionPipeline pipeline = mock(ToolExecutionPipeline.class);
        var approval = mock(com.shiyu.ai.agent.implementation.runtime.model.ToolApproval.class);
        when(approval.id()).thenReturn("approval-1");
        when(pipeline.executeWithContext(
                eq(run), argThat(request -> request.highRisk()), any()))
                .thenReturn(new ToolExecutionPipeline.Result(
                        ToolExecutionPipeline.Result.Status.APPROVAL_REQUIRED,
                        null, approval));
        ToolCallNode tracked = ToolCallNode.builder()
                .toolService(tools).executionPipeline(pipeline)
                .config(ToolCallConfig.builder().toolName("external.write")
                        .toolType("NORMAL").build())
                .build();

        assertThrows(NodeApprovalRequiredException.class,
                () -> invoke(tracked, trustedInput(Map.of("_aiRun", run, "path", "a"))));
        verify(tools, never()).execute(eq("external.write"), any());
    }

    @Test
    void llmNodeUsesConfigFallbacksAndMapsMessageVariants() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        LlmCallNode node =
                LlmCallNode.builder()
                        .chatEngine(engine)
                        .config(
                                LlmCallConfig.builder()
                                        .platform("OPENAI")
                                        .modelName("gpt")
                                        .defaultPrompt("default")
                                        .build())
                        .build();
        when(engine.chat(any()))
                .thenReturn(
                        ChatResponse.builder()
                                .success(true)
                                .content("ok")
                                .platform("OPENAI")
                                .model("gpt")
                                .build());
        NodeOutput result =
                invoke(
                        node,
                        trustedInput(
                                Map.of(
                                        "chatType",
                                        "unknown",
                                        "tenantId",
                                        "1",
                                        "userId",
                                        2L,
                                        "messages",
                                        List.of(Map.of("role", "system", "content", "rules")),
                                        "query",
                                        "ignored")));
        assertTrue(result.isSuccess());
        verify(engine)
                .chat(
                        argThat(
                                request ->
                                        "OPENAI".equals(request.getPlatform())
                                                && "gpt".equals(request.getModel())));

        LlmCallNode template =
                LlmCallNode.builder()
                        .chatEngine(engine)
                        .config(LlmCallConfig.builder().promptTemplate("hello {name}").build())
                        .build();
        invoke(template, trustedInput(Map.of("name", "Ada")));
        LlmCallNode.builder()
                .chatEngine(engine)
                .config(LlmCallConfig.builder().stream(true).build())
                .build();
        when(engine.stream(any())).thenReturn(Flux.error(new IllegalStateException("stream down")));
        NodeOutput streamFailure =
                invoke(
                        LlmCallNode.builder()
                                .chatEngine(engine)
                                .config(LlmCallConfig.builder().stream(true).build())
                                .build(),
                        trustedInput(Map.of()));
        assertTrue(streamFailure.isSuccess());
    }

    @Test
    void llmNodeCoversSyncFailureValidationAndStructuredStreamEvents() throws Exception {
        ChatEngine engine = mock(ChatEngine.class);
        assertThrows(IllegalStateException.class, () -> LlmCallNode.builder().build());
        LlmCallNode node =
                LlmCallNode.builder().chatEngine(engine).config(new LlmCallConfig()).build();
        when(engine.chat(any()))
                .thenReturn(
                        ChatResponse.builder()
                                .success(false)
                                .errorMessage("provider rejected")
                                .build());
        NodeOutput failure = invoke(node, trustedInput(Map.of()));
        assertFalse(failure.isSuccess());
        assertEquals("LLM 调用失败，请稍后重试", failure.getMsg());
        assertFalse(failure.getMsg().contains("provider rejected"));
        NodeOutput invalidTenant =
                invoke(node, NodeInput.fromMap(Map.of("tenantId", 0L, "userId", 2L)));
        assertFalse(invalidTenant.isSuccess());
        assertEquals("LLM 调用节点执行失败，请稍后重试", invalidTenant.getMsg());

        LlmCallNode stream =
                LlmCallNode.builder()
                        .chatEngine(engine)
                        .config(LlmCallConfig.builder().stream(true).build())
                        .build();
        when(engine.stream(any()))
                .thenReturn(
                        Flux.just(
                                ChatResponse.builder()
                                        .eventType("REASONING_DELTA")
                                        .reasoningContent("think")
                                        .build(),
                                ChatResponse.builder().eventType("DELTA").content("answer").build(),
                                ChatResponse.builder().eventType("DELTA").content(null).build(),
                                ChatResponse.builder()
                                        .eventType("OTHER")
                                        .content("ignored")
                                        .build()));
        NodeOutput streamed =
                invoke(
                        stream,
                        trustedInput(
                                Map.of(
                                        "tenantId",
                                        "1",
                                        "userId",
                                        "2",
                                        "query",
                                        "hello",
                                        "messages",
                                        List.of(
                                                com.shiyu.ai.model.contract.model.ChatMessage.text(
                                                        "user", "hi"),
                                                "ignored"))));
        assertTrue(streamed.isSuccess());
        assertEquals(Boolean.TRUE, streamed.getData(FieldKey.STREAM, null));
        assertNotNull(streamed.getData(FieldKey.STREAMING_GENERATOR, null));
    }

    private static NodeInput trustedInput(Map<String, Object> data) {
        return NodeInput.fromMap(NodeActorContext.bind(TRUSTED_ACTOR, data));
    }

    @Test
    void toolNodeNormalizesNonSerializableResultBeforeGraphCheckpoint() throws Exception {
        /** 模拟不实现序列化接口的工具返回值。 */
        record ToolPayload(String answer) {}
        ToolService tools = mock(ToolService.class);
        ToolCallNode node = ToolCallNode.builder().toolService(tools)
                .config(ToolCallConfig.builder().toolName("search").build()).build();
        when(tools.execute(eq("search"), any()))
                .thenReturn(new ToolService.ToolExecutionResult(
                        true, new ToolPayload("found"), null));

        NodeOutput output = invoke(node, NodeInput.fromMap(Map.of("query", "example")));

        assertTrue(output.isSuccess());
        assertEquals(Map.of("answer", "found"), output.getData("toolResult", null));
    }

    @Test
    void nestedAgentFailureAndCancellationDoNotBecomeSuccessfulNodeResults() throws Exception {
        AgentRuntime runtime = mock(AgentRuntime.class);
        AgentCallConfig config = new AgentCallConfig();
        config.setTargetAgentId("child");
        AgentCallNode node = AgentCallNode.builder().config(config).agentRuntime(runtime).build();
        Execution failed = new Execution("child", "v1", Map.of());
        failed.start();
        failed.fail("private child error");
        Execution cancelled = new Execution("child", "v1", Map.of());
        cancelled.start();
        cancelled.cancel();
        when(runtime.execute(eq(TRUSTED_ACTOR), eq("child"), any()))
                .thenReturn(failed, cancelled);

        NodeOutput failedOutput = invoke(node, trustedInput(Map.of()));
        NodeOutput cancelledOutput = invoke(node, trustedInput(Map.of()));

        assertFalse(failedOutput.isSuccess());
        assertFalse(cancelledOutput.isSuccess());
        assertNull(failedOutput.getData("agentResult", null));
        assertFalse(failedOutput.getMsg().contains("private child error"));
    }

    @Test
    void configuredChildAgentAndVersionCannotBeRedirectedByInput() throws Exception {
        AgentRuntime runtime = mock(AgentRuntime.class);
        AgentCallConfig config = new AgentCallConfig();
        config.setTargetAgentId("allowed-child");
        config.setTargetVersion("v1");
        AgentCallNode node = AgentCallNode.builder().config(config).agentRuntime(runtime).build();
        Execution completed = new Execution("allowed-child", "v1", Map.of());
        completed.start();
        completed.complete(Map.of("answer", "ok"));
        when(runtime.execute(eq(TRUSTED_ACTOR), eq("allowed-child"), eq("v1"), any()))
                .thenReturn(completed);

        NodeOutput output = invoke(node, trustedInput(Map.of(
                FieldKey.TARGET_AGENT_ID.key(), "unapproved-child",
                FieldKey.TARGET_VERSION.key(), "v99",
                "query", "hello")));

        assertTrue(output.isSuccess());
        assertEquals("allowed-child", output.getData(FieldKey.TARGET_AGENT_ID.key(), null));
        assertEquals("v1", output.getData(FieldKey.TARGET_VERSION.key(), null));
        verify(runtime).execute(eq(TRUSTED_ACTOR), eq("allowed-child"), eq("v1"), any());
        verify(runtime, never()).execute(eq(TRUSTED_ACTOR), eq("unapproved-child"),
                anyString(), any());
    }

    @Test
    void dynamicChildTargetIsAvailableOnlyWhenTheNodeDoesNotConfigureOne() throws Exception {
        AgentRuntime runtime = mock(AgentRuntime.class);
        AgentCallNode node = AgentCallNode.builder().config(new AgentCallConfig())
                .agentRuntime(runtime).build();
        Execution completed = new Execution("selected-child", "v1", Map.of());
        completed.start();
        completed.complete(Map.of("answer", "ok"));
        when(runtime.execute(eq(TRUSTED_ACTOR), eq("selected-child"), eq("v1"), any()))
                .thenReturn(completed);

        NodeOutput output = invoke(node, trustedInput(Map.of(
                FieldKey.TARGET_AGENT_ID.key(), "selected-child",
                FieldKey.TARGET_VERSION.key(), "v1", "query", "hello")));

        assertTrue(output.isSuccess());
        verify(runtime).execute(eq(TRUSTED_ACTOR), eq("selected-child"), eq("v1"),
                argThat(childInput -> "hello".equals(childInput.get("query"))
                        && !childInput.containsKey(FieldKey.TARGET_AGENT_ID.key())
                        && !childInput.containsKey(FieldKey.TARGET_VERSION.key())));
    }

    @Test
    void childCallWithoutATargetOrSuccessfulExecutionNeverReportsSuccess() throws Exception {
        assertThrows(IllegalStateException.class, () -> AgentCallNode.builder().build());
        AgentRuntime runtime = mock(AgentRuntime.class);
        AgentCallNode noTarget = AgentCallNode.builder().agentRuntime(runtime).build();
        NodeOutput missingTarget = invoke(noTarget, trustedInput(Map.of()));
        assertFalse(missingTarget.isSuccess());
        NodeOutput blankTarget = invoke(noTarget, trustedInput(Map.of(
                FieldKey.TARGET_AGENT_ID.key(), "   ")));
        assertFalse(blankTarget.isSuccess());
        verifyNoInteractions(runtime);

        AgentCallConfig config = new AgentCallConfig();
        config.setTargetAgentId("child");
        AgentCallNode call = AgentCallNode.builder().config(config).agentRuntime(runtime).build();
        when(runtime.execute(eq(TRUSTED_ACTOR), eq("child"), any()))
                .thenReturn(null)
                .thenThrow(new IllegalStateException("private child detail"));

        assertFalse(invoke(call, trustedInput(Map.of())).isSuccess());
        NodeOutput failed = invoke(call, trustedInput(Map.of()));
        assertFalse(failed.isSuccess());
        assertFalse(failed.getMsg().contains("private child detail"));
    }

    private static NodeOutput invoke(Object node, NodeInput input) throws Exception {
        Method method = node.getClass().getDeclaredMethod("doExecute", NodeInput.class);
        method.setAccessible(true);
        try {
            return (NodeOutput) method.invoke(node, input);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof Exception checked) throw checked;
            if (cause instanceof Error error) throw error;
            throw exception;
        }
    }
}
