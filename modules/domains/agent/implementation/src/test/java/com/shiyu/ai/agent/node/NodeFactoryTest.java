package com.shiyu.ai.agent.node;

import org.junit.jupiter.api.Test;
import com.shiyu.ai.agent.contract.ExecutionHistoryService;
import com.shiyu.ai.agent.node.creator.NodeCreator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NodeFactoryTest {
    private final NodeFactory factory = new NodeFactory(List.of(), null);

    @Test
    void createsRegistersAndRemovesDefaultNodes() {
        NodeConfig config = NodeConfig.builder().nodeId("start").nodeName("Start").nodeType(NodeType.DEFAULT).build();
        BaseNode node = factory.createNode(config);
        assertNotNull(node);
        assertSame(node, factory.getNode("start"));
        assertNotNull(factory.getNode("start"));
        assertTrue(factory.removeNode("start"));
        assertNull(factory.getNode("start"));
        assertFalse(factory.removeNode("missing"));
    }

    @Test
    void createsBatchesAndRejectsInvalidConfiguration() {
        NodeConfig first = NodeConfig.builder().nodeId("a").nodeType(NodeType.DEFAULT).build();
        NodeConfig second = NodeConfig.builder().nodeId("b").nodeType(NodeType.TRANSFORM).build();
        Map<String, NodeConfig> configs = new LinkedHashMap<>(); configs.put("a", first); configs.put("b", second);
        assertEquals(2, factory.createNodes(configs).size());
        assertThrows(IllegalArgumentException.class, () -> factory.createNode((NodeConfig) null));
        NodeConfig missingType = NodeConfig.builder().nodeId("bad").nodeType(null).build();
        assertThrows(IllegalArgumentException.class, () -> factory.createNode(missingType));
        factory.clearNodes(); assertTrue(factory.getAllRegisteredNodes().isEmpty());
    }

    @Test
    void createsAllFallbackNodeTypesAndConvertsGenericConfig() {
        NodeType[] fallbackTypes = {
                NodeType.DEFAULT, NodeType.RAG_ENHANCEMENT, NodeType.CONDITION,
                NodeType.TRANSFORM, NodeType.OUTPUT_FORMAT
        };
        for (NodeType type : fallbackTypes) {
            BaseNode node = factory.createNode(type, "node-" + type.getCode(), type.getName());
            assertNotNull(node);
            assertEquals(type == NodeType.DEFAULT ? NodeType.DEFAULT : type, node.getConfig().getNodeType());
        }

        NodeConfig genericCondition = NodeConfig.builder()
                .nodeId("converted")
                .nodeName("converted")
                .nodeType(NodeType.CONDITION)
                .properties(Map.of("conditionExpression", "true"))
                .build();
        BaseNode converted = factory.createNode(genericCondition);
        assertNotNull(converted);
        assertEquals("converted", converted.getConfig().getNodeId());
        assertSame(converted, factory.getNode("converted"));
    }

    @Test
    void usesBeanCreatorsInjectsServicesAndReportsBatchFailures() {
        ExecutionHistoryService history = mock(ExecutionHistoryService.class);
        NodeCreator creator = mock(NodeCreator.class);
        ServiceNode supplied = new ServiceNode();
        when(creator.getType()).thenReturn(NodeType.INTENT);
        when(creator.create(any(NodeConfig.class))).thenReturn(supplied);
        NodeFactory withBeans = new NodeFactory(List.of(creator), history);

        BaseNode created = withBeans.createNode(NodeConfig.builder()
                .nodeId("intent-1").nodeType(NodeType.INTENT).build());
        assertSame(supplied, created);
        assertSame(history, created.getExecutionHistoryService());
        withBeans.registerServiceToNode("intent-1", "service", "service-value");
        assertEquals("service-value", supplied.service);
        withBeans.registerServiceToNode("intent-1", "unknown", Integer.valueOf(4));

        Map<String, NodeConfig> configs = new LinkedHashMap<>();
        configs.put("ok", NodeConfig.builder().nodeId("ok").nodeType(NodeType.DEFAULT).build());
        configs.put("education", NodeConfig.builder().nodeId("education").nodeType(NodeType.ABILITY_QUERY).build());
        RuntimeException failure = assertThrows(RuntimeException.class, () -> withBeans.createNodes(configs));
        assertTrue(failure.getMessage().contains("education"));
        assertNotNull(withBeans.createNodesWithServices(
                Map.of("ok-2", NodeConfig.builder().nodeId("ok-2").nodeType(NodeType.DEFAULT).build()),
                Map.of("ok-2", Map.of("unknown", "ignored"))));
    }

    @Test
    void beanCreatorFailureIsReportedWhenFallbackRequiresDependencies() {
        NodeCreator failing = mock(NodeCreator.class);
        when(failing.getType()).thenReturn(NodeType.INTENT);
        when(failing.create(any(NodeConfig.class))).thenThrow(new IllegalStateException("creator failed"));
        NodeFactory withFailingBean = new NodeFactory(List.of(failing), null);
        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> withFailingBean.createNode(NodeType.INTENT, "intent-fallback", "intent"));
        assertTrue(failure.getMessage().contains("intent-fallback"));
    }

    @Test
    void rejectsEducationNodesWhenTheirBeanCreatorsAreNotInstalled() {
        for (NodeType type : List.of(NodeType.ABILITY_QUERY, NodeType.EDUCATION_TEACH,
                NodeType.EDUCATION_PRACTICE, NodeType.SCORE_ANALYSIS,
                NodeType.REVIEW_SCHEDULE, NodeType.PREREQ_CHECK)) {
            RuntimeException failure = assertThrows(RuntimeException.class,
                    () -> factory.createNode(NodeConfig.builder().nodeId(type.getCode())
                            .nodeType(type).build()));
            assertNotNull(failure.getMessage());
        }
    }

    private static final class ServiceNode extends BaseNode {
        private String service;

        @Override
        protected NodeOutput doExecute(NodeInput input) {
            NodeOutput output = new NodeOutput();
            output.setSuccess(true);
            return output;
        }
    }
}
