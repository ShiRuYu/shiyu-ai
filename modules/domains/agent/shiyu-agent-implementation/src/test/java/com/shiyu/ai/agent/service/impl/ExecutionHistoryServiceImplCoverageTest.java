package com.shiyu.ai.agent.service.impl;

import com.shiyu.ai.agent.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExecutionHistoryServiceImplCoverageTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(5), new UserId(8), false);

    @Test
    void startsAndCompletesExecutionInTenantScope() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        ExecutionHistoryServiceImpl service = new ExecutionHistoryServiceImpl(repository);
        String id = service.startExecution(ACTOR, "agent", "v1", "session", "node", "HTTP", "{\"q\":1}");
        assertNotNull(id);
        AgentExecutionBO row = new AgentExecutionBO(); row.setExecutionId(id); row.setStartTime(java.time.LocalDateTime.now().minusSeconds(1));
        when(repository.selectByExecutionId(ACTOR.tenantId(), id)).thenReturn(row);
        service.completeExecution(ACTOR, id, "{\"ok\":true}", 1, null);
        assertEquals(1, row.getStatus());
        verify(repository).insert(eq(ACTOR.tenantId()), any(AgentExecutionBO.class));
        verify(repository).update(ACTOR.tenantId(), row);
    }

    @Test
    void completionRequiresAnExistingExecution() {
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        when(repository.selectByExecutionId(ACTOR.tenantId(), "missing")).thenReturn(null);
        ExecutionHistoryServiceImpl service = new ExecutionHistoryServiceImpl(repository);
        assertThrows(IllegalStateException.class, () -> service.completeExecution(ACTOR, "missing", "", 2, "not found"));
    }
}
