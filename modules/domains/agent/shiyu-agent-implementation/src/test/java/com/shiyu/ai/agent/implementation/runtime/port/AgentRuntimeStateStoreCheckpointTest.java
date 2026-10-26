package com.shiyu.ai.agent.implementation.runtime.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
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

    @Test
    void compareAndSetLossReloadsAndReconcilesPersistedCancellation() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeStateStore store = new AgentRuntimeStateStore(
                repository, mock(CheckpointManager.class));
        Execution stale = new Execution("agent-1", "v1", Map.of("message", "hello"));
        stale.start();
        AgentExecutionBO persisted = new AgentExecutionBO();
        persisted.setExecutionId(stale.getExecutionId());
        persisted.setAgentId("agent-1");
        persisted.setVersion("v1");
        persisted.setTenantId(actor.tenantId().value());
        persisted.setUserId(actor.userId().value());
        persisted.setInputData("{\"tenantId\":9,\"userId\":7}");
        persisted.setOutputData("{}");
        persisted.setStatus(4);
        persisted.setStartTime(LocalDateTime.now().minusSeconds(1));
        persisted.setEndTime(LocalDateTime.now());
        persisted.setDurationMs(1000L);
        AgentExecutionBO running = new AgentExecutionBO();
        running.setExecutionId(stale.getExecutionId());
        running.setAgentId("agent-1");
        running.setVersion("v1");
        running.setTenantId(actor.tenantId().value());
        running.setUserId(actor.userId().value());
        running.setInputData("{\"tenantId\":9,\"userId\":7}");
        running.setOutputData("{}");
        running.setStatus(0);
        running.setStartTime(persisted.getStartTime());
        when(repository.selectByExecutionId(actor.tenantId(), stale.getExecutionId()))
                .thenReturn(running, persisted);
        when(repository.updateIfStatus(any(), any(), any())).thenReturn(0);
        Execution reconciled = store.save(actor, stale);

        assertEquals(ExecutionStatus.CANCELLED, reconciled.getStatus());
        verify(repository).updateIfStatus(
                eq(actor.tenantId()), any(AgentExecutionBO.class), eq(0));
        assertEquals(4, persisted.getStatus());
        assertEquals(1000L, reconciled.getDurationMs());
    }

    @Test
    void statusRefreshDoesNotEraseAComputedCompletionBeforeItIsSaved() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeStateStore store = new AgentRuntimeStateStore(
                repository, mock(CheckpointManager.class));
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.start();
        AgentExecutionBO persisted = new AgentExecutionBO();
        persisted.setExecutionId(execution.getExecutionId());
        persisted.setAgentId("agent-1");
        persisted.setVersion("v1");
        persisted.setTenantId(actor.tenantId().value());
        persisted.setUserId(actor.userId().value());
        persisted.setInputData("{}");
        persisted.setOutputData("{}");
        persisted.setStatus(0);
        persisted.setStartTime(LocalDateTime.now().minusSeconds(1));
        when(repository.selectByExecutionId(actor.tenantId(), execution.getExecutionId()))
                .thenReturn(persisted);
        when(repository.updateIfStatus(any(), any(), eq(0))).thenReturn(1);

        assertTrue(execution.complete(Map.of("answer", "done")));
        store.effective(actor, execution);
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals(Map.of("answer", "done"), execution.getOutput());
        assertEquals(ExecutionStatus.COMPLETED, store.save(actor, execution).getStatus());
    }

    @Test
    void statusRefreshAfterAnotherRuntimeResumesSupersedesTheOldPausedWorker() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeStateStore store = new AgentRuntimeStateStore(
                repository, mock(CheckpointManager.class));
        Execution execution = new Execution("agent-1", "v1", Map.of());
        execution.start();
        execution.pause();
        AgentExecutionBO persisted = new AgentExecutionBO();
        persisted.setExecutionId(execution.getExecutionId());
        persisted.setAgentId("agent-1");
        persisted.setVersion("v1");
        persisted.setTenantId(actor.tenantId().value());
        persisted.setUserId(actor.userId().value());
        persisted.setInputData("{}");
        persisted.setOutputData("{}");
        persisted.setStatus(0);
        when(repository.selectByExecutionId(actor.tenantId(), execution.getExecutionId()))
                .thenReturn(persisted);
        execution.setStateRefresh(() -> store.effective(actor, execution));

        assertFalse(execution.awaitResumeOrCancellation(1));
        assertEquals(ExecutionStatus.RUNNING, execution.getStatus());
        assertTrue(execution.isSuperseded());
        assertFalse(execution.complete(Map.of("answer", "duplicate")));
    }

    @Test
    void checkpointSavedDuringPauseAdvancesThePausedExecutionPointer() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeStateStore store = new AgentRuntimeStateStore(
                repository, mock(CheckpointManager.class));
        Execution staleRunning = new Execution("agent-1", "v1", Map.of());
        staleRunning.start();
        AgentExecutionBO paused = new AgentExecutionBO();
        paused.setExecutionId(staleRunning.getExecutionId());
        paused.setAgentId("agent-1");
        paused.setVersion("v1");
        paused.setTenantId(actor.tenantId().value());
        paused.setUserId(actor.userId().value());
        paused.setInputData("{\"tenantId\":9,\"userId\":7}");
        paused.setOutputData("{}");
        paused.setStatus(3);
        paused.setLastCheckpointId("older-checkpoint");
        when(repository.updateCheckpointIfStatus(
                actor.tenantId(), staleRunning.getExecutionId(), "new-checkpoint", 0))
                .thenReturn(0);
        when(repository.selectByExecutionId(actor.tenantId(), staleRunning.getExecutionId()))
                .thenReturn(paused);
        when(repository.updateCheckpointIfStatus(
                actor.tenantId(), staleRunning.getExecutionId(), "new-checkpoint", 3))
                .thenAnswer(invocation -> {
                    paused.setLastCheckpointId(invocation.getArgument(2));
                    return 1;
                });

        Execution result = store.recordCheckpoint(actor, staleRunning, "new-checkpoint");

        assertEquals(ExecutionStatus.PAUSED, result.getStatus());
        assertEquals("new-checkpoint", result.getLastCheckpointId());
        assertEquals("new-checkpoint", paused.getLastCheckpointId());
    }

    @Test
    void compareAndSetLossDoesNotInsertWhenThePersistedExecutionDisappears() {
        ActorContext actor = new ActorContext(new TenantId(9), new UserId(7), false);
        AgentExecutionRepository repository = mock(AgentExecutionRepository.class);
        AgentRuntimeStateStore store = new AgentRuntimeStateStore(
                repository, mock(CheckpointManager.class));
        Execution stale = new Execution("agent-1", "v1", Map.of());
        stale.start();
        AgentExecutionBO running = new AgentExecutionBO();
        running.setExecutionId(stale.getExecutionId());
        running.setAgentId("agent-1");
        running.setVersion("v1");
        running.setTenantId(actor.tenantId().value());
        running.setUserId(actor.userId().value());
        running.setInputData("{\"tenantId\":9,\"userId\":7}");
        running.setOutputData("{}");
        running.setStatus(0);
        running.setStartTime(LocalDateTime.now());
        when(repository.selectByExecutionId(actor.tenantId(), stale.getExecutionId()))
                .thenReturn(running)
                .thenReturn(null);
        when(repository.updateIfStatus(any(), any(), any())).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> store.save(actor, stale));
        verify(repository, never()).insert(eq(actor.tenantId()), any(AgentExecutionBO.class));
    }
}
