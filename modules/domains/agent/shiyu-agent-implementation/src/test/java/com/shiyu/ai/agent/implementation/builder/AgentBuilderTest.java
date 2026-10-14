package com.shiyu.ai.agent.implementation.builder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.implementation.service.AgentService;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.HashMap;

/**
 * 验证 智能体 Builder 相关功能、边界条件、异常路径和协作行为。
 */
class AgentBuilderTest {
    @Test
    void rejectsWhitespaceIdentityAndEntryNodeBeforeRegistration() {
        AgentService service = mock(AgentService.class);
        assertThrows(RuntimeException.class, () -> new AgentBuilder()
                .agentId("  ").name("Agent")
                .addNode("start", node("start")).setStartNode("start")
                .buildAndRegister(service));
        assertThrows(RuntimeException.class, () -> new AgentBuilder()
                .agentId("agent").name("  ")
                .addNode("start", node("start")).setStartNode("start")
                .buildAndRegister(service));
        assertThrows(RuntimeException.class, () -> new AgentBuilder()
                .agentId("agent").name("Agent")
                .addNode("start", node("start")).setStartNode("  ")
                .buildAndRegister(service));
        verifyNoInteractions(service);
    }

    @Test
    void rejectsPredicateRouteWithBlankTarget() {
        AgentBuilder builder = new AgentBuilder().agentId("agent").name("Agent")
                .addNode("start", node("start")).setStartNode("start");
        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start", "start", Map.of(state -> true, " ")));
    }

    @Test
    void buildsGraphWithEdgesAndRegistersDefinition() {
        BaseNode start = node("start");
        BaseNode end = node("end");
        AgentService service = mock(AgentService.class);
        AgentBuilder builder =
                new AgentBuilder()
                        .agentId("demo")
                        .name("Demo")
                        .description("demo agent")
                        .version("v2")
                        .versionDescription("version")
                        .addNode("start", start)
                        .addNode("end", end)
                        .setStartNode("start")
                        .setEndNode("end")
                        .addEdge("start", "end")
                        .addConditionalEdge(
                                "start",
                                "end",
                                Map.of(value -> Boolean.TRUE.equals(value.get("ok")), "end"));

        AgentDefinition definition = builder.build();
        assertEquals("demo", definition.getAgentId());
        assertEquals("v2", definition.getCurrentVersion());
        assertEquals(2, definition.getVersion("v2").getGraph().getNodes().size());
        assertEquals("demo", builder.buildAndRegister(service).getAgentId());
        verify(service).registerSystemAgent(any(AgentDefinition.class));
    }

    @Test
    void rejectsIncompleteDefinitionsAndWrapsRegistrationFailures() {
        assertThrows(RuntimeException.class, () -> new AgentBuilder().build());
        assertThrows(
                RuntimeException.class,
                () ->
                        new AgentBuilder()
                                .name("n")
                                .setStartNode("s")
                                .addNode("s", node("s"))
                                .build());
        assertThrows(
                RuntimeException.class,
                () ->
                        new AgentBuilder()
                                .agentId("a")
                                .setStartNode("s")
                                .addNode("s", node("s"))
                                .build());

        AgentService service = mock(AgentService.class);
        doThrow(new IllegalStateException("blocked")).when(service).registerSystemAgent(any());
        AgentBuilder valid =
                new AgentBuilder().agentId("a").name("A").addNode("s", node("s")).setStartNode("s");
        assertThrows(RuntimeException.class, () -> valid.buildAndRegister(service));
    }

    @Test
    void rejectsAnIncompleteGraphBeforeRegistration() {
        AgentService service = mock(AgentService.class);
        AgentBuilder missingStart = new AgentBuilder()
                .agentId("agent").name("Agent").addNode("start", node("start"));
        RuntimeException startFailure = assertThrows(RuntimeException.class,
                () -> missingStart.buildAndRegister(service));
        assertTrue(startFailure.getMessage().contains("startNode 不能为空"));

        AgentBuilder missingNode = new AgentBuilder()
                .agentId("agent").name("Agent").setStartNode("start");
        RuntimeException nodeFailure = assertThrows(RuntimeException.class,
                () -> missingNode.buildAndRegister(service));
        assertTrue(nodeFailure.getMessage().contains("至少需要添加一个节点"));
        verify(service, never()).registerSystemAgent(any());
    }

    @Test
    void rejectsConditionalRouteWithoutTargetsInsteadOfSilentlyDroppingIt() {
        AgentBuilder builder = new AgentBuilder().agentId("agent").name("Agent")
                .addNode("start", node("start")).setStartNode("start");

        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start", values -> "next", null));
        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start", values -> "next", Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start",
                        (java.util.function.Function<Map<String, Object>, String>) null,
                        Map.of("next", "start")));
        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start", (String) null, Map.of()));
    }

    @Test
    void rejectsNullPredicateAndTargetBeforeTheyReachRuntimeRouting() {
        AgentBuilder builder = new AgentBuilder().agentId("agent").name("Agent")
                .addNode("start", node("start")).setStartNode("start");
        Map<java.util.function.Predicate<Map<String, Object>>, String> nullPredicate =
                new java.util.HashMap<>();
        nullPredicate.put(null, "start");
        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start", "start", nullPredicate));

        Map<java.util.function.Predicate<Map<String, Object>>, String> nullTarget =
                new java.util.HashMap<>();
        nullTarget.put(state -> true, null);
        assertThrows(IllegalArgumentException.class,
                () -> builder.addConditionalEdge("start", "start", nullTarget));
    }

    @Test
    void conditionMappingsStayStableAfterCallerChangesItsMap() {
        Map<String, String> mappings = new HashMap<>(Map.of("choice", "left"));
        AgentBuilder builder = new AgentBuilder().agentId("agent").name("Agent")
                .addNode("start", node("start"))
                .addNode("left", node("left"))
                .addNode("right", node("right"))
                .setStartNode("start")
                .setEndNode("left")
                .addConditionalEdge("start", state -> "choice", mappings);
        mappings.put("choice", "right");

        AgentDefinition definition = builder.build();

        assertEquals("left", definition.getVersion("v1.0.0").getGraph()
                .getConditionalEdges().get("start").getNodeMappings().get("choice"));
    }

    private static BaseNode node(String id) {
        return new BaseNode(NodeConfig.builder().nodeId(id).nodeName(id).build()) {
            @Override
            protected NodeOutput doExecute(NodeInput input) {
                return NodeOutput.fromMap(Map.of("ok", true));
            }
        };
    }
}
