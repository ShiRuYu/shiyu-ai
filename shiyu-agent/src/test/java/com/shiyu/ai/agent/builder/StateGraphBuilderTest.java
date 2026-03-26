package com.shiyu.ai.agent.builder;

import com.shiyu.ai.agent.config.NodeConfig;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.state.AgentState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StateGraphBuilder 测试类
 */
@Slf4j
class StateGraphBuilderTest {
    
    /**
     * 简单测试节点
     */
    private static class SimpleTestNode extends BaseNode {
        @Override
        protected Map<String, Object> doExecute(AgentState state, Map<String, Object> params) {
            Map<String, Object> result = new HashMap<>();
            result.put("node_executed", true);
            result.put("node_id", config.getNodeId());
            return result;
        }
    }
    
    private StateGraphBuilder builder;
    
    @BeforeEach
    void setUp() {
        builder = new StateGraphBuilder();
    }
    
    @Test
    void testBuild_WithGraphObject() {
        // 创建 Graph
        Graph graph = Graph.builder()
                .name("test_graph")
                .description("测试图")
                .build();
        
        // 添加节点
        BaseNode node1 = new SimpleTestNode();
        NodeConfig config1 = NodeConfig.builder()
                .nodeId("node1")
                .nodeName("节点 1")
                .build();
        
        BaseNode node2 = new SimpleTestNode();
        NodeConfig config2 = NodeConfig.builder()
                .nodeId("node2")
                .nodeName("节点 2")
                .build();
        
        graph.addNode("node1", node1, config1);
        graph.addNode("node2", node2, config2);
        
        // 添加边
        graph.addEdge("node1", "node2");
        
        // 设置起始和结束节点
        graph.setStartNode("node1");
        graph.setEndNode("node2");
        
        // 构建 StateGraph
        StateGraphBuilder builder = new StateGraphBuilder();
        StateGraph<AgentState> stateGraph = null;
        try {
            stateGraph = builder.build(graph, AgentState::new);
        } catch (Exception e) {
            fail("构建失败：" + e.getMessage());
        }
        
        // 验证
        assertNotNull(stateGraph);
    }
    
    @Test
    void testBuild_WithFluentBuilder() {
        // 使用流式构建器
        StateGraph<AgentState> stateGraph = null;
        try {
            stateGraph = StateGraphBuilder.create(AgentState::new)
                    .name("fluent_graph")
                    .description("流式构建测试")
                    .addNode("start", new SimpleTestNode())
                    .addNode("end", new SimpleTestNode())
                    .addEdge("start", "end")
                    .startNode("start")
                    .endNode("end")
                    .buildStateGraph();
        } catch (Exception e) {
            fail("构建失败：" + e.getMessage());
        }
        
        // 验证
        assertNotNull(stateGraph);
    }
    
    @Test
    void testGraph_AddNodes() {
        Graph graph = Graph.builder().build();
        
        BaseNode node1 = new SimpleTestNode();
        BaseNode node2 = new SimpleTestNode();
        
        graph.addNode("node1", node1);
        graph.addNode("node2", node2);
        
        assertEquals(2, graph.getNodes().size());
        assertEquals(node1, graph.getNode("node1"));
        assertEquals(node2, graph.getNode("node2"));
    }
    
    @Test
    void testGraph_AddEdges() {
        Graph graph = Graph.builder().build();
        
        graph.addEdge("node1", "node2");
        graph.addEdge("node1", "node3");
        graph.addEdge("node2", "node4");
        
        assertEquals(2, graph.getTargets("node1").size());
        assertEquals(1, graph.getTargets("node2").size());
    }
    
    @Test
    void testGraph_AddConditionalEdge() {
        Graph graph = Graph.builder().build();
        
        Map<String, String> conditionMap = new HashMap<>();
        conditionMap.put("condition1", "node2");
        conditionMap.put("condition2", "node3");
        
        graph.addConditionalEdge("node1", conditionMap);
        
        assertEquals(2, graph.getConditionalEdge("node1").size());
        assertEquals("node2", graph.getConditionalEdge("node1").get("condition1"));
    }
}
