package com.shiyu.ai.agent.web;

import com.shiyu.ai.agent.request.EdgeRequest;
import com.shiyu.ai.agent.request.GraphConfigRequest;
import com.shiyu.ai.agent.request.NodeConfigRequest;
import com.shiyu.ai.agent.request.VersionRequest;
import com.shiyu.ai.agent.service.AgentVersionService;
import com.shiyu.ai.agent.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.vo.AgentVersionVO;
import com.shiyu.ai.agent.vo.GraphValidationVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentVersionControllerCoverageTest {
    @Test
    void coversVersionLifecycleGraphNodesEdgesAndCanvas() {
        AgentVersionService service = mock(AgentVersionService.class);
        AgentVersionController controller = new AgentVersionController(service);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        AgentVersionDetailVO detail = new AgentVersionDetailVO();
        when(service.getVersions(actor, "a1")).thenReturn(List.of(new AgentVersionVO()));
        when(service.getVersionDetail(actor, "a1", 1L)).thenReturn(detail);
        when(service.createVersion(eq(actor), eq("a1"), any())).thenReturn(new AgentVersionVO());
        when(service.updateVersion(eq(actor), eq("a1"), eq(1L), any())).thenReturn(new AgentVersionVO());
        when(service.copyVersion(eq(actor), eq("a1"), any())).thenReturn(new AgentVersionVO());
        when(service.getGraphConfig(actor, "a1", 1L)).thenReturn(detail);
        when(service.updateGraphConfig(eq(actor), eq("a1"), eq(1L), any())).thenReturn(detail);
        when(service.validateGraphConfig(any())).thenReturn(new GraphValidationVO());
        when(service.getCanvasConfig(actor, "a1", 1L)).thenReturn("{}");
        VersionRequest version = new VersionRequest();
        GraphConfigRequest graph = new GraphConfigRequest();
        NodeConfigRequest node = new NodeConfigRequest();
        EdgeRequest edge = new EdgeRequest();
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.getVersions("a1").isSuccess());
            assertTrue(controller.getVersionDetail("a1", 1L).isSuccess());
            assertTrue(controller.createVersion("a1", version).isSuccess());
            assertTrue(controller.updateVersion("a1", 1L, version).isSuccess());
            assertTrue(controller.deleteVersion("a1", 1L).isSuccess());
            assertTrue(controller.publish("a1", 1L).isSuccess());
            assertTrue(controller.archive("a1", 1L).isSuccess());
            assertTrue(controller.activate("a1", 1L).isSuccess());
            assertTrue(controller.copy("a1", version).isSuccess());
            assertTrue(controller.getGraph("a1", 1L).isSuccess());
            assertTrue(controller.updateGraph("a1", 1L, graph).isSuccess());
            assertTrue(controller.validate("a1", 1L, graph).isSuccess());
            assertTrue(controller.addNode("a1", 1L, node).isSuccess());
            assertTrue(controller.updateNode("a1", 1L, "n1", node).isSuccess());
            assertTrue(controller.deleteNode("a1", 1L, "n1").isSuccess());
            assertTrue(controller.addEdge("a1", 1L, edge).isSuccess());
            assertTrue(controller.deleteEdge("a1", 1L, "n1", "n2").isSuccess());
            assertTrue(controller.getCanvas("a1", 1L).isSuccess());
            assertTrue(controller.updateCanvas("a1", 1L, "{}").isSuccess());
        }
    }

    @Test
    void mapsMissingVersionsAndMutationFailures() {
        AgentVersionService service = mock(AgentVersionService.class);
        AgentVersionController controller = new AgentVersionController(service);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        when(service.getVersionDetail(any(), anyString(), anyLong())).thenReturn(null);
        when(service.getGraphConfig(any(), anyString(), anyLong())).thenReturn(null);
        when(service.createVersion(any(), anyString(), any())).thenThrow(new IllegalStateException());
        doThrow(new IllegalStateException()).when(service).deleteVersion(any(), anyString(), anyLong());
        doThrow(new IllegalStateException()).when(service).publishVersion(any(), anyString(), anyLong());
        doThrow(new IllegalStateException()).when(service).archiveVersion(any(), anyString(), anyLong());
        doThrow(new IllegalStateException()).when(service).activateVersion(any(), anyString(), anyLong());
        when(service.updateVersion(any(), anyString(), anyLong(), any())).thenThrow(new IllegalStateException());
        when(service.copyVersion(any(), anyString(), any())).thenThrow(new IllegalStateException());
        when(service.updateGraphConfig(any(), anyString(), anyLong(), any())).thenThrow(new IllegalStateException());
        doThrow(new IllegalStateException()).when(service).addNode(any(), anyString(), anyLong(), any());
        doThrow(new IllegalStateException()).when(service).updateNode(any(), anyString(), anyLong(), anyString(), any());
        doThrow(new IllegalStateException()).when(service).deleteNode(any(), anyString(), anyLong(), anyString());
        doThrow(new IllegalStateException()).when(service).addEdge(any(), anyString(), anyLong(), any());
        doThrow(new IllegalStateException()).when(service).deleteEdge(any(), anyString(), anyLong(), anyString(), anyString());
        doThrow(new IllegalStateException()).when(service).updateCanvasConfig(any(), anyString(), anyLong(), anyString());
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertFalse(controller.getVersionDetail("a", 1L).isSuccess());
            assertFalse(controller.getGraph("a", 1L).isSuccess());
            assertFalse(controller.createVersion("a", new VersionRequest()).isSuccess());
            assertFalse(controller.deleteVersion("a", 1L).isSuccess());
            assertFalse(controller.publish("a", 1L).isSuccess());
            assertFalse(controller.archive("a", 1L).isSuccess());
            assertFalse(controller.activate("a", 1L).isSuccess());
            assertFalse(controller.updateVersion("a", 1L, new VersionRequest()).isSuccess());
            assertFalse(controller.copy("a", new VersionRequest()).isSuccess());
            assertFalse(controller.updateGraph("a", 1L, new GraphConfigRequest()).isSuccess());
            assertFalse(controller.addNode("a", 1L, new NodeConfigRequest()).isSuccess());
            assertFalse(controller.updateNode("a", 1L, "n", new NodeConfigRequest()).isSuccess());
            assertFalse(controller.deleteNode("a", 1L, "n").isSuccess());
            assertFalse(controller.addEdge("a", 1L, new EdgeRequest()).isSuccess());
            assertFalse(controller.deleteEdge("a", 1L, "n1", "n2").isSuccess());
            assertFalse(controller.updateCanvas("a", 1L, "{}").isSuccess());
        }
    }
}
