package com.shiyu.ai.agent.implementation.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import com.shiyu.ai.agent.contract.node.NodeActorContext;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.node.agent.AgentCallConfig;
import com.shiyu.ai.agent.implementation.node.agent.AgentCallNode;
import com.shiyu.ai.agent.implementation.node.llm.LlmCallNode;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallConfig;
import com.shiyu.ai.agent.implementation.node.tool.ToolCallNode;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntime;
import com.shiyu.ai.agent.implementation.runtime.service.ToolExecutionPipeline;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import com.shiyu.ai.model.contract.api.ChatEngine;
import com.shiyu.ai.model.contract.model.ChatResponse;
import com.shiyu.ai.tooling.contract.api.ToolService;
import static org.mockito.ArgumentMatchers.argThat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证 Graph Execution Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class GraphExecutionCoverageTest {

    @Test
    void compilesCachesRecompilesAndExecutesSyncAndStream() throws Exception {
        Graph graph =
                Graph.builder()
                        .name("execution-coverage")
                        .startNode("start")
                        .endNode("finish")
                        .nodes(Map.of("start", node("start"), "finish", node("finish")))
                        .edges(Map.of("start", List.of("finish")))
                        .conditionalEdges(Map.of())
                        .build();

        assertFalse(graph.isCompiled());
        var first = graph.compile();
        assertTrue(graph.isCompiled());
        assertEquals(first, graph.compile());
        assertEquals("finish", graph.execute(Map.of()).get("node"));
        assertNotNull(graph.executeStream(Map.of()).collectList().block());
        assertNotNull(graph.stream(Map.of()));
        assertNotNull(graph.recompile());
        assertTrue(graph.validateGraph());
    }

    @Test
    void reportsUnreachableNodesWithoutTreatingConditionalEdgesAsCycles() {
        Graph graph =
                Graph.builder()
                        .startNode("start")
                        .nodes(Map.of("start", node("start"), "orphan", node("orphan")))
                        .edges(Map.of())
                        .conditionalEdges(Map.of())
                        .build();
        assertTrue(graph.findUnreachableNodes().contains("orphan"));
        assertEquals(null, graph.detectCycle());
    }

    @Test
    void invalidatesCompiledGraphWhenStructureChanges() throws Exception {
        Graph graph =
                Graph.builder()
                        .startNode("start")
                        .endNode("finish")
                        .nodes(Map.of("start", node("start"), "finish", node("finish")))
                        .edges(Map.of("start", List.of("finish")))
                        .conditionalEdges(Map.of())
                        .build();

        var first = graph.compile();
        graph.addNode("extra", node("extra")).addEdge("finish", "extra").setEndNode("extra");

        var second = graph.compile();

        assertNotSame(first, second);
        assertEquals("extra", graph.execute(Map.of()).get("node"));
    }

    @Test
    void copiesEdgeTargetListsWhenConfigured() {
        List<String> targets = new ArrayList<>(List.of("finish"));
        Graph graph =
                Graph.builder()
                        .startNode("start")
                        .nodes(Map.of("start", node("start"), "finish", node("finish")))
                        .edges(Map.of("start", targets))
                        .build();

        targets.add("start");

        assertEquals(List.of("finish"), graph.getEdges().get("start"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getEdges().get("start").add("start"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> graph.getEdges().put("finish", List.of("start")));
    }

    @Test
    void streamPropagatesNodeFailureToSubscriber() {
        BaseNode broken = new BaseNode(NodeConfig.builder()
                .nodeId("broken").nodeName("broken").timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                throw new IllegalStateException("upstream unavailable");
            }
        };
        Graph graph = Graph.builder().name("failed-stream")
                .startNode("broken").endNode("broken")
                .nodes(Map.of("broken", broken)).build();

        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> graph.executeStream(Map.of()).collectList().block());

        assertTrue(failure.toString().contains("broken"));
    }

    @Test
    void rejectsBrokenEdgesBeforeExecution() {
        Graph graph = Graph.builder()
                .startNode("start")
                .nodes(Map.of("start", node("start"), "finish", node("finish")))
                .build();

        graph.setEdges(Map.of("missing", List.of("finish")));
        assertFalse(graph.validateGraph());
        graph.setEdges(Map.of("start", List.of("missing")));
        assertThrows(IllegalStateException.class, graph::validate);
        graph.setEdges(Map.of("start", List.of()));
        assertThrows(IllegalStateException.class, graph::validate);
        graph.setEdges(Map.of("start", List.of("finish")));
        assertTrue(graph.validateGraph());
    }

    @Test
    void rejectsUnconditionalCycleBeforeExecution() {
        Graph graph = Graph.builder()
                .startNode("start")
                .nodes(Map.of("start", node("start"), "finish", node("finish")))
                .edges(Map.of("start", List.of("finish"), "finish", List.of("start")))
                .build();

        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("循环"));
        assertTrue(assertThrows(IllegalStateException.class, graph::compile)
                .getMessage().contains("循环"));
    }

    @Test
    void reportsTheActualCycleWhenItStartsAfterTheEntryNode() {
        Graph graph = Graph.builder()
                .startNode("a")
                .nodes(Map.of("a", node("a"), "b", node("b"), "c", node("c")))
                .edges(Map.of("a", List.of("b"), "b", List.of("c"), "c", List.of("b")))
                .build();

        assertEquals("b -> c -> b", graph.detectCycle());
    }

    @Test
    void rejectsMissingStartAndEndNodesBeforeCompiling() {
        Graph graph = Graph.builder().nodes(Map.of("start", node("start"))).build();

        assertTrue(assertThrows(IllegalStateException.class, graph::compile)
                .getMessage().contains("起始节点"));
        graph.setStartNode("missing");
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("未在节点列表中定义"));
        graph.setStartNode("start");
        graph.setEndNode("missing");
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("结束节点"));
    }

    @Test
    void rejectsInvalidConditionalRoutesAndNullEdgeTargets() {
        Graph graph = Graph.builder()
                .startNode("start")
                .nodes(Map.of("start", node("start"), "finish", node("finish")))
                .build();
        Map<String, List<String>> nullTargets = new HashMap<>();
        nullTargets.put("start", null);
        graph.setEdges(nullTargets);
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("目标列表"));

        graph.setEdges(Map.of());
        graph.setConditionalEdges(Map.of("missing", ConditionEdge.builder().build()));
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("条件边的源节点"));

        Map<String, ConditionEdge> nullRoute = new HashMap<>();
        nullRoute.put("start", null);
        graph.setConditionalEdges(nullRoute);
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("不能为空"));

        graph.setConditionalEdges(Map.of("start", ConditionEdge.builder()
                .from("start").nodeMappings(Map.of("choice", "missing")).build()));
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("条件边的目标节点"));

        graph.setConditionalEdges(Map.of("start", ConditionEdge.builder()
                .from("start").nodeMappings(Map.of()).build()));
        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("没有可用的目标节点"));
    }

    @Test
    void validatesDefaultConditionalTargetAndCountsItAsReachable() {
        Graph graph = Graph.builder()
                .startNode("start")
                .endNode("finish")
                .nodes(Map.of("start", node("start"), "pass", node("pass"),
                        "finish", node("finish")))
                .conditionalEdges(Map.of("start", ConditionEdge.builder()
                        .from("start").defaultTarget("missing")
                        .nodeMappings(Map.of("pass", "pass")).build()))
                .build();

        assertTrue(assertThrows(IllegalStateException.class, graph::validate)
                .getMessage().contains("条件边的默认目标节点"));

        graph.setConditionalEdges(Map.of("start", ConditionEdge.builder()
                .from("start").defaultTarget("finish")
                .nodeMappings(Map.of("pass", "pass")).build()));
        graph.validate();
        assertFalse(graph.findUnreachableNodes().contains("finish"));
    }

    @Test
    void executesDefaultConditionalTargetWhenNoConditionMatches() throws Exception {
        Graph graph = Graph.builder()
                .startNode("start")
                .endNode("fallback")
                .nodes(Map.of("start", node("start"), "pass", node("pass"),
                        "fallback", node("fallback")))
                .conditionalEdges(Map.of("start", ConditionEdge.builder()
                        .from("start")
                        .functionCondition(state -> null)
                        .defaultTarget("fallback")
                        .nodeMappings(Map.of("pass", "pass"))
                        .build()))
                .build();

        assertEquals("fallback", graph.execute(Map.of()).get("node"));
    }

    @Test
    void nestedAgentUsesAuthenticatedActorAfterPreviousNodeChangesIdentityState() throws Exception {
        ActorContext authenticated = new ActorContext(new TenantId(9), new UserId(7), false);
        ActorContext forged = new ActorContext(new TenantId(99), new UserId(88), false);
        AgentRuntime runtime = mock(AgentRuntime.class);
        Execution child = new Execution("child", "v1", Map.of());
        child.start();
        child.complete(Map.of("answer", "ok"));
        when(runtime.execute(any(ActorContext.class), eq("child"), any())).thenReturn(child);
        BaseNode mutator = new BaseNode(NodeConfig.builder()
                .nodeId("mutator").nodeName("mutator").timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("tenantId", 99L);
                output.addData("userId", 88L);
                output.addData("__knowledgeAccessContext", forged);
                return output;
            }
        };
        AgentCallConfig nestedConfig = new AgentCallConfig();
        nestedConfig.setTargetAgentId("child");
        nestedConfig.setTimeout(0L);
        AgentCallNode nested = AgentCallNode.builder()
                .config(nestedConfig)
                .agentRuntime(runtime).build();
        Graph graph = Graph.builder().startNode("mutator").endNode("nested")
                .nodes(Map.of("mutator", mutator, "nested", nested))
                .edges(Map.of("mutator", List.of("nested"))).build();

        graph.execute(Map.of("tenantId", 9L, "userId", 7L,
                "__knowledgeAccessContext", authenticated));

        verify(runtime).execute(eq(authenticated), eq("child"), any());
    }

    @Test
    void llmAndToolUseAuthenticatedActorAfterPreviousNodeChangesIdentityState() throws Exception {
        ActorContext authenticated = new ActorContext(new TenantId(9), new UserId(7), false);
        ActorContext forged = new ActorContext(new TenantId(99), new UserId(88), false);
        ChatEngine chat = mock(ChatEngine.class);
        when(chat.chat(any())).thenReturn(ChatResponse.builder().success(true)
                .content("ok").platform("p").model("m").build());
        ToolExecutionPipeline pipeline = mock(ToolExecutionPipeline.class);
        when(pipeline.executeById(anyString(), any(TenantId.class), anyLong(), any(), any()))
                .thenReturn(new ToolExecutionPipeline.Result(
                        ToolExecutionPipeline.Result.Status.COMPLETED, "tool-ok", null));
        BaseNode mutator = new BaseNode(NodeConfig.builder()
                .nodeId("mutator").nodeName("mutator").timeout(0L).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("tenantId", 99L);
                output.addData("userId", 88L);
                output.addData(NodeActorContext.KEY, forged);
                return output;
            }
        };
        ToolCallNode tool = ToolCallNode.builder().toolService(mock(ToolService.class))
                .executionPipeline(pipeline)
                .config(ToolCallConfig.builder().toolName("search").timeout(0L).build())
                .build();
        Graph graph = Graph.builder().startNode("mutator").endNode("tool")
                .nodes(Map.of("mutator", mutator,
                        "llm", LlmCallNode.builder().chatEngine(chat).build(), "tool", tool))
                .edges(Map.of("mutator", List.of("llm"), "llm", List.of("tool"))).build();

        graph.execute(NodeActorContext.bind(authenticated, Map.of("__aiRunId", "run-1")));

        verify(chat).chat(argThat(request -> request.getTenantId() == 9L
                && request.getUserId() == 7L));
        verify(pipeline).executeById(eq("run-1"), eq(authenticated.tenantId()), eq(7L),
                any(), any());
    }

    @Test
    void nodeExecutionHistoryUsesTrustedActorInsteadOfMutableIdentityFields() throws Exception {
        ActorContext authenticated = new ActorContext(new TenantId(9), new UserId(7), false);
        ExecutionHistoryService history = mock(ExecutionHistoryService.class);
        BaseNode recorder = node("recorder");
        recorder.setExecutionHistoryService(history);

        recorder.apply(Map.of("tenantId", 99L, "userId", 88L,
                "__knowledgeAccessContext", authenticated));

        verify(history).startExecution(eq(authenticated), isNull(), isNull(), isNull(),
                eq("recorder"), eq("DEFAULT"), anyString());
    }

    @Test
    void changingEdgesInvalidatesCompiledExecutionPlan() throws Exception {
        Graph graph = Graph.builder()
                .startNode("start")
                .endNode("finish")
                .nodes(Map.of("start", node("start"), "middle", node("middle"),
                        "finish", node("finish")))
                .edges(Map.of("start", List.of("finish")))
                .build();
        var first = graph.compile();

        graph.setEdges(Map.of("start", List.of("middle"), "middle", List.of("finish")));

        assertFalse(graph.isCompiled());
        assertNotSame(first, graph.compile());
        assertTrue(graph.executeStream(Map.of()).map(output -> output.node())
                .collectList().block().contains("middle"));
    }

    @Test
    void externalConditionalRouteChangesCannotAlterACompiledGraph() throws Exception {
        ConditionEdge route = ConditionEdge.builder().from("start")
                .functionCondition(state -> "left")
                .nodeMappings(Map.of("left", "left", "right", "right"))
                .build();
        Graph graph = Graph.builder().startNode("start").endNode("finish")
                .nodes(Map.of("start", node("start"), "left", node("left"),
                        "right", node("right"), "finish", node("finish")))
                .edges(Map.of("left", List.of("finish"), "right", List.of("finish")))
                .conditionalEdges(Map.of("start", route)).build();
        graph.compile();

        route.setFunctionCondition(state -> "right");
        graph.getConditionalEdges().get("start").setFunctionCondition(state -> "right");

        List<String> visited = graph.executeStream(Map.of()).map(output -> output.node())
                .collectList().block();
        assertTrue(visited.contains("left"));
        assertFalse(visited.contains("right"));
    }

    private static BaseNode node(String id) {
        return new BaseNode(
                NodeConfig.builder()
                        .nodeId(id)
                        .nodeName(id)
                        .nodeType(NodeType.DEFAULT)
                        .timeout(0L)
                        .build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                NodeOutput output = new NodeOutput();
                output.setSuccess(true);
                output.addData("node", id);
                return output;
            }
        };
    }
}
