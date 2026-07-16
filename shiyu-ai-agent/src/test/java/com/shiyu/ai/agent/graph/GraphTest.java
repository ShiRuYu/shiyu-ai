package com.shiyu.ai.agent.graph;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.DefaultNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Graph 编译、验证、循环检测、不可达节点检测测试
 */
@Tag("dev")
class GraphTest {

    private Graph graph;
    private BaseNode nodeA;
    private BaseNode nodeB;
    private BaseNode nodeC;

    @BeforeEach
    void setUp() {
        graph = new Graph();
        graph.setName("testGraph");
        nodeA = createNode("nodeA", NodeType.DEFAULT);
        nodeB = createNode("nodeB", NodeType.DEFAULT);
        nodeC = createNode("nodeC", NodeType.DEFAULT);
    }

    private BaseNode createNode(String id, NodeType type) {
        NodeConfig config = NodeConfig.builder()
                .nodeId(id)
                .nodeName(id)
                .nodeType(type)
                .build();
        return DefaultNode.builder().config(config).build();
    }

    // ============ 图构建 ============

    @Test
    void testAddNode() {
        graph.addNode("a", nodeA);
        assertTrue(graph.getNodes().containsKey("a"));
    }

    @Test
    void testAddEdge() {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        assertTrue(graph.getEdges().get("a").contains("b"));
    }

    @Test
    void testSetStartAndEndNode() {
        graph.setStartNode("a").setEndNode("b");
        assertEquals("a", graph.getStartNode());
        assertEquals("b", graph.getEndNode());
    }

    // ============ 验证 ============

    @Test
    void testValidateValidGraph() {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.setStartNode("a").setEndNode("b");
        assertDoesNotThrow(() -> graph.validate());
    }

    @Test
    void testValidateMissingStartNode() {
        graph.addNode("a", nodeA);
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> graph.validate());
        assertTrue(ex.getMessage().contains("起始节点未设置"));
    }

    @Test
    void testValidateStartNodeNotInNodes() {
        graph.setStartNode("nonexistent");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> graph.validate());
        assertTrue(ex.getMessage().contains("未在节点列表中定义"));
    }

    @Test
    void testValidateEdgeTargetNotExists() {
        graph.addNode("a", nodeA);
        graph.addEdge("a", "nonexistent");
        graph.setStartNode("a");
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> graph.validate());
        assertTrue(ex.getMessage().contains("未定义"));
    }

    // ============ 循环检测 ============

    @Test
    void testDetectNoCycle() {
        graph.addNode("a", nodeA).addNode("b", nodeB).addNode("c", nodeC);
        graph.addEdge("a", "b");
        graph.addEdge("b", "c");
        assertNull(graph.detectCycle());
    }

    @Test
    void testDetectDirectCycle() {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.addEdge("b", "a");
        assertNotNull(graph.detectCycle());
    }

    @Test
    void testDetectSelfCycle() {
        graph.addNode("a", nodeA);
        graph.addEdge("a", "a");
        assertNotNull(graph.detectCycle());
    }

    @Test
    void testDetectTriangleCycle() {
        graph.addNode("a", nodeA).addNode("b", nodeB).addNode("c", nodeC);
        graph.addEdge("a", "b").addEdge("b", "c").addEdge("c", "a");
        assertNotNull(graph.detectCycle());
    }

    @Test
    void testConditionalEdgeNotCountedAsCycle() {
        // 条件边是"有时"路径，不应参与循环检测
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.addConditionalEdge("b", state -> "a", Map.of("a", "a"));
        graph.setStartNode("a").setEndNode("b");
        // 验证不会因条件边引发误判
        assertDoesNotThrow(() -> graph.validate());
    }

    // ============ 不可达节点 ============

    @Test
    void testAllNodesReachable() {
        graph.addNode("a", nodeA).addNode("b", nodeB).addNode("c", nodeC);
        graph.addEdge("a", "b").addEdge("b", "c");
        graph.setStartNode("a");
        Set<String> unreachable = graph.findUnreachableNodes();
        assertTrue(unreachable.isEmpty());
    }

    @Test
    void testHasUnreachableNode() {
        graph.addNode("a", nodeA).addNode("b", nodeB).addNode("c", nodeC);
        graph.addEdge("a", "b");  // c 不可达
        graph.setStartNode("a");
        Set<String> unreachable = graph.findUnreachableNodes();
        assertTrue(unreachable.contains("c"));
        assertEquals(1, unreachable.size());
    }

    @Test
    void testUnreachableViaConditionalEdgeNotMarked() {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.setStartNode("a");
        graph.addConditionalEdge("a", state -> "b", Map.of("b", "b"));
        Set<String> unreachable = graph.findUnreachableNodes();
        assertFalse(unreachable.contains("b"));
    }

    // ============ 编译 ============

    @Test
    void testCompileValidGraph() {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.setStartNode("a").setEndNode("b");
        assertDoesNotThrow(() -> graph.compile());
        assertTrue(graph.isCompiled());
    }

    @Test
    void testCompileCacheHit() throws Exception {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.setStartNode("a").setEndNode("b");
        var compiled1 = graph.compile();
        var compiled2 = graph.compile();
        assertSame(compiled1, compiled2, "重复 compile() 应返回缓存实例");
    }

    @Test
    void testRecompileClearsCache() throws Exception {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.setStartNode("a").setEndNode("b");
        var compiled1 = graph.compile();
        var compiled2 = graph.recompile();
        assertNotSame(compiled1, compiled2, "recompile() 应返回新实例");
    }

    @Test
    void testValidateGraphReturnsTrueForValid() {
        graph.addNode("a", nodeA).addNode("b", nodeB);
        graph.addEdge("a", "b");
        graph.setStartNode("a").setEndNode("b");
        assertTrue(graph.validateGraph());
    }

    @Test
    void testValidateGraphReturnsFalseForInvalid() {
        assertFalse(graph.validateGraph());
    }

    // ============ 执行 ============

    @Test
    void testExecuteSimpleGraph() throws Exception {
        graph.addNode("a", nodeA);
        graph.setStartNode("a").setEndNode("a");
        Map<String, Object> input = new HashMap<>();
        input.put("test", "value");
        Map<String, Object> result = graph.execute(input);
        assertNotNull(result);
    }

    @Test
    void testExecuteStreamReturnsFlux() throws Exception {
        graph.addNode("a", nodeA);
        graph.setStartNode("a").setEndNode("a");
        Map<String, Object> input = new HashMap<>();
        input.put("test", "value");
        var flux = graph.executeStream(input);
        assertNotNull(flux);
    }
}
