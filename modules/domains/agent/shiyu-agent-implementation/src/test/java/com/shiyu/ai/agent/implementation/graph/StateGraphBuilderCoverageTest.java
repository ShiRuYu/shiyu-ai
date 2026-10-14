package com.shiyu.ai.agent.implementation.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;

import org.junit.jupiter.api.Test;
import org.bsc.langgraph4j.GraphStateException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 验证 State Graph Builder Coverage 相关功能、边界条件、异常路径和协作行为。
 */
class StateGraphBuilderCoverageTest {
    @Test
    void rejectsNullConditionalRouteBeforeCompiling() {
        Map<String, ConditionEdge> routes = new HashMap<>();
        routes.put("start", null);
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(Map.of("start", node("start")))
                .conditionalEdges(routes).startNode("start").endNode("start")
                .build().build());
    }

    @Test
    void rejectsEmptyDefaultAndNullMappingsTogether() {
        ConditionEdge route = ConditionEdge.builder().from("start")
                .functionCondition(state -> "start")
                .defaultTarget("").nodeMappings(null).build();
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(Map.of("start", node("start")))
                .conditionalEdges(Map.of("start", route))
                .startNode("start").endNode("start").build().build());
    }


    @Test
    void defaultOnlyConditionalRouteReachesItsFallbackNode() throws Exception {
        Graph graph = Graph.builder()
                .name("default-route")
                .startNode("start")
                .endNode("fallback")
                .nodes(Map.of("start", node("start"), "fallback", node("fallback")))
                .conditionalEdges(Map.of("start", ConditionEdge.builder()
                        .from("start").functionCondition(state -> null)
                        .defaultTarget("fallback").nodeMappings(null).build()))
                .build();

        var result = StateGraphBuilder.fromGraph(graph).build()
                .invoke(Map.of()).orElseThrow();

        assertEquals("fallback", result.data().get("node"));
    }

    @Test
    void buildsAValidatedGraphWithLinearAndConditionalEdges() throws Exception {
        Graph graph =
                Graph.builder()
                        .name("builder-test")
                        .startNode("start")
                        .endNode("finish")
                        .nodes(
                                Map.of(
                                        "start",
                                        node("start"),
                                        "middle",
                                        node("middle"),
                                        "finish",
                                        node("finish"),
                                        "fallback",
                                        node("fallback")))
                        .edges(Map.of("start", List.of("middle")))
                        .conditionalEdges(
                                Map.of(
                                        "middle",
                                        ConditionEdge.builder()
                                                .from("middle")
                                                .functionCondition(state -> "finish")
                                                .nodeMappings(
                                                        Map.of(
                                                                "finish",
                                                                "finish",
                                                                "fallback",
                                                                "fallback"))
                                                .build()))
                        .build();

        StateGraphBuilder builder = StateGraphBuilder.fromGraph(graph);
        assertNotNull(builder.build());
    }

    @Test
    void rejectsEdgesWhoseSourceOrDestinationDoesNotExist() {
        Map<String, BaseNode> nodes = Map.of("start", node("start"));
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).edges(Map.of("start", List.of("missing")))
                .startNode("start").endNode("start").build().build());
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).edges(Map.of("missing", List.of("start")))
                .startNode("start").endNode("start").build().build());
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).edges(Map.of("start", List.of()))
                .startNode("start").endNode("start").build().build());
    }

    @Test
    void rejectsStartOrEndThatIsNotPresentInTheGraph() {
        Map<String, BaseNode> nodes = Map.of("present", node("present"));
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).startNode("missing").endNode("present")
                .build().build());
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).startNode("present").endNode("missing")
                .build().build());
    }

    @Test
    void rejectsConditionalRoutesThatPointOutsideTheGraph() {
        Map<String, BaseNode> nodes = Map.of("present", node("present"));
        ConditionEdge missingDestination = ConditionEdge.builder()
                .from("present").functionCondition(state -> "missing")
                .nodeMappings(Map.of("missing", "missing")).build();
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).conditionalEdges(Map.of("present", missingDestination))
                .startNode("present").endNode("present").build().build());
        ConditionEdge missingSource = ConditionEdge.builder()
                .from("missing").functionCondition(state -> "present")
                .nodeMappings(Map.of("present", "present")).build();
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).conditionalEdges(Map.of("missing", missingSource))
                .startNode("present").endNode("present").build().build());
        ConditionEdge missingDefault = ConditionEdge.builder()
                .from("present").functionCondition(state -> "missing")
                .defaultTarget("missing").build();
        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).conditionalEdges(Map.of("present", missingDefault))
                .startNode("present").endNode("present").build().build());
    }

    @Test
    void rejectsConditionalRouteWithoutAnyTargetInsteadOfDroppingIt() {
        Map<String, BaseNode> nodes = Map.of("present", node("present"));
        ConditionEdge route = ConditionEdge.builder()
                .from("present").functionCondition(state -> "present").build();

        assertThrows(GraphStateException.class, () -> StateGraphBuilder.builder()
                .nodes(nodes).conditionalEdges(Map.of("present", route))
                .startNode("present").endNode("present").build().build());
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
