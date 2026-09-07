package com.shiyu.ai.agent.cache;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeConfig;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.agent.node.NodeInput;
import com.shiyu.ai.agent.node.NodeOutput;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentLoaderTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7L), new UserId(9L), false);

    @Test
    void returnsNullForMissingInactiveOrIncompleteDatabaseDefinitions() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        NodeFactory factory = mock(NodeFactory.class);
        AgentLoader loader = new AgentLoader(factory, repository);
        assertNull(loader.loadFromDb(ACTOR, "missing"));

        AgentDefBO inactive = new AgentDefBO(); inactive.setStatus(0);
        when(repository.selectByAgentId(ACTOR.tenantId(), "inactive")).thenReturn(inactive);
        assertNull(loader.loadFromDb(ACTOR, "inactive"));
        AgentDefBO noVersion = new AgentDefBO(); noVersion.setStatus(1);
        when(repository.selectByAgentId(ACTOR.tenantId(), "noversion")).thenReturn(noVersion);
        assertNull(loader.loadFromDb(ACTOR, "noversion"));
        AgentDefBO missingGraph = new AgentDefBO(); missingGraph.setStatus(1); missingGraph.setCurrentVersion("v1");
        when(repository.selectByAgentId(ACTOR.tenantId(), "nog")).thenReturn(missingGraph);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "nog", "v1")).thenReturn(null);
        assertNull(loader.loadFromDb(ACTOR, "nog"));
        AgentVersionBO blankGraph = new AgentVersionBO(); blankGraph.setGraphConfig(" ");
        when(repository.selectByAgentId(ACTOR.tenantId(), "blank")).thenReturn(missingGraph);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "blank", "v1")).thenReturn(blankGraph);
        assertNull(loader.loadFromDb(ACTOR, "blank"));
    }

    @Test
    void buildsGraphDefaultsEdgesAndConditionalRouting() {
        AgentAdminRepository repository = mock(AgentAdminRepository.class);
        NodeFactory factory = mock(NodeFactory.class);
        when(factory.createNode(any(NodeConfig.class))).thenAnswer(invocation -> node(((NodeConfig) invocation.getArgument(0)).getNodeId()));
        AgentLoader loader = new AgentLoader(factory, repository);
        GraphConfigRequest config = new GraphConfigRequest();
        config.setStartNode("start"); config.setEndNode("end");
        GraphConfigRequest.NodeConfigDTO start = new GraphConfigRequest.NodeConfigDTO(); start.setNodeType("DEFAULT");
        GraphConfigRequest.NodeConfigDTO end = new GraphConfigRequest.NodeConfigDTO(); end.setNodeType("DEFAULT"); end.setEnabled(false);
        config.setNodes(Map.of("start", start, "end", end));
        config.setEdges(Map.of("start", List.of("end")));
        GraphConfigRequest.ConditionalEdgeDTO conditional = new GraphConfigRequest.ConditionalEdgeDTO(); conditional.setDefaultTarget("end"); conditional.setConditionType("SCORE_ROUTING");
        config.setConditionalEdges(Map.of("start", conditional));
        Graph graph = loader.buildGraph("agent", config);
        assertEquals("agent_graph", graph.getName());
        assertEquals(2, graph.getNodes().size());
        assertEquals("end", graph.getConditionalEdges().get("start").getDefaultTarget());
        assertEquals("retry", graph.getConditionalEdges().get("start").getFunctionCondition().apply(Map.of("reviewNeeded", true)));
        assertEquals("pass", graph.getConditionalEdges().get("start").getFunctionCondition().apply(Map.of("reviewNeeded", false)));

        GraphConfigRequest minimal = new GraphConfigRequest();
        minimal.setStartNode("start");
        minimal.setEndNode("end");
        minimal.setNodes(Map.of("start", start, "end", end));
        Graph noEdges = loader.buildGraph("agent", minimal);
        assertEquals("agent_graph", noEdges.getName());

        GraphConfigRequest intentConfig = new GraphConfigRequest();
        intentConfig.setStartNode("start");
        intentConfig.setEndNode("end");
        intentConfig.setNodes(Map.of("start", start, "end", end));
        GraphConfigRequest.ConditionalEdgeDTO intent = new GraphConfigRequest.ConditionalEdgeDTO();
        intentConfig.setConditionalEdges(Map.of("start", intent));
        Graph intentGraph = loader.buildGraph("agent", intentConfig);
        assertEquals("UNKNOWN", intentGraph.getConditionalEdges().get("start").getFunctionCondition().apply(Map.of()));
        assertEquals("LOGIN", intentGraph.getConditionalEdges().get("start").getFunctionCondition().apply(Map.of("intentCode", "LOGIN")));
    }

    @Test
    void buildsAllContractNodeConfigVariants() {
        NodeFactory factory = mock(NodeFactory.class);
        when(factory.createNode(any(NodeConfig.class))).thenAnswer(invocation -> node(((NodeConfig) invocation.getArgument(0)).getNodeId()));
        AgentLoader loader = new AgentLoader(factory, mock(AgentAdminRepository.class));
        Map<String, GraphConfigRequest.NodeConfigDTO> nodes = new LinkedHashMap<>();
        String previous = null;
        Map<String, List<String>> edges = new LinkedHashMap<>();
        for (com.shiyu.ai.agent.node.NodeType type : com.shiyu.ai.agent.node.NodeType.values()) {
            String id = type.getCode().toLowerCase(java.util.Locale.ROOT);
            GraphConfigRequest.NodeConfigDTO dto = new GraphConfigRequest.NodeConfigDTO();
            dto.setNodeType(type.getCode()); dto.setProperties(Map.of()); dto.setConfig(Map.of());
            nodes.put(id, dto);
            if (previous != null) edges.put(previous, List.of(id));
            previous = id;
        }
        GraphConfigRequest config = new GraphConfigRequest();
        config.setName("all-types"); config.setStartNode("default"); config.setEndNode(previous); config.setNodes(nodes); config.setEdges(edges);
        Graph graph = loader.buildGraph("agent", config);
        assertEquals(nodes.size(), graph.getNodes().size());
        verify(factory, times(nodes.size())).createNode(any(NodeConfig.class));
    }

    @Test
    void appliesExplicitNodeDefaultsAndSkipsUnchangedMetadataWrites() {
        NodeFactory factory = mock(NodeFactory.class);
        AtomicReference<NodeConfig> captured = new AtomicReference<>();
        when(factory.createNode(any(NodeConfig.class))).thenAnswer(invocation -> {
            NodeConfig config = invocation.getArgument(0);
            captured.set(config);
            return node(config.getNodeId());
        });
        AgentLoader loader = new AgentLoader(factory, mock(AgentAdminRepository.class));

        GraphConfigRequest.NodeConfigDTO dto = new GraphConfigRequest.NodeConfigDTO();
        dto.setNodeType("DEFAULT");
        dto.setNodeName("configured");
        dto.setDescription("description");
        dto.setEnabled(false);
        dto.setTimeout(123L);
        dto.setRetryCount(2);
        dto.setRetryInterval(456L);
        dto.setErrorStrategy("CONTINUE");
        dto.setLogLevel("DEBUG");
        dto.setProperties(Map.of("source", "test"));
        dto.setConfig(Map.of("nodeName", "overridden"));

        GraphConfigRequest config = new GraphConfigRequest();
        config.setStartNode("node");
        config.setEndNode("node");
        config.setNodes(Map.of("node", dto));
        Graph graph = loader.buildGraph("agent", config);
        assertEquals("overridden", captured.get().getNodeName());
        assertFalse(captured.get().getEnabled());
        assertEquals(123L, captured.get().getTimeout());
    }

    private static BaseNode node(String id) {
        return new BaseNode(NodeConfig.builder().nodeId(id).nodeName(id).build()) {
            @Override protected NodeOutput doExecute(NodeInput input) { return NodeOutput.fromMap(Map.of()); }
        };
    }
}
