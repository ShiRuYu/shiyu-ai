package com.shiyu.ai.web.agent;

import com.shiyu.ai.agent.runtime.AgentRuntime;
import com.shiyu.ai.common.core.domain.UserContext;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("dev")
class ExecutionControllerTest {

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clearContext();
    }

    @Test
    void streamEventsExposeTheActualExecutionId() {
        UserContext context = new UserContext();
        context.setUserId(2L);
        context.setCurrentTenantId(1L);
        context.setCurrentRoleId(1L);
        context.setCurrentRoleCode("super");
        UserContextHolder.setContext(context);

        AgentRuntime runtime = mock(AgentRuntime.class);
        when(runtime.executeStream(eq("agent-1"), any()))
                .thenReturn(Flux.just(Map.of("executionId", "execution-1", "node", "start")));
        ExecutionController controller = new ExecutionController(runtime);

        var result = controller.executeStream("agent-1", Map.of("message", "hello")).blockFirst();

        assertEquals(200, result.getCode());
        assertEquals("execution-1", result.getData().get("executionId"));
        assertEquals("execution-1", ((Map<?, ?>) result.getData().get("data")).get("executionId"));
    }
}
