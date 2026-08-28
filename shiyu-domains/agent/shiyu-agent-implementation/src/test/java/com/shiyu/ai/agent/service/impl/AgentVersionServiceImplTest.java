package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.enums.AgentVersionStatus;
import com.shiyu.ai.agent.domain.model.AgentDefBO;
import com.shiyu.ai.agent.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.node.NodeFactory;
import com.shiyu.ai.agent.node.BaseNode;
import com.shiyu.ai.agent.node.NodeInputParam;
import com.shiyu.ai.agent.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.request.EdgeRequest;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.request.NodeConfigRequest;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentVersionServiceImplTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(13), new UserId(6), false);
    private final AgentAdminRepository repository = mock(AgentAdminRepository.class);
    private final AgentService agentService = mock(AgentService.class);
    private final NodeFactory nodeFactory = mock(NodeFactory.class);
    private final AgentVersionServiceImpl service = new AgentVersionServiceImpl(repository, agentService, nodeFactory);

    @Test
    void createsPublishesActivatesAndArchivesVersions() {
        AgentDefBO def = new AgentDefBO(); def.setAgentId("math");
        AgentVersionBO version = version(2L, "math", "v2", AgentVersionStatus.DRAFT.getCode());
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2")).thenReturn(null);
        when(repository.createVersion(eq(ACTOR.tenantId()), any(AgentVersionBO.class))).thenAnswer(invocation -> invocation.getArgument(1));
        VersionRequest request = new VersionRequest(); request.setVersionNumber("v2"); request.setDescription("second");
        assertEquals("v2", service.createVersion(ACTOR, "math", request).getVersionNumber());
        when(repository.selectVersionById(ACTOR.tenantId(), 2L)).thenReturn(version);
        service.publishVersion(ACTOR, "math", 2L); assertEquals(AgentVersionStatus.PUBLISHED.getCode(), version.getStatus());
        service.activateVersion(ACTOR, "math", 2L); verify(repository).update(eq(ACTOR.tenantId()), eq(def));
        version.setStatus(AgentVersionStatus.PUBLISHED.getCode()); service.archiveVersion(ACTOR, "math", 2L);
        assertEquals(AgentVersionStatus.ARCHIVED.getCode(), version.getStatus());
        verify(agentService, atLeast(2)).evictRuntimeCache("math");
    }

    @Test
    void editsGraphNodesAndEdgesAndReturnsValidation() {
        AgentVersionBO version = version(3L, "math", "v1", AgentVersionStatus.DRAFT.getCode()); version.setGraphConfig("{}");
        when(repository.selectVersionById(ACTOR.tenantId(), 3L)).thenReturn(version);
        GraphConfigRequest graph = new GraphConfigRequest(); graph.setName("graph"); graph.setStartNode("start"); graph.setEndNode("end");
        service.updateGraphConfig(ACTOR, "math", 3L, graph);
        NodeConfigRequest node = new NodeConfigRequest(); node.setNodeId("start"); node.setNodeName("Start"); node.setNodeType("DEFAULT");
        service.addNode(ACTOR, "math", 3L, node);
        EdgeRequest edge = new EdgeRequest(); edge.setSourceNodeId("start"); edge.setTargetNodeId("end");
        service.addEdge(ACTOR, "math", 3L, edge); service.deleteEdge(ACTOR, "math", 3L, "start", "end");
        service.deleteNode(ACTOR, "math", 3L, "start");
        verify(repository, atLeast(4)).updateVersion(ACTOR.tenantId(), version);
        assertTrue(service.validateGraphConfig(graph).isValid());
    }

    @Test
    void rejectsMissingAgentsVersionsAndDuplicateNumbers() {
        when(repository.selectByAgentId(ACTOR.tenantId(), "missing")).thenReturn(null);
        VersionRequest request = new VersionRequest(); request.setVersionNumber("v1");
        assertThrows(IllegalArgumentException.class, () -> service.createVersion(ACTOR, "missing", request));
        AgentDefBO def = new AgentDefBO(); when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        AgentVersionBO existing = version(1L, "math", "v1", 0); when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v1")).thenReturn(existing);
        assertThrows(IllegalArgumentException.class, () -> service.createVersion(ACTOR, "math", request));
    }

    @Test
    void coversVersionQueriesUpdatesDeletionAndLifecycleGuards() {
        AgentVersionBO v = version(4L, "math", "v4", AgentVersionStatus.DRAFT.getCode());
        v.setDescription("draft");
        AgentDefBO def = new AgentDefBO(); def.setAgentId("math");
        when(repository.selectVersionsByAgentId(ACTOR.tenantId(), "math")).thenReturn(List.of(v));
        assertEquals(1, service.getVersions(ACTOR, "math").size());
        when(repository.selectVersionById(ACTOR.tenantId(), 4L)).thenReturn(v);
        assertNotNull(service.getVersionDetail(ACTOR, "math", 4L));
        assertNull(service.getVersionDetail(ACTOR, "other", 4L));
        VersionRequest update = new VersionRequest(); update.setDescription("updated");
        assertEquals("updated", service.updateVersion(ACTOR, "math", 4L, update).getDescription());
        service.deleteVersion(ACTOR, "math", 4L);
        verify(repository).deleteVersionById(ACTOR.tenantId(), 4L);
        assertNull(service.getCanvasConfig(ACTOR, "other", 4L));
        v.setCanvasConfig("{}"); service.updateCanvasConfig(ACTOR, "math", 4L, "canvas");
        assertEquals("canvas", v.getCanvasConfig());
        v.setStatus(AgentVersionStatus.PUBLISHED.getCode());
        assertThrows(IllegalArgumentException.class, () -> service.publishVersion(ACTOR, "math", 4L));
        v.setStatus(AgentVersionStatus.ARCHIVED.getCode());
        assertThrows(IllegalArgumentException.class, () -> service.archiveVersion(ACTOR, "math", 4L));
        v.setStatus(AgentVersionStatus.DRAFT.getCode());
        assertThrows(IllegalArgumentException.class, () -> service.activateVersion(ACTOR, "math", 4L));
        when(repository.selectVersionById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> service.updateVersion(ACTOR, "math", 99L, update));
    }

    @Test
    void editsConditionalAndDuplicateGraphEdgesAndNodeMetadata() {
        AgentVersionBO v = version(5L, "math", "v5", AgentVersionStatus.DRAFT.getCode());
        v.setGraphConfig("{\"nodes\":{\"start\":{\"nodeName\":\"old\",\"nodeType\":\"DEFAULT\"}},\"edges\":{\"start\":[\"end\"]},\"conditionalEdges\":{}} ");
        when(repository.selectVersionById(ACTOR.tenantId(), 5L)).thenReturn(v);
        NodeConfigRequest update = new NodeConfigRequest(); update.setNodeName("new"); update.setEnabled(false);
        update.setTimeout(42L); update.setRetryCount(2); update.setErrorStrategy("SKIP");
        service.updateNode(ACTOR, "math", 5L, "start", update);
        EdgeRequest duplicate = new EdgeRequest(); duplicate.setSourceNodeId("start"); duplicate.setTargetNodeId("end");
        service.addEdge(ACTOR, "math", 5L, duplicate);
        EdgeRequest conditional = new EdgeRequest(); conditional.setSourceNodeId("start"); conditional.setConditionType("INTENT");
        conditional.setDefaultTarget("end"); conditional.setConditionMappings(Map.of("yes", "end"));
        service.addEdge(ACTOR, "math", 5L, conditional);
        service.deleteEdge(ACTOR, "math", 5L, "start", "end");
        assertThrows(IllegalArgumentException.class, () -> service.updateNode(ACTOR, "math", 5L, "missing", update));
        service.deleteNode(ACTOR, "math", 5L, "start");
        verify(repository, atLeast(5)).updateVersion(ACTOR.tenantId(), v);
    }

    @Test
    void copiesGraphAndExtractsRequiredInputsWhileHandlingMalformedConfig() {
        AgentDefBO def = new AgentDefBO(); def.setAgentId("math");
        AgentVersionBO source = version(6L, "math", "v1", AgentVersionStatus.DRAFT.getCode());
        source.setGraphConfig("{\"nodes\":{\"n1\":{\"nodeType\":\"DEFAULT\"}}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2")).thenReturn(null);
        when(repository.selectVersionById(ACTOR.tenantId(), 6L)).thenReturn(source);
        when(repository.createVersion(eq(ACTOR.tenantId()), any(AgentVersionBO.class))).thenAnswer(i -> i.getArgument(1));
        VersionRequest copy = new VersionRequest(); copy.setVersionNumber("v2"); copy.setCopyFromVersionId(6L);
        service.createVersion(ACTOR, "math", copy);
        AgentVersionBO target = version(7L, "math", "v7", AgentVersionStatus.DRAFT.getCode());
        target.setGraphConfig("not-json"); when(repository.selectVersionById(ACTOR.tenantId(), 7L)).thenReturn(target);
        assertThrows(RuntimeException.class, () -> service.addNode(ACTOR, "math", 7L, new NodeConfigRequest()));
        assertTrue(service.validateGraphConfig(new GraphConfigRequest()).isValid());
        BaseNode node = mock(BaseNode.class);
        when(node.getRequiredInputs()).thenReturn(List.of(NodeInputParam.apiRequired("question", "string", "q")));
        when(nodeFactory.createNode(any())).thenReturn(node);
        target.setGraphConfig("{\"nodes\":{\"n1\":{\"nodeType\":\"DEFAULT\"}},\"edges\":{}}");
        NodeConfigRequest add = new NodeConfigRequest(); add.setNodeId("n2"); add.setNodeName("N2"); add.setNodeType("DEFAULT");
        service.addNode(ACTOR, "math", 7L, add);
        assertNotNull(target.getExtInfo());
    }

    @Test
    void coversVersionEdgeCasesAndEmptyGraphMetadata() {
        AgentDefBO def = new AgentDefBO(); def.setAgentId("math");
        AgentVersionBO v = version(8L, "math", "v8", AgentVersionStatus.DRAFT.getCode());
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v9")).thenReturn(null);
        when(repository.selectVersionById(ACTOR.tenantId(), 8L)).thenReturn(v);
        when(repository.createVersion(eq(ACTOR.tenantId()), any(AgentVersionBO.class))).thenAnswer(i -> i.getArgument(1));
        VersionRequest copy = new VersionRequest(); copy.setVersionNumber("v9"); copy.setCopyFromVersionId(999L);
        when(repository.selectVersionById(ACTOR.tenantId(), 999L)).thenReturn(null);
        assertEquals("v9", service.createVersion(ACTOR, "math", copy).getVersionNumber());

        VersionRequest noDescription = new VersionRequest();
        service.updateVersion(ACTOR, "math", 8L, noDescription);
        service.deleteVersion(ACTOR, "other", 8L);
        when(repository.selectVersionById(ACTOR.tenantId(), 99L)).thenReturn(null);
        service.deleteVersion(ACTOR, "math", 99L);
        v.setCanvasConfig("canvas");
        assertEquals("canvas", service.getCanvasConfig(ACTOR, "math", 8L));
        assertNotNull(service.getGraphConfig(ACTOR, "math", 8L));
        assertFalse(service.validateGraphConfig(null).isValid());

        v.setGraphConfig("{\"nodes\":{\"n1\":{\"nodeType\":\"DEFAULT\"}},\"edges\":{}}");
        when(nodeFactory.createNode(any())).thenReturn(null);
        NodeConfigRequest add = new NodeConfigRequest();
        add.setNodeId("n2");
        add.setNodeName("N2");
        add.setNodeType("DEFAULT");
        service.addNode(ACTOR, "math", 8L, add);
        assertNotNull(v.getExtInfo());

        NodeConfigRequest complete = new NodeConfigRequest();
        complete.setNodeId("n3");
        complete.setNodeName("N3");
        complete.setDescription("description");
        complete.setNodeType("DEFAULT");
        complete.setEnabled(true);
        complete.setTimeout(100L);
        complete.setRetryCount(2);
        complete.setRetryInterval(3L);
        complete.setErrorStrategy("SKIP");
        complete.setConfig(Map.of("key", "value"));
        service.addNode(ACTOR, "math", 8L, complete);
        NodeConfigRequest update = new NodeConfigRequest();
        update.setNodeName("updated");
        update.setDescription("updated description");
        update.setNodeType("DEFAULT");
        update.setEnabled(false);
        update.setTimeout(200L);
        update.setRetryCount(4);
        update.setErrorStrategy("THROW");
        update.setConfig(Map.of("updated", true));
        service.updateNode(ACTOR, "math", 8L, "n3", update);
        EdgeRequest regular = new EdgeRequest();
        regular.setSourceNodeId("n3");
        regular.setTargetNodeId("n4");
        service.addEdge(ACTOR, "math", 8L, regular);
        EdgeRequest conditionalNoMappings = new EdgeRequest();
        conditionalNoMappings.setSourceNodeId("n3");
        conditionalNoMappings.setConditionType("CONDITION");
        conditionalNoMappings.setDefaultTarget("n4");
        service.addEdge(ACTOR, "math", 8L, conditionalNoMappings);
        service.deleteEdge(ACTOR, "math", 8L, "n3", "n4");
        v.setGraphConfig("{\"nodes\":{},\"edges\":{\"n3\":\"not-a-list\"},\"conditionalEdges\":{\"n3\":{}}}");
        service.deleteNode(ACTOR, "math", 8L, "n3");
        v.setGraphConfig("not-json");
        assertNotNull(service.getVersionDetail(ACTOR, "math", 8L));
        v.setStatus(null);
        assertNotNull(service.getVersionDetail(ACTOR, "math", 8L));
    }

    private static AgentVersionBO version(Long id, String agentId, String number, Integer status) {
        AgentVersionBO version = new AgentVersionBO(); version.setId(id); version.setAgentId(agentId); version.setVersionNumber(number); version.setStatus(status); return version;
    }
}
