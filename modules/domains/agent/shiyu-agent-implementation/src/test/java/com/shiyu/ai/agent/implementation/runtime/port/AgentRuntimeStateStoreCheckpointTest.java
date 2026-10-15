package com.shiyu.ai.agent.implementation.runtime.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

/** 验证持久化的执行在重启后仍保留其检查点标识。 */
class AgentRuntimeStateStoreCheckpointTest {
    @Test
    void savesAndRestoresLatestCheckpointIdentity() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeStateStore store = new AgentRuntimeStateStore(
                repository, mock(CheckpointManager.class));
        Execution execution = new Execution("agent-1", "v1", Map.of("message", "hello"));
        execution.start();
        execution.setLastCheckpointId("checkpoint-1");

        store.save(actor, execution);

        ArgumentCaptor<AgentExecutionBO> saved = ArgumentCaptor.forClass(AgentExecutionBO.class);
        verify(repository).insert(eq(actor.tenantId()), saved.capture());
        assertEquals("checkpoint-1", saved.getValue().getLastCheckpointId());
        assertEquals("checkpoint-1", store.rebuild(saved.getValue()).getLastCheckpointId());
    }
}
