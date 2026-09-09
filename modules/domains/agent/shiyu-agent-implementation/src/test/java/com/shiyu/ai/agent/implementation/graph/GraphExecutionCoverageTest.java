package com.shiyu.ai.agent.implementation.graph;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeConfig;
import com.shiyu.ai.agent.contract.node.NodeInput;
import com.shiyu.ai.agent.contract.node.NodeOutput;
import com.shiyu.ai.agent.contract.node.NodeType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GraphExecutionCoverageTest {

    @Test
    void compilesCachesRecompilesAndExecutesSyncAndStream() throws Exception {
        Graph graph = Graph.builder()
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
        Graph graph = Graph.builder()
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
        Graph graph = Graph.builder()
                .startNode("start")
                .endNode("finish")
                .nodes(Map.of("start", node("start"), "finish", node("finish")))
                .edges(Map.of("start", List.of("finish")))
                .conditionalEdges(Map.of())
                .build();

        var first = graph.compile();
        graph.addNode("extra", node("extra"))
                .addEdge("finish", "extra")
                .setEndNode("extra");

        var second = graph.compile();

        assertNotSame(first, second);
        assertEquals("extra", graph.execute(Map.of()).get("node"));
    }

    @Test
    void copiesEdgeTargetListsWhenConfigured() {
        List<String> targets = new ArrayList<>(List.of("finish"));
        Graph graph = Graph.builder()
                .startNode("start")
                .nodes(Map.of("start", node("start"), "finish", node("finish")))
                .edges(Map.of("start", targets))
                .build();

        targets.add("start");

        assertEquals(List.of("finish"), graph.getEdges().get("start"));
        assertThrows(UnsupportedOperationException.class,
                () -> graph.getEdges().get("start").add("start"));
        assertThrows(UnsupportedOperationException.class,
                () -> graph.getEdges().put("finish", List.of("start")));
    }

    private static BaseNode node(String id) {
        return new BaseNode(NodeConfig.builder().nodeId(id).nodeName(id)
                .nodeType(NodeType.DEFAULT).timeout(0L).build()) {
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
