package com.shiyu.ai.agent.graph;

import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.node.NodeType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StateGraphBuilderCoverageTest {

    @Test
    void buildsAValidatedGraphWithLinearAndConditionalEdges() throws Exception {
        Graph graph = Graph.builder()
                .name("builder-test")
                .startNode("start")
                .endNode("finish")
                .nodes(Map.of("start", node("start"), "middle", node("middle"),
                        "finish", node("finish"), "fallback", node("fallback")))
                .edges(Map.of("start", List.of("middle")))
                .conditionalEdges(Map.of("middle", ConditionEdge.builder()
                        .from("middle")
                        .functionCondition(state -> "finish")
                        .nodeMappings(Map.of("finish", "finish", "fallback", "fallback"))
                        .build()))
                .build();

        StateGraphBuilder builder = StateGraphBuilder.fromGraph(graph);
        assertNotNull(builder.build());
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
