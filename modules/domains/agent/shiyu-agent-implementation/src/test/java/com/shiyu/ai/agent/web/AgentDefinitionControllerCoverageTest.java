package com.shiyu.ai.agent.web;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.graph.Graph;
import com.shiyu.ai.agent.request.AgentRequest;
import com.shiyu.ai.agent.service.AgentAdminService;
import com.shiyu.ai.agent.service.AgentService;
import com.shiyu.ai.agent.vo.AgentDetailVO;
import com.shiyu.ai.agent.vo.AgentVO;
import com.shiyu.ai.agent.vo.NodeTypeMetaVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AgentDefinitionControllerCoverageTest {
    @Test
    void mapsCrudRegistrationVersionAndNodeTypeOperations() {
        AgentAdminService admin = mock(AgentAdminService.class);
        AgentService service = mock(AgentService.class);
        AgentDefinitionController controller = new AgentDefinitionController(admin, service);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        AgentVO vo = new AgentVO();
        NodeTypeMetaVO node = new NodeTypeMetaVO();
        node.setCode("llm");
        when(admin.getPage(actor, 2, 10, "a", 1)).thenReturn(Pair.of(1L, List.of(vo)));
        when(admin.getById(actor, 3L)).thenReturn(new AgentDetailVO());
        when(admin.create(eq(actor), any())).thenReturn(vo);
        when(admin.update(eq(actor), eq(3L), any())).thenReturn(vo);
        when(admin.getNodeTypes()).thenReturn(List.of(node));
        when(admin.listAllOptions(actor)).thenReturn(List.of());
        when(service.getAgent(actor, "a1")).thenReturn(AgentDefinition.builder().agentId("a1").name("A").build());
        when(service.unregisterAgent(actor, "a1")).thenReturn(true);
        when(service.switchVersion(actor, "a1", "v2")).thenReturn(true);
        when(service.listAgents(actor)).thenReturn(List.of());
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.getPage(2, 10, "a", 1).isSuccess());
            assertTrue(controller.getById(3L).isSuccess());
            assertTrue(controller.create(new AgentRequest()).isSuccess());
            assertTrue(controller.update(3L, new AgentRequest()).isSuccess());
            assertTrue(controller.delete(3L).isSuccess());
            assertTrue(controller.updateStatus(3L, 1).isSuccess());
            assertTrue(controller.listAllOptions().isSuccess());
            AgentDefinitionController.RegisterAgentRequest request = new AgentDefinitionController.RegisterAgentRequest();
            request.setAgentId("a1"); request.setName("A"); request.setGraph(mock(Graph.class));
            assertTrue(controller.registerAgent(request).isSuccess());
            assertTrue(controller.getAgent("a1").isSuccess());
            assertTrue(controller.deleteAgent("a1").isSuccess());
            assertTrue(controller.switchVersion("a1", "v2").isSuccess());
            assertTrue(controller.listAgents().isSuccess());
            assertTrue(controller.getNodeTypes().isSuccess());
            assertTrue(controller.getNodeType("LLM").isSuccess());
            assertFalse(controller.getNodeType("missing").isSuccess());
        }
    }

    @Test
    void mapsNotFoundAndServiceFailures() {
        AgentAdminService admin = mock(AgentAdminService.class);
        AgentService service = mock(AgentService.class);
        AgentDefinitionController controller = new AgentDefinitionController(admin, service);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        when(admin.getById(any(), anyLong())).thenReturn(null);
        when(admin.create(any(), any())).thenThrow(new IllegalStateException("bad"));
        when(admin.update(any(), anyLong(), any())).thenThrow(new IllegalStateException("bad"));
        doThrow(new IllegalStateException("bad")).when(admin).deleteById(any(), anyLong());
        when(service.getAgent(any(), anyString())).thenReturn(null);
        when(service.unregisterAgent(any(), anyString())).thenReturn(false);
        when(service.switchVersion(any(), anyString(), anyString())).thenReturn(false);
        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertFalse(controller.getById(1L).isSuccess());
            assertFalse(controller.create(new AgentRequest()).isSuccess());
            assertFalse(controller.update(1L, new AgentRequest()).isSuccess());
            assertFalse(controller.delete(1L).isSuccess());
            assertFalse(controller.getAgent("missing").isSuccess());
            assertFalse(controller.deleteAgent("missing").isSuccess());
            assertFalse(controller.switchVersion("a", "v").isSuccess());
        }
    }
}
