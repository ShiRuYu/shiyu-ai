package com.shiyu.ai.agent.implementation.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.contract.node.BaseNode;
import com.shiyu.ai.agent.contract.node.NodeInputParam;
import com.shiyu.ai.agent.implementation.domain.enums.AgentVersionStatus;
import com.shiyu.ai.agent.implementation.domain.model.AgentDefBO;
import com.shiyu.ai.agent.implementation.domain.model.AgentVersionBO;
import com.shiyu.ai.agent.implementation.node.NodeFactory;
import com.shiyu.ai.agent.implementation.port.repository.AgentAdminRepository;
import com.shiyu.ai.agent.implementation.request.EdgeRequest;
import com.shiyu.ai.agent.implementation.request.GraphConfigRequest;
import com.shiyu.ai.agent.implementation.request.NodeConfigRequest;
import com.shiyu.ai.agent.implementation.request.VersionRequest;
import com.shiyu.ai.agent.implementation.service.AgentService;
import com.shiyu.ai.common.foundation.utils.JSONUtils;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 验证 智能体 Version Service Impl 相关功能、边界条件、异常路径和协作行为。
 */
class AgentVersionServiceImplTest {
    private static final ActorContext ACTOR =
            new ActorContext(new TenantId(13), new UserId(6), false);
    private final AgentAdminRepository repository = mock(AgentAdminRepository.class);
    private final AgentService agentService = mock(AgentService.class);
    private final NodeFactory nodeFactory = mock(NodeFactory.class);
    private final AgentVersionServiceImpl service =
            new AgentVersionServiceImpl(repository, agentService, nodeFactory);

    @Test
    void createsPublishesActivatesAndArchivesVersions() {
        AgentDefBO def = new AgentDefBO();
        def.setAgentId("math");
        AgentVersionBO version = version(2L, "math", "v2", AgentVersionStatus.DRAFT.getCode());
        version.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2"))
                .thenReturn(null);
        when(repository.createVersion(eq(ACTOR.tenantId()), any(AgentVersionBO.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));
        VersionRequest request = new VersionRequest();
        request.setVersionNumber("v2");
        request.setDescription("second");
        assertEquals("v2", service.createVersion(ACTOR, "math", request).getVersionNumber());
        when(repository.selectVersionById(ACTOR.tenantId(), 2L)).thenReturn(version);
        service.publishVersion(ACTOR, "math", 2L);
        assertEquals(AgentVersionStatus.PUBLISHED.getCode(), version.getStatus());
        service.activateVersion(ACTOR, "math", 2L);
        verify(repository).update(eq(ACTOR.tenantId()), eq(def));
        version.setStatus(AgentVersionStatus.PUBLISHED.getCode());
        assertThrows(IllegalStateException.class,
                () -> service.archiveVersion(ACTOR, "math", 2L));
        def.setCurrentVersion("v1");
        service.archiveVersion(ACTOR, "math", 2L);
        assertEquals(AgentVersionStatus.ARCHIVED.getCode(), version.getStatus());
        verify(agentService, atLeast(2)).evictRuntimeCache(ACTOR, "math");
    }

    @Test
    void activationRejectsHistoricalPublishedVersionWithInvalidGraph() {
        AgentVersionBO invalid = version(21L, "math", "v21", AgentVersionStatus.PUBLISHED.getCode());
        invalid.setGraphConfig("{\"startNode\":\"missing\",\"endNode\":\"n\",\"nodes\":{\"n\":{}}}");
        AgentDefBO definition = new AgentDefBO();
        definition.setAgentId("math");
        definition.setCurrentVersion("v1");
        when(repository.selectVersionById(ACTOR.tenantId(), 21L)).thenReturn(invalid);
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(definition);

        assertThrows(IllegalArgumentException.class,
                () -> service.activateVersion(ACTOR, "math", 21L));
        assertEquals("v1", definition.getCurrentVersion());
        verify(repository, never()).update(eq(ACTOR.tenantId()), any(AgentDefBO.class));
    }

    @Test
    void activeVersionCannotBeArchivedBeforeReplacement() {
        AgentVersionBO version = version(12L, "math", "v2", AgentVersionStatus.PUBLISHED.getCode());
        AgentDefBO definition = new AgentDefBO();
        definition.setAgentId("math");
        definition.setCurrentVersion("v2");
        when(repository.selectVersionById(ACTOR.tenantId(), 12L)).thenReturn(version);
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(definition);

        assertThrows(IllegalStateException.class,
                () -> service.archiveVersion(ACTOR, "math", 12L));
        assertEquals(AgentVersionStatus.PUBLISHED.getCode(), version.getStatus());
        verify(repository, never()).updateVersion(eq(ACTOR.tenantId()), any());
    }

    @Test
    void publishedVersionCannotBeDeletedAroundArchiveLifecycle() {
        AgentVersionBO published = version(16L, "math", "v2", AgentVersionStatus.PUBLISHED.getCode());
        when(repository.selectVersionById(ACTOR.tenantId(), 16L)).thenReturn(published);

        assertThrows(IllegalStateException.class,
                () -> service.deleteVersion(ACTOR, "math", 16L));

        verify(repository, never()).deleteVersionById(ACTOR.tenantId(), 16L);
    }

    @Test
    void publishRejectsMissingAndStructurallyInvalidGraphs() {
        AgentVersionBO version = version(9L, "math", "v9", AgentVersionStatus.DRAFT.getCode());
        when(repository.selectVersionById(ACTOR.tenantId(), 9L)).thenReturn(version);
        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 9L));
        assertEquals(AgentVersionStatus.DRAFT.getCode(), version.getStatus());

