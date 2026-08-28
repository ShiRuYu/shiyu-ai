package com.shiyu.ai.agent.graph;

import com.shiyu.ai.agent.node.BaseNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ConditionEdgeCoverageTest {
    @Test
    void resolvesFunctionAndPredicateConditionsWithFallbacks() {
        ConditionEdge function = ConditionEdge.builder().from("a").defaultTarget("fallback")
                .functionCondition(state -> state.containsKey("route") ? "next" : null)
                .nodeMappings(Map.of("next", "b")).build();
        function.validate();
        assertEquals("next", function.getTarget(Map.of("route", true)));
        assertEquals("fallback", function.getTarget(Map.of()));
        assertTrue(function.hasFunctionCondition());
        assertTrue(function.hasNodeMappings());

        ConditionEdge predicate = ConditionEdge.builder().from("a").defaultTarget("fallback").build();
        predicate.predicateCondition(state -> Boolean.TRUE.equals(state.get("ok")), "yes");
        predicate.addNodeMapping("unused", "x");
        predicate.validate();
        assertEquals("yes", predicate.getTarget(Map.of("ok", true)));
        assertEquals("fallback", predicate.getTarget(Map.of("ok", false)));
        assertTrue(predicate.hasPredicateCondition());
    }

    @Test
    void rejectsIncompleteConditionEdges() {
        ConditionEdge edge = ConditionEdge.builder().build();
        assertThrows(IllegalStateException.class, edge::validate);
        edge.setFrom("a");
        assertThrows(IllegalStateException.class, edge::validate);
        edge.setFunctionCondition(state -> "x");
        assertThrows(IllegalStateException.class, edge::validate);
        edge.setDefaultTarget("b");
        assertDoesNotThrow(edge::validate);
    }

    @Test
    void validatesGraphsAndDetectsCyclesAndUnreachableNodes() {
        Graph graph = new Graph();
        graph.addNode("start", mock(BaseNode.class)).addNode("end", mock(BaseNode.class)).addNode("orphan", mock(BaseNode.class));
        graph.setStartNode("start").setEndNode("end").addEdge("start", "end");
        assertTrue(graph.validateGraph());
        assertEquals(List.of("orphan"), graph.findUnreachableNodes().stream().sorted().toList());

        Graph cycle = new Graph();
        cycle.addNode("a", mock(BaseNode.class)).addNode("b", mock(BaseNode.class)).setStartNode("a").addEdge("a", "b").addEdge("b", "a");
        assertFalse(cycle.validateGraph());
        assertNotNull(cycle.detectCycle());

        Graph invalid = new Graph();
        invalid.setStartNode("missing");
        assertFalse(invalid.validateGraph());
    }

    @Test
    void rejectsEveryInvalidGraphBoundaryAndTraversesConditionalReachability() {
        Graph nullStart = new Graph();
        nullStart.setStartNode(null);
        assertFalse(nullStart.validateGraph());
        Graph emptyStart = new Graph();
        emptyStart.setStartNode("");
        assertFalse(emptyStart.validateGraph());

        Graph missingEnd = new Graph();
        missingEnd.addNode("start", mock(BaseNode.class)).setStartNode("start").setEndNode("missing");
        assertFalse(missingEnd.validateGraph());

        Graph missingSource = new Graph();
        missingSource.addNode("start", mock(BaseNode.class)).setStartNode("start").addEdge("ghost", "start");
        assertFalse(missingSource.validateGraph());
        Graph missingTarget = new Graph();
        missingTarget.addNode("start", mock(BaseNode.class)).setStartNode("start").addEdge("start", "ghost");
        assertFalse(missingTarget.validateGraph());

        Graph conditionalSource = new Graph();
        conditionalSource.addNode("start", mock(BaseNode.class)).setStartNode("start")
                .addConditionalEdge("ghost", state -> "next", Map.of("next", "start"));
        assertFalse(conditionalSource.validateGraph());
        Graph conditionalTarget = new Graph();
        conditionalTarget.addNode("start", mock(BaseNode.class)).setStartNode("start")
                .addConditionalEdge("start", state -> "next", Map.of("next", "ghost"));
        assertFalse(conditionalTarget.validateGraph());

        Graph conditionalReachable = new Graph();
        conditionalReachable.addNode("start", mock(BaseNode.class)).addNode("next", mock(BaseNode.class))
                .setStartNode("start")
                .addConditionalEdge("start", state -> "next", Map.of("next", "next"));
        assertDoesNotThrow(conditionalReachable::validate);
        assertTrue(conditionalReachable.findUnreachableNodes().isEmpty());
        conditionalReachable.getConditionalEdges().get("start").setNodeMappings(null);
        assertFalse(conditionalReachable.findUnreachableNodes().isEmpty());

        Graph noEdges = new Graph();
        noEdges.addNode("start", mock(BaseNode.class)).setStartNode("start");
        assertNull(noEdges.detectCycle());
        assertTrue(noEdges.findUnreachableNodes().isEmpty());
    }
}
