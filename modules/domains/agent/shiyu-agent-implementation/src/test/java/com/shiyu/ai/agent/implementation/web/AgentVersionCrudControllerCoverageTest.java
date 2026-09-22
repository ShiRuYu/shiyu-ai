package com.shiyu.ai.agent.implementation.web;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.implementation.request.VersionRequest;
import com.shiyu.ai.agent.implementation.service.AgentVersionService;
import com.shiyu.ai.agent.implementation.vo.AgentVersionDetailVO;
import com.shiyu.ai.agent.implementation.vo.AgentVersionVO;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 验证嵌套智能体版本 CRUD 控制器的资源操作和委托行为。
 */
class AgentVersionCrudControllerCoverageTest {

    @Test
    void delegatesNestedVersionCrudOperationsToService() {
        AgentVersionService service = mock(AgentVersionService.class);
        AgentVersionCrudController controller = new AgentVersionCrudController(service);
        ActorContext actor = new ActorContext(new TenantId(7L), new UserId(9L), false);
        AgentVersionVO version = new AgentVersionVO();
        AgentVersionDetailVO detail = new AgentVersionDetailVO();
        when(service.getVersions(actor, "agent-1")).thenReturn(List.of(version));
        when(service.getVersionDetail(actor, "agent-1", 2L)).thenReturn(detail);
        when(service.createVersion(eq(actor), eq("agent-1"), any())).thenReturn(version);
        when(service.updateVersion(eq(actor), eq("agent-1"), eq(2L), any())).thenReturn(version);

        try (var ignored = mockStatic(ActorContextHttpAdapter.class)) {
            ignored.when(ActorContextHttpAdapter::currentActor).thenReturn(actor);
            assertTrue(controller.getVersions("agent-1").isSuccess());
            assertTrue(controller.getVersionDetail("agent-1", 2L).isSuccess());
            assertTrue(controller.createVersion("agent-1", new VersionRequest()).isSuccess());
            assertTrue(controller.updateVersion("agent-1", 2L, new VersionRequest()).isSuccess());
            assertTrue(controller.deleteVersion("agent-1", 2L).isSuccess());
        }
    }
}