        version.setGraphConfig("not-json");
        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 9L));
        assertEquals(AgentVersionStatus.DRAFT.getCode(), version.getStatus());

        version.setGraphConfig("{\"startNode\":\"missing\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}}}");
        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 9L));
        assertEquals(AgentVersionStatus.DRAFT.getCode(), version.getStatus());

        version.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}},\"conditionalEdges\":{\"n\":{\"defaultTarget\":\"missing\"}}}");
        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 9L));
        assertEquals(AgentVersionStatus.DRAFT.getCode(), version.getStatus());
        verify(repository, never()).updateVersion(eq(ACTOR.tenantId()), any());
    }

    @Test
    void publishRejectsConditionalRoutesWithoutAUsableDestination() {
        AgentVersionBO draft = version(17L, "math", "v17", AgentVersionStatus.DRAFT.getCode());
        when(repository.selectVersionById(ACTOR.tenantId(), 17L)).thenReturn(draft);
        String base = "{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}},\"conditionalEdges\":{\"n\":";

        draft.setGraphConfig(base + "null}}");
        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 17L));
        draft.setGraphConfig(base + "{\"nodeMappings\":{},\"defaultTarget\":\"\"}}}");
        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 17L));

        assertEquals(AgentVersionStatus.DRAFT.getCode(), draft.getStatus());
        verify(repository, never()).updateVersion(eq(ACTOR.tenantId()), any());
    }

    @Test
    void publishRejectsNullNodeConfigurationThatRuntimeCannotLoad() {
        AgentVersionBO draft = version(22L, "math", "v22", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":null}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 22L)).thenReturn(draft);

        assertThrows(IllegalArgumentException.class,
                () -> service.publishVersion(ACTOR, "math", 22L));
        assertEquals(AgentVersionStatus.DRAFT.getCode(), draft.getStatus());
        verify(repository, never()).updateVersion(eq(ACTOR.tenantId()), any());
    }

    @Test
    void publishedVersionGraphCannotBeChangedInPlace() {
        AgentVersionBO published = version(10L, "math", "v10", AgentVersionStatus.PUBLISHED.getCode());
        published.setGraphConfig("{\"startNode\":\"n\",\"endNode\":\"n\",\"nodes\":{\"n\":{\"nodeType\":\"DEFAULT\"}}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 10L)).thenReturn(published);
        String originalGraph = published.getGraphConfig();
        GraphConfigRequest replacement = new GraphConfigRequest();
        NodeConfigRequest node = new NodeConfigRequest();
        node.setNodeId("other");
        EdgeRequest edge = new EdgeRequest();
        edge.setSourceNodeId("n");
        edge.setTargetNodeId("other");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.updateGraphConfig(ACTOR, "math", 10L, replacement)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.addNode(ACTOR, "math", 10L, node)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.updateNode(ACTOR, "math", 10L, "n", node)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.deleteNode(ACTOR, "math", 10L, "n")),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.addEdge(ACTOR, "math", 10L, edge)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.deleteEdge(ACTOR, "math", 10L, "n", "other")));
        assertEquals(originalGraph, published.getGraphConfig());
        verify(repository, never()).updateVersion(eq(ACTOR.tenantId()), any());
    }

    @Test
    void editsGraphNodesAndEdgesAndReturnsValidation() {
        AgentVersionBO version = version(3L, "math", "v1", AgentVersionStatus.DRAFT.getCode());
        version.setGraphConfig("{}");
        when(repository.selectVersionById(ACTOR.tenantId(), 3L)).thenReturn(version);
        GraphConfigRequest graph = new GraphConfigRequest();
        graph.setName("graph");
        graph.setStartNode("start");
        graph.setEndNode("end");
        graph.setNodes(
                Map.of(
                        "start",
                        new GraphConfigRequest.NodeConfigDTO(),
                        "end",
                        new GraphConfigRequest.NodeConfigDTO()));
        graph.setEdges(Map.of("start", List.of("end")));
        service.updateGraphConfig(ACTOR, "math", 3L, graph);
        NodeConfigRequest node = new NodeConfigRequest();
        node.setNodeId("start");
        node.setNodeName("Start");
        node.setNodeType("DEFAULT");
        service.addNode(ACTOR, "math", 3L, node);
        EdgeRequest edge = new EdgeRequest();
        edge.setSourceNodeId("start");
        edge.setTargetNodeId("end");
        service.addEdge(ACTOR, "math", 3L, edge);
        service.deleteEdge(ACTOR, "math", 3L, "start", "end");
        service.deleteNode(ACTOR, "math", 3L, "start");
        verify(repository, atLeast(4)).updateVersion(ACTOR.tenantId(), version);
        assertTrue(service.validateGraphConfig(graph).isValid());
    }

    @Test
    void rejectsMissingAgentsVersionsAndDuplicateNumbers() {
        when(repository.selectByAgentId(ACTOR.tenantId(), "missing")).thenReturn(null);
        VersionRequest request = new VersionRequest();
        request.setVersionNumber("v1");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createVersion(ACTOR, "missing", request));
        AgentDefBO def = new AgentDefBO();
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        AgentVersionBO existing = version(1L, "math", "v1", 0);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v1"))
                .thenReturn(existing);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.createVersion(ACTOR, "math", request));
    }

    @Test
    void copyingVersionRequiresAnExistingSourceOwnedByTheSameAgent() {
        AgentDefBO target = new AgentDefBO();
        target.setAgentId("math");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(target);
        VersionRequest request = new VersionRequest();
        request.setVersionNumber("v2");
        request.setCopyFromVersionId(40L);

        assertThrows(IllegalArgumentException.class,
                () -> service.createVersion(ACTOR, "math", request));
        verify(repository, never()).createVersion(eq(ACTOR.tenantId()), any());

        AgentVersionBO foreign = version(40L, "other", "v1", AgentVersionStatus.PUBLISHED.getCode());
        foreign.setGraphConfig("{\"startNode\":\"private\"}");
        when(repository.selectVersionById(ACTOR.tenantId(), 40L)).thenReturn(foreign);
        assertThrows(IllegalArgumentException.class,
                () -> service.createVersion(ACTOR, "math", request));
        verify(repository, never()).createVersion(eq(ACTOR.tenantId()), any());

        AgentVersionBO source = version(40L, "math", "v1", AgentVersionStatus.PUBLISHED.getCode());
        source.setGraphConfig("{\"startNode\":\"copy\"}");
        source.setCanvasConfig("{\"zoom\":2}");
        when(repository.selectVersionById(ACTOR.tenantId(), 40L)).thenReturn(source);
        service.createVersion(ACTOR, "math", request);
        verify(repository).createVersion(eq(ACTOR.tenantId()), argThat(created ->
                "{\"startNode\":\"copy\"}".equals(created.getGraphConfig())
                        && "{\"zoom\":2}".equals(created.getCanvasConfig())));
    }

    @Test
    void deletingByVersionIdCannotDeleteAnotherAgentsDraft() {
        AgentVersionBO foreign = version(41L, "other", "v1",
                AgentVersionStatus.DRAFT.getCode());
        when(repository.selectVersionById(ACTOR.tenantId(), 41L)).thenReturn(foreign);

        service.deleteVersion(ACTOR, "math", 41L);

        verify(repository, never()).deleteVersionById(ACTOR.tenantId(), 41L);
        verify(repository, never()).updateVersion(eq(ACTOR.tenantId()), any());
    }

    @Test
    void coversVersionQueriesUpdatesDeletionAndLifecycleGuards() {
        AgentVersionBO v = version(4L, "math", "v4", AgentVersionStatus.DRAFT.getCode());
        v.setDescription("draft");
        AgentDefBO def = new AgentDefBO();
        def.setAgentId("math");
        when(repository.selectVersionsByAgentId(ACTOR.tenantId(), "math")).thenReturn(List.of(v));
        assertEquals(1, service.getVersions(ACTOR, "math").size());
        when(repository.selectVersionById(ACTOR.tenantId(), 4L)).thenReturn(v);
        assertNotNull(service.getVersionDetail(ACTOR, "math", 4L));
        assertNull(service.getVersionDetail(ACTOR, "other", 4L));
        VersionRequest update = new VersionRequest();
        update.setDescription("updated");
        assertEquals("updated", service.updateVersion(ACTOR, "math", 4L, update).getDescription());
        service.deleteVersion(ACTOR, "math", 4L);
        verify(repository).deleteVersionById(ACTOR.tenantId(), 4L);
        assertNull(service.getCanvasConfig(ACTOR, "other", 4L));
        v.setCanvasConfig("{}");
        service.updateCanvasConfig(ACTOR, "math", 4L, "canvas");
        assertEquals("canvas", v.getCanvasConfig());
        v.setStatus(AgentVersionStatus.PUBLISHED.getCode());
        assertThrows(
                IllegalArgumentException.class, () -> service.publishVersion(ACTOR, "math", 4L));
        v.setStatus(AgentVersionStatus.ARCHIVED.getCode());
        assertThrows(
                IllegalArgumentException.class, () -> service.archiveVersion(ACTOR, "math", 4L));
        v.setStatus(AgentVersionStatus.DRAFT.getCode());
        assertThrows(
                IllegalArgumentException.class, () -> service.activateVersion(ACTOR, "math", 4L));
        when(repository.selectVersionById(ACTOR.tenantId(), 99L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateVersion(ACTOR, "math", 99L, update));
    }

    @Test
    void editsConditionalAndDuplicateGraphEdgesAndNodeMetadata() {
        AgentVersionBO v = version(5L, "math", "v5", AgentVersionStatus.DRAFT.getCode());
        v.setGraphConfig(
                "{\"nodes\":{\"start\":{\"nodeName\":\"old\",\"nodeType\":\"DEFAULT\"}},\"edges\":{\"start\":[\"end\"]},\"conditionalEdges\":{}}"
                    + " ");
        when(repository.selectVersionById(ACTOR.tenantId(), 5L)).thenReturn(v);
        NodeConfigRequest update = new NodeConfigRequest();
        update.setNodeName("new");
        update.setDescription("updated description");
        update.setNodeType("DEFAULT");
        update.setEnabled(false);
        update.setTimeout(42L);
        update.setRetryCount(2);
        update.setErrorStrategy("SKIP");
        update.setConfig(Map.of("prompt", "hello"));
        service.updateNode(ACTOR, "math", 5L, "start", update);
        Map<String, Object> savedGraph = JSONUtils.parseMap(v.getGraphConfig());
        @SuppressWarnings("unchecked")
        Map<String, Object> savedNode = (Map<String, Object>)
                ((Map<String, Object>) savedGraph.get("nodes")).get("start");
        assertEquals("updated description", savedNode.get("description"));
        assertEquals(Map.of("prompt", "hello"), savedNode.get("config"));
        EdgeRequest duplicate = new EdgeRequest();
        duplicate.setSourceNodeId("start");
        duplicate.setTargetNodeId("end");
        service.addEdge(ACTOR, "math", 5L, duplicate);
        EdgeRequest conditional = new EdgeRequest();
        conditional.setSourceNodeId("start");
        conditional.setConditionType("INTENT");
        conditional.setDefaultTarget("end");
        conditional.setConditionMappings(Map.of("yes", "end"));
        service.addEdge(ACTOR, "math", 5L, conditional);
        service.deleteEdge(ACTOR, "math", 5L, "start", "end");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateNode(ACTOR, "math", 5L, "missing", update));
        service.deleteNode(ACTOR, "math", 5L, "start");
        verify(repository, atLeast(5)).updateVersion(ACTOR.tenantId(), v);
    }

    @Test
    void unknownStoredVersionStatusStillAllowsListingAndDetailInspection() {
        AgentVersionBO legacy = version(19L, "math", "legacy", 99);
        when(repository.selectVersionsByAgentId(ACTOR.tenantId(), "math"))
                .thenReturn(List.of(legacy));
        when(repository.selectVersionById(ACTOR.tenantId(), 19L))
                .thenReturn(legacy);

        assertNull(service.getVersions(ACTOR, "math").getFirst().getStatusDesc());
        assertNull(service.getVersionDetail(ACTOR, "math", 19L).getStatusDesc());
    }

    @Test
    @SuppressWarnings("unchecked")
    void partialNodeEditPreservesUnmentionedRuntimeSettings() {
        AgentVersionBO draft = version(17L, "math", "v17", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"nodes\":{\"start\":{\"nodeName\":\"old\","
                + "\"description\":\"keep\",\"nodeType\":\"DEFAULT\",\"enabled\":false,"
                + "\"timeout\":123,\"retryCount\":2,\"errorStrategy\":\"SKIP\","
                + "\"config\":{\"prompt\":\"keep\"}}},\"edges\":{},\"conditionalEdges\":{}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 17L)).thenReturn(draft);
        NodeConfigRequest change = new NodeConfigRequest();
        change.setNodeName("new");

        service.updateNode(ACTOR, "math", 17L, "start", change);

        Map<String, Object> graph = JSONUtils.parseMap(draft.getGraphConfig());
        Map<String, Object> nodes = (Map<String, Object>) graph.get("nodes");
        Map<String, Object> node = (Map<String, Object>) nodes.get("start");
        assertEquals("new", node.get("nodeName"));
        assertEquals("keep", node.get("description"));
        assertEquals(false, node.get("enabled"));
        assertEquals(123, ((Number) node.get("timeout")).intValue());
        assertEquals(2, ((Number) node.get("retryCount")).intValue());
        assertEquals("SKIP", node.get("errorStrategy"));
        assertEquals(Map.of("prompt", "keep"), node.get("config"));
    }

    @Test
    void deletingNodeRemovesConditionalRoutesThatPointToIt() {
        AgentVersionBO draft = version(15L, "math", "v15", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"startNode\":\"start\",\"endNode\":\"finish\","
                + "\"nodes\":{\"start\":{\"nodeType\":\"DEFAULT\"},"
                + "\"choice\":{\"nodeType\":\"DEFAULT\"},"
                + "\"finish\":{\"nodeType\":\"DEFAULT\"}},"
                + "\"edges\":{},\"conditionalEdges\":{\"start\":{"
                + "\"defaultTarget\":\"choice\","
                + "\"nodeMappings\":{\"yes\":\"finish\",\"no\":\"choice\"}}}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 15L)).thenReturn(draft);

        service.deleteNode(ACTOR, "math", 15L, "choice");

        GraphConfigRequest remaining = JSONUtils.parseObject(draft.getGraphConfig(),
                new tools.jackson.core.type.TypeReference<GraphConfigRequest>() {});
        assertTrue(service.validateGraphConfig(remaining).isValid());
        assertFalse(draft.getGraphConfig().contains("\"choice\""));
    }

    @Test
    void deletingTheSoleConditionalDestinationRemovesTheEmptyRoute() {
        AgentVersionBO draft = version(23L, "math", "v23", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"startNode\":\"start\",\"endNode\":\"start\","
                + "\"nodes\":{\"start\":{\"nodeType\":\"DEFAULT\"},"
                + "\"choice\":{\"nodeType\":\"DEFAULT\"}},\"edges\":{},"
                + "\"conditionalEdges\":{\"start\":{\"defaultTarget\":\"choice\","
                + "\"nodeMappings\":{\"yes\":\"choice\"}}}} ");
        when(repository.selectVersionById(ACTOR.tenantId(), 23L)).thenReturn(draft);

        service.deleteNode(ACTOR, "math", 23L, "choice");

        GraphConfigRequest remaining = JSONUtils.parseObject(draft.getGraphConfig(),
                new tools.jackson.core.type.TypeReference<GraphConfigRequest>() {});
        assertTrue(remaining.getConditionalEdges().isEmpty());
        assertTrue(service.validateGraphConfig(remaining).isValid());
    }

    @Test
    void deletingTheSoleOrdinaryDestinationRemovesTheEmptyEdge() {
        AgentVersionBO draft = version(24L, "math", "v24", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"startNode\":\"start\",\"endNode\":\"start\","
                + "\"nodes\":{\"start\":{\"nodeType\":\"DEFAULT\"},"
                + "\"choice\":{\"nodeType\":\"DEFAULT\"}},"
                + "\"edges\":{\"start\":[\"choice\"]},\"conditionalEdges\":{}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 24L)).thenReturn(draft);

        service.deleteNode(ACTOR, "math", 24L, "choice");

        GraphConfigRequest remaining = JSONUtils.parseObject(draft.getGraphConfig(),
                new tools.jackson.core.type.TypeReference<GraphConfigRequest>() {});
        assertTrue(remaining.getEdges().isEmpty());
        assertTrue(service.validateGraphConfig(remaining).isValid());
    }

    @Test
    void deletingUnrelatedNodeKeepsConditionalRouteExecutable() {
        AgentVersionBO draft = version(18L, "math", "v18", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"startNode\":\"start\",\"endNode\":\"finish\","
                + "\"nodes\":{\"start\":{\"nodeType\":\"DEFAULT\"},"
                + "\"finish\":{\"nodeType\":\"DEFAULT\"},"
                + "\"orphan\":{\"nodeType\":\"DEFAULT\"}},\"edges\":{},"
                + "\"conditionalEdges\":{\"start\":{\"defaultTarget\":\"finish\","
                + "\"nodeMappings\":{\"yes\":\"finish\"}}}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 18L)).thenReturn(draft);

        service.deleteNode(ACTOR, "math", 18L, "orphan");

        GraphConfigRequest remaining = JSONUtils.parseObject(draft.getGraphConfig(),
                new tools.jackson.core.type.TypeReference<GraphConfigRequest>() {});
        assertTrue(service.validateGraphConfig(remaining).isValid());
        assertEquals("finish", remaining.getConditionalEdges().get("start").getDefaultTarget());
        assertEquals(Map.of("yes", "finish"),
                remaining.getConditionalEdges().get("start").getNodeMappings());
    }

    @Test
    @SuppressWarnings("unchecked")
    void deletingOneEdgePreservesUnrelatedConditionalRoutes() {
        AgentVersionBO draft = version(16L, "math", "v16", AgentVersionStatus.DRAFT.getCode());
        draft.setGraphConfig("{\"nodes\":{},\"edges\":{\"start\":[\"finish\"]},"
                + "\"conditionalEdges\":{\"start\":{\"defaultTarget\":\"right\","
                + "\"nodeMappings\":{\"yes\":\"left\",\"no\":\"right\"}}}}");
        when(repository.selectVersionById(ACTOR.tenantId(), 16L)).thenReturn(draft);

        service.deleteEdge(ACTOR, "math", 16L, "start", "finish");

        Map<String, Object> graph = JSONUtils.parseMap(draft.getGraphConfig());
        Map<String, Object> routes = (Map<String, Object>) graph.get("conditionalEdges");
        Map<String, Object> route = (Map<String, Object>) routes.get("start");
        assertEquals("right", route.get("defaultTarget"));
        assertEquals(Map.of("yes", "left", "no", "right"), route.get("nodeMappings"));

        service.deleteEdge(ACTOR, "math", 16L, "start", "left");

        graph = JSONUtils.parseMap(draft.getGraphConfig());
        routes = (Map<String, Object>) graph.get("conditionalEdges");
        route = (Map<String, Object>) routes.get("start");
        assertEquals("right", route.get("defaultTarget"));
        assertEquals(Map.of("no", "right"), route.get("nodeMappings"));

        service.deleteEdge(ACTOR, "math", 16L, "start", "right");

        graph = JSONUtils.parseMap(draft.getGraphConfig());
        routes = (Map<String, Object>) graph.get("conditionalEdges");
        assertFalse(routes.containsKey("start"));
    }

    @Test
    void copiesGraphAndExtractsRequiredInputsWhileHandlingMalformedConfig() {
        AgentDefBO def = new AgentDefBO();
        def.setAgentId("math");
        AgentVersionBO source = version(6L, "math", "v1", AgentVersionStatus.DRAFT.getCode());
        source.setGraphConfig("{\"nodes\":{\"n1\":{\"nodeType\":\"DEFAULT\"}}}");
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v2"))
                .thenReturn(null);
        when(repository.selectVersionById(ACTOR.tenantId(), 6L)).thenReturn(source);
        when(repository.createVersion(eq(ACTOR.tenantId()), any(AgentVersionBO.class)))
                .thenAnswer(i -> i.getArgument(1));
        VersionRequest copy = new VersionRequest();
        copy.setVersionNumber("v2");
        copy.setCopyFromVersionId(6L);
        service.createVersion(ACTOR, "math", copy);
        AgentVersionBO target = version(7L, "math", "v7", AgentVersionStatus.DRAFT.getCode());
        target.setGraphConfig("not-json");
        when(repository.selectVersionById(ACTOR.tenantId(), 7L)).thenReturn(target);
        assertThrows(
                RuntimeException.class,
                () -> service.addNode(ACTOR, "math", 7L, new NodeConfigRequest()));
        var emptyGraph = service.validateGraphConfig(new GraphConfigRequest());
        assertFalse(emptyGraph.isValid());
        assertEquals(List.of("图配置校验失败，请检查配置"), emptyGraph.getErrors());
        BaseNode node = mock(BaseNode.class);
        when(node.getRequiredInputs())
                .thenReturn(List.of(NodeInputParam.apiRequired("question", "string", "q")));
        when(nodeFactory.createNode(any())).thenReturn(node);
        target.setGraphConfig("{\"nodes\":{\"n1\":{\"nodeType\":\"DEFAULT\"}},\"edges\":{}}");
        NodeConfigRequest add = new NodeConfigRequest();
        add.setNodeId("n2");
        add.setNodeName("N2");
        add.setNodeType("DEFAULT");
        service.addNode(ACTOR, "math", 7L, add);
        assertNotNull(target.getExtInfo());
    }

    @Test
    void validatesGraphNodesAndEdgesInsteadOfAcceptingEveryConfiguration() {
        GraphConfigRequest valid = new GraphConfigRequest();
        valid.setStartNode("start");
        valid.setEndNode("end");
        GraphConfigRequest.NodeConfigDTO start = new GraphConfigRequest.NodeConfigDTO();
        GraphConfigRequest.NodeConfigDTO end = new GraphConfigRequest.NodeConfigDTO();
        GraphConfigRequest.NodeConfigDTO orphan = new GraphConfigRequest.NodeConfigDTO();
        valid.setNodes(new LinkedHashMap<>(Map.of("start", start, "end", end, "orphan", orphan)));
        valid.setEdges(Map.of("start", List.of("end")));

        var validResult = service.validateGraphConfig(valid);
        assertTrue(validResult.isValid());
        assertEquals(List.of("节点不可达: orphan"), validResult.getWarnings());

        GraphConfigRequest conditional = new GraphConfigRequest();
        conditional.setStartNode("start");
        conditional.setNodes(new LinkedHashMap<>(Map.of("start", start, "end", end)));
        GraphConfigRequest.ConditionalEdgeDTO conditionalEdge =
                new GraphConfigRequest.ConditionalEdgeDTO();
        conditionalEdge.setDefaultTarget("end");
        conditionalEdge.setNodeMappings(Map.of("next", "end"));
        conditional.setConditionalEdges(Map.of("start", conditionalEdge));
        assertTrue(service.validateGraphConfig(conditional).isValid());

        GraphConfigRequest invalid = new GraphConfigRequest();
        invalid.setStartNode("start");
        invalid.setNodes(Map.of("start", start));
        invalid.setEdges(Map.of("start", List.of("missing")));
        var invalidResult = service.validateGraphConfig(invalid);
        assertFalse(invalidResult.isValid());
        assertEquals(List.of("图配置校验失败，请检查配置"), invalidResult.getErrors());
    }

    @Test
    void coversVersionEdgeCasesAndEmptyGraphMetadata() {
        AgentDefBO def = new AgentDefBO();
        def.setAgentId("math");
        AgentVersionBO v = version(8L, "math", "v8", AgentVersionStatus.DRAFT.getCode());
        when(repository.selectByAgentId(ACTOR.tenantId(), "math")).thenReturn(def);
        when(repository.selectVersionByAgentIdAndNumber(ACTOR.tenantId(), "math", "v9"))
                .thenReturn(null);
        when(repository.selectVersionById(ACTOR.tenantId(), 8L)).thenReturn(v);
        when(repository.createVersion(eq(ACTOR.tenantId()), any(AgentVersionBO.class)))
                .thenAnswer(i -> i.getArgument(1));
        VersionRequest copy = new VersionRequest();
        copy.setVersionNumber("v9");
        copy.setCopyFromVersionId(999L);
        when(repository.selectVersionById(ACTOR.tenantId(), 999L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.createVersion(ACTOR, "math", copy));

        VersionRequest noDescription = new VersionRequest();
        service.updateVersion(ACTOR, "math", 8L, noDescription);
        service.deleteVersion(ACTOR, "other", 8L);
        when(repository.selectVersionById(ACTOR.tenantId(), 99L)).thenReturn(null);
        service.deleteVersion(ACTOR, "math", 99L);
        v.setCanvasConfig("canvas");
        assertEquals("canvas", service.getCanvasConfig(ACTOR, "math", 8L));
        assertNotNull(service.getGraphConfig(ACTOR, "math", 8L));
        var invalidGraph = service.validateGraphConfig(null);
        assertFalse(invalidGraph.isValid());
        assertEquals(List.of("图配置校验失败，请检查配置"), invalidGraph.getErrors());

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
        v.setGraphConfig(
                "{\"nodes\":{},\"edges\":{\"n3\":\"not-a-list\"},\"conditionalEdges\":{\"n3\":{}}}");
        service.deleteNode(ACTOR, "math", 8L, "n3");
        v.setGraphConfig("not-json");
        assertNotNull(service.getVersionDetail(ACTOR, "math", 8L));
        v.setStatus(null);
        assertNotNull(service.getVersionDetail(ACTOR, "math", 8L));
    }

    private static AgentVersionBO version(Long id, String agentId, String number, Integer status) {
        AgentVersionBO version = new AgentVersionBO();
        version.setId(id);
        version.setAgentId(agentId);
        version.setVersionNumber(number);
        version.setStatus(status);
        return version;
    }
}
