package com.shiyu.ai.agent.implementation.runtime.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.shiyu.ai.agent.AgentDefinition;
import com.shiyu.ai.agent.AgentVersion;
import com.shiyu.ai.agent.contract.runtime.AiRun;
import com.shiyu.ai.agent.contract.runtime.AiRunStatus;
import com.shiyu.ai.agent.implementation.cache.AgentCacheManager;
import com.shiyu.ai.agent.implementation.cache.AgentLoader;
import com.shiyu.ai.agent.implementation.checkpoint.Checkpoint;
import com.shiyu.ai.agent.implementation.checkpoint.CheckpointManager;
import com.shiyu.ai.agent.implementation.domain.model.AgentExecutionBO;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionCompletedEvent;
import com.shiyu.ai.agent.implementation.event.model.AgentExecutionFailedEvent;
import com.shiyu.ai.agent.implementation.event.publisher.EventPublisher;
import com.shiyu.ai.agent.implementation.execution.Execution;
import com.shiyu.ai.agent.implementation.execution.ExecutionStatus;
import com.shiyu.ai.agent.implementation.graph.Graph;
import com.shiyu.ai.agent.implementation.port.repository.AgentExecutionRepository;
import com.shiyu.ai.agent.implementation.runtime.port.AgentRuntimeStateStore;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.ActorContext;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;

/** 验证执行终态的持久化重试、并发胜出结果和不安全检查点的恢复边界。 */
class AgentExecutionLifecycleRecoveryTest {
    private static final ActorContext ACTOR = new ActorContext(new TenantId(7), new UserId(9), false);

    @Test
    void retriesCompletedPersistenceWithoutReexecutingProviderOrLosingOutput() {
        Fixture f = new Fixture();
        AtomicBoolean firstCompletedWrite = new AtomicBoolean(true);
        when(f.store.save(eq(ACTOR), any())).thenAnswer(call -> {
            Execution current = call.getArgument(1);
            if (current.getStatus() == ExecutionStatus.COMPLETED && firstCompletedWrite.getAndSet(false))
                throw new IllegalStateException("transient write failure");
            return current;
        });

        Execution result = f.lifecycle.execute(ACTOR, "agent", null, Map.of());

        assertEquals(ExecutionStatus.COMPLETED, result.getStatus());
        assertEquals(Map.of("answer", "saved"), result.getOutput());
        verify(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());
        verify(f.publisher).publish(any(AgentExecutionCompletedEvent.class));
        verify(f.store).cleanup(ACTOR, result);
    }

    @Test
    void preservesBothPersistenceFailuresAndCleansUpWithoutPublishingCompletion() {
        Fixture f = new Fixture();
        var first = new IllegalStateException("first write");
        var retry = new IllegalStateException("retry write");
        AtomicBoolean firstCompletedWrite = new AtomicBoolean(true);
        when(f.store.save(eq(ACTOR), any())).thenAnswer(call -> {
            Execution current = call.getArgument(1);
            if (current.getStatus() == ExecutionStatus.COMPLETED)
                throw firstCompletedWrite.getAndSet(false) ? first : retry;
            return current;
        });

        assertSame(first, assertThrows(IllegalStateException.class,
                () -> f.lifecycle.execute(ACTOR, "agent", null, Map.of())));
        assertArrayEquals(new Throwable[] {retry}, first.getSuppressed());
        verify(f.store).cleanup(eq(ACTOR), any());
        verify(f.publisher, never()).publish(any(AgentExecutionCompletedEvent.class));
    }

    @Test
    void honorsCancellationOrCompletionThatWinsWhileExecutorThrows() {
        for (ExecutionStatus winner : List.of(ExecutionStatus.CANCELLED, ExecutionStatus.COMPLETED)) {
            Fixture f = new Fixture();
            doThrow(new IllegalStateException("late executor failure"))
                    .when(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());
            when(f.store.effective(eq(ACTOR), any())).thenAnswer(call -> {
                Execution current = call.getArgument(1);
                if (winner == ExecutionStatus.CANCELLED) current.cancel();
                else current.complete(Map.of("winner", true));
                return current;
            });

            Execution result = f.lifecycle.execute(ACTOR, "agent", null, Map.of());

            assertEquals(winner, result.getStatus());
            verify(f.publisher, never()).publish(any(AgentExecutionFailedEvent.class));
            verify(f.store).cleanup(ACTOR, result);
        }
    }

    @Test
    void retainsExecutorErrorWhenEveryFailureCleanupAdapterAlsoFails() {
        Fixture f = new Fixture();
        var provider = new IllegalStateException("provider failure");
        var persistence = new IllegalStateException("persistence failure");
        var runtime = new IllegalStateException("runtime finish failure");
        var publishing = new IllegalStateException("failed event failure");
        var cleanup = new IllegalStateException("cleanup failure");
        doThrow(provider).when(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());
        when(f.store.save(eq(ACTOR), any())).thenAnswer(call -> {
            Execution current = call.getArgument(1);
            if (current.getStatus() == ExecutionStatus.FAILED) throw persistence;
            return current;
        });
        when(f.bridge.finish(any(), any())).thenThrow(runtime);
        doThrow(publishing).when(f.publisher).publish(any(AgentExecutionFailedEvent.class));
        doThrow(cleanup).when(f.store).cleanup(eq(ACTOR), any());

        assertSame(provider, assertThrows(IllegalStateException.class,
                () -> f.lifecycle.execute(ACTOR, "agent", null, Map.of())));
        assertArrayEquals(new Throwable[] {persistence, runtime, publishing, cleanup}, provider.getSuppressed());
    }

    @Test
    void reconcilesRuntimeCancellationWithPersistedWinnerAndRejectsInconsistentTerminals() {
        for (boolean consistent : List.of(true, false)) {
            Fixture f = new Fixture();
            AiRun cancelled = mock(AiRun.class);
            when(cancelled.status()).thenReturn(AiRunStatus.CANCELLED);
            when(f.bridge.finish(any(), any())).thenReturn(cancelled);
            AgentExecutionBO persisted = new AgentExecutionBO();
            when(f.store.persisted(eq(ACTOR), anyString())).thenReturn(persisted);
            when(f.store.rebuild(persisted)).thenAnswer(call -> Execution.restore(
                    f.lastExecution.getExecutionId(), "agent", "v1",
                    consistent ? ExecutionStatus.CANCELLED : ExecutionStatus.FAILED,
                    Map.of(), Map.of(), null, 9L, null, null, null, 0L));
            if (consistent) {
                assertEquals(ExecutionStatus.CANCELLED,
                        f.lifecycle.execute(ACTOR, "agent", null, Map.of()).getStatus());
            } else {
                assertThrows(IllegalStateException.class,
                        () -> f.lifecycle.execute(ACTOR, "agent", null, Map.of()));
            }
            verify(f.publisher, never()).publish(any(AgentExecutionCompletedEvent.class));
            verify(f.store).cleanup(eq(ACTOR), any());
        }
    }

    @Test
    void refusesCheckpointReplayWithWrongIdentityIncompleteWritesOrMissingContinuation() {
        for (String unsafe : List.of("missing", "tenant", "execution", "started", "unrecoverable", "stale", "legacy")) {
            Fixture f = new Fixture();
            Execution paused = new Execution("agent", "v1", Map.of());
            paused.start();
            paused.pause();
            AgentExecutionBO record = new AgentExecutionBO();
            when(f.store.persisted(ACTOR, paused.getExecutionId())).thenReturn(record);
            when(f.store.rebuild(record)).thenReturn(paused);
            String node = switch (unsafe) {
                case "started" -> "__agent_node_started__";
                case "unrecoverable" -> "__agent_nonresumable__";
                default -> "node";
            };
            if (unsafe.equals("stale")) paused.setLastCheckpointId("older");
            Checkpoint checkpoint = unsafe.equals("missing") ? null : new Checkpoint(
                    unsafe.equals("tenant") ? new TenantId(8) : ACTOR.tenantId(),
                    unsafe.equals("execution") ? "other" : paused.getExecutionId(),
                    "checkpoint", node, unsafe.equals("legacy") ? null : "next", Map.of());
            when(f.checkpoints.loadLatestCheckpoint(ACTOR.tenantId(), paused.getExecutionId()))
                    .thenReturn(checkpoint);

            assertThrows(IllegalStateException.class,
                    () -> f.lifecycle.resume(ACTOR, paused.getExecutionId()), unsafe);
            verify(f.executor, never()).resumeFromCheckpoint(any(), any(), any(), any(), any(), any(), any());
            verify(f.store, never()).claimPausedForResume(any(), any());
        }
    }

    @Test
    void checkpointResumePublishesOnlyItsWinningTerminalOutcome() {
        for (ExecutionStatus terminal : List.of(ExecutionStatus.COMPLETED, ExecutionStatus.FAILED,
                ExecutionStatus.CANCELLED, ExecutionStatus.PAUSED)) {
            Fixture f = new Fixture();
            Execution paused = f.persistPaused();
            when(f.checkpoints.loadLatestCheckpoint(ACTOR.tenantId(), paused.getExecutionId()))
                    .thenReturn(new Checkpoint(ACTOR.tenantId(), paused.getExecutionId(), "final", "graph_final", null, Map.of()));
            when(f.store.claimPausedForResume(ACTOR, paused)).thenReturn(true);
            when(f.executor.resumeFromCheckpoint(any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(call -> {
                        Execution current = call.getArgument(1);
                        switch (terminal) {
                            case COMPLETED -> current.complete(Map.of("restored", true));
                            case FAILED -> current.fail("restored failure");
                            case CANCELLED -> current.cancel();
                            default -> current.pause();
                        }
                        return current;
                    });

            assertEquals(terminal, f.lifecycle.resume(ACTOR, paused.getExecutionId()).getStatus());

            if (terminal == ExecutionStatus.COMPLETED) verify(f.publisher).publish(any(AgentExecutionCompletedEvent.class));
            else verify(f.publisher, never()).publish(any(AgentExecutionCompletedEvent.class));
            if (terminal == ExecutionStatus.FAILED) verify(f.publisher).publish(any(AgentExecutionFailedEvent.class));
            else verify(f.publisher, never()).publish(any(AgentExecutionFailedEvent.class));
            verify(f.store).cleanup(ACTOR, paused);
        }
    }

    @Test
    void pausedExecutionResumeRequiresExclusiveClaimAndCannotResumeTwice() {
        for (boolean claimed : List.of(true, false)) {
            Fixture f = new Fixture();
            Execution paused = new Execution("agent", "v1", Map.of());
            paused.start();
            paused.pause();
            when(f.store.active(paused.getExecutionId())).thenReturn(paused);
            when(f.store.claimPausedForResume(ACTOR, paused)).thenReturn(claimed);
            if (claimed) {
                assertSame(paused, f.lifecycle.resume(ACTOR, paused.getExecutionId()));
                assertEquals(ExecutionStatus.RUNNING, paused.getStatus());
                assertThrows(IllegalStateException.class, () -> f.lifecycle.resume(ACTOR, paused.getExecutionId()));
            } else {
                assertThrows(IllegalStateException.class, () -> f.lifecycle.resume(ACTOR, paused.getExecutionId()));
                assertEquals(ExecutionStatus.PAUSED, paused.getStatus());
            }
            verifyNoInteractions(f.executor);
        }
    }

    @Test
    void pendingApprovalCannotResumeUntilApprovedForTheSameRuntime() {
        for (String reason : List.of("missing-runtime", "rejected", "wrong-run", "claim-lost")) {
            Fixture f = new Fixture();
            Execution paused = f.persistPaused();
            when(f.checkpoints.loadLatestCheckpoint(ACTOR.tenantId(), paused.getExecutionId()))
                    .thenReturn(new Checkpoint(ACTOR.tenantId(), paused.getExecutionId(), "approval", "__agent_approval_pending__",
                            "tool", Map.of("agentApprovalId", "approval-1")));
            if (!reason.equals("missing-runtime")) {
                AiRun runtime = mock(AiRun.class);
                when(runtime.id()).thenReturn("run");
                when(f.bridge.executionRun(ACTOR, paused.getExecutionId())).thenReturn(runtime);
                when(f.approvals.require("approval-1", ACTOR.tenantId(), 9)).thenReturn(new ToolApproval(
                        "approval-1", reason.equals("wrong-run") ? "other" : "run", 7, 9, "tool", "{}",
                        reason.equals("rejected") ? ToolApprovalStatus.REJECTED : ToolApprovalStatus.APPROVED,
                        Instant.now(), Instant.now(), Instant.now().plusSeconds(60)));
            }
            assertThrows(IllegalStateException.class, () -> f.lifecycle.resume(ACTOR, paused.getExecutionId()), reason);
            assertEquals(ExecutionStatus.PAUSED, paused.getStatus());
            verifyNoInteractions(f.executor);
        }
    }

    @Test
    void malformedStreamingGraphPreservesAConcurrentlyWonTerminalOrPausedState() {
        for (ExecutionStatus won : List.of(ExecutionStatus.COMPLETED, ExecutionStatus.CANCELLED, ExecutionStatus.PAUSED)) {
            Fixture f = new Fixture();
            AtomicInteger refreshes = new AtomicInteger();
            when(f.store.effective(eq(ACTOR), any())).thenAnswer(call -> {
                Execution current = call.getArgument(1);
                if (refreshes.incrementAndGet() == 1) return current;
                if (won == ExecutionStatus.COMPLETED) current.complete(Map.of("winner", true));
                else if (won == ExecutionStatus.CANCELLED) current.cancel();
                else if (current.getStatus() == ExecutionStatus.RUNNING) current.pause();
                return current;
            });

            var events = f.lifecycle.executeStream(ACTOR, "agent", null, Map.of()).collectList().block();

            assertEquals(won.name(), events.getLast().get("status"));
            verify(f.publisher, never()).publish(any(AgentExecutionFailedEvent.class));
            verify(f.store).cleanup(eq(ACTOR), any());
        }
    }

    @Test
    void malformedStreamingGraphReportsFailureAndCleansUpWithoutCallingSyncExecutor() {
        Fixture f = new Fixture();
        assertThrows(RuntimeException.class,
                () -> f.lifecycle.executeStream(ACTOR, "agent", null, Map.of()).collectList().block());
        verify(f.publisher).publish(any(AgentExecutionFailedEvent.class));
        verify(f.store).cleanup(eq(ACTOR), any());
        verifyNoInteractions(f.executor);
    }

    @Test
    void resumedCompletionReconcilesAConcurrentRuntimeTerminalAndRejectsDisagreement() {
        for (boolean consistent : List.of(true, false)) {
            Fixture f = new Fixture();
            Execution paused = f.persistPaused();
            when(f.checkpoints.loadLatestCheckpoint(ACTOR.tenantId(), paused.getExecutionId()))
                    .thenReturn(new Checkpoint(ACTOR.tenantId(), paused.getExecutionId(), "final", "graph_final", null, Map.of()));
            when(f.store.claimPausedForResume(ACTOR, paused)).thenReturn(true);
            when(f.executor.resumeFromCheckpoint(any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(call -> { paused.complete(Map.of("local", true)); return paused; });
            AiRun cancelled = mock(AiRun.class);
            when(cancelled.status()).thenReturn(AiRunStatus.CANCELLED);
            when(f.bridge.finish(any(), eq(paused))).thenAnswer(call -> {
                var record = f.store.persisted(ACTOR, paused.getExecutionId());
                when(f.store.rebuild(record)).thenReturn(Execution.restore(
                        paused.getExecutionId(), "agent", "v1",
                        consistent ? ExecutionStatus.CANCELLED : ExecutionStatus.FAILED,
                        Map.of(), null, null, 9L, null, null, null, 0L));
                return cancelled;
            });
            if (consistent) assertEquals(ExecutionStatus.CANCELLED,
                    f.lifecycle.resume(ACTOR, paused.getExecutionId()).getStatus());
            else assertThrows(IllegalStateException.class, () -> f.lifecycle.resume(ACTOR, paused.getExecutionId()));
            verify(f.publisher, never()).publish(any(AgentExecutionCompletedEvent.class));
        }
    }

    @Test
    void successfulExecutorMayPauseOrFailWithoutPublishingFalseCompletion() {
        for (ExecutionStatus outcome : List.of(ExecutionStatus.PAUSED, ExecutionStatus.FAILED)) {
            Fixture f = new Fixture();
            doAnswer(call -> {
                Execution current = call.getArgument(4);
                if (outcome == ExecutionStatus.PAUSED) current.pause();
                else current.fail("node returned a failure result");
                return current;
            }).when(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());
            assertEquals(outcome, f.lifecycle.execute(ACTOR, "agent", null, Map.of()).getStatus());
            verify(f.publisher, never()).publish(any(AgentExecutionCompletedEvent.class));
            if (outcome == ExecutionStatus.PAUSED) verify(f.bridge, never()).finish(any(), any());
            else verify(f.publisher).publish(any(AgentExecutionFailedEvent.class));
        }
    }

    @Test
    void cancellationAtCheckpointPreventsExecutorFromContinuingSideEffects() {
        Fixture f = new Fixture();
        AtomicBoolean continued = new AtomicBoolean();
        when(f.store.recordCheckpoint(eq(ACTOR), any(), eq("checkpoint"))).thenAnswer(call -> {
            Execution current = call.getArgument(1);
            current.cancel();
            return current;
        });
        doAnswer(call -> {
            java.util.function.Consumer<String> recorder = call.getArgument(5);
            recorder.accept("checkpoint");
            continued.set(true);
            return call.getArgument(4);
        }).when(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());

        assertEquals(ExecutionStatus.CANCELLED, f.lifecycle.execute(ACTOR, "agent", null, Map.of()).getStatus());
        assertFalse(continued.get());
        verify(f.publisher, never()).publish(any(AgentExecutionFailedEvent.class));
    }

    @Test
    void runtimeCancellationWinningFailureRacePreventsContradictoryFailedEvent() {
        Fixture f = new Fixture();
        var failure = new IllegalStateException("late node failure");
        doThrow(failure).when(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());
        AiRun cancelled = mock(AiRun.class);
        when(cancelled.status()).thenReturn(AiRunStatus.CANCELLED);
        when(f.bridge.finish(any(), any())).thenReturn(cancelled);

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> f.lifecycle.execute(ACTOR, "agent", null, Map.of())));
        verify(f.publisher, never()).publish(any(AgentExecutionFailedEvent.class));
        verify(f.store).cleanup(eq(ACTOR), any());
    }

    @Test
    void alreadyPersistedFailureDoesNotPublishTheFailureEventTwice() {
        Fixture f = new Fixture();
        var failure = new IllegalStateException("late node failure");
        doThrow(failure).when(f.executor).executeAgent(any(), any(), any(), anyMap(), any(), any());
        when(f.store.effective(eq(ACTOR), any())).thenAnswer(call -> {
            Execution current = call.getArgument(1);
            current.fail("already failed by other owner");
            return current;
        });

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> f.lifecycle.execute(ACTOR, "agent", null, Map.of())));
        verify(f.publisher, never()).publish(any(AgentExecutionFailedEvent.class));
    }

    @Test
    void cancellingPersistedPausedExecutionStillCleansCheckpointsWhenWriteFails() {
        Fixture f = new Fixture();
        Execution paused = f.persistPaused();
        var failure = new IllegalStateException("cancel state unavailable");
        doThrow(failure).when(f.store).save(ACTOR, paused);

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> f.lifecycle.cancel(ACTOR, paused.getExecutionId())));
        assertEquals(ExecutionStatus.CANCELLED, paused.getStatus());
        verify(f.store).cleanup(ACTOR, paused);
        verify(f.bridge, never()).finish(any(), any());
    }

    /** 使用真实执行状态机并将存储和运行适配器作为可控故障边界。 */
    private static final class Fixture {
        private final AgentCacheManager cache = mock(AgentCacheManager.class);
        private final AgentExecutor executor = mock(AgentExecutor.class);
        private final CheckpointManager checkpoints = mock(CheckpointManager.class);
        private final EventPublisher publisher = mock(EventPublisher.class);
        private final AgentRuntimeStateStore store = mock(AgentRuntimeStateStore.class);
        private final AgentRuntimeEventBridge bridge = mock(AgentRuntimeEventBridge.class);
        private final ToolApprovalService approvals = mock(ToolApprovalService.class);
        private final AgentExecutionLifecycle lifecycle;
        private Execution lastExecution;

        private Fixture() {
            AgentDefinition definition = AgentDefinition.builder().agentId("agent").currentVersion("v1").build();
            definition.addVersion(AgentVersion.builder().versionNumber("v1").graph(mock(Graph.class)).build());
            when(cache.getOrLoad(eq(ACTOR), eq("agent"), any())).thenReturn(definition);
            when(store.save(eq(ACTOR), any())).thenAnswer(call -> call.getArgument(1));
            when(store.effective(eq(ACTOR), any())).thenAnswer(call -> call.getArgument(1));
            when(executor.executeAgent(any(), any(), any(), anyMap(), any(), any())).thenAnswer(call -> {
                Execution current = call.getArgument(4);
                lastExecution = current;
                current.complete(Map.of("answer", "saved"));
                return current;
            });
            lifecycle = new AgentExecutionLifecycle(cache, mock(AgentLoader.class),
                    mock(AgentExecutionRepository.class), executor, checkpoints, publisher, store, bridge, approvals);
        }

        /** 保存一个等待恢复的执行快照，让生命周期负责检查点和所有权校验。 */
        private Execution persistPaused() {
            Execution paused = new Execution("agent", "v1", Map.of());
            paused.start();
            paused.pause();
            AgentExecutionBO record = new AgentExecutionBO();
            when(store.persisted(ACTOR, paused.getExecutionId())).thenReturn(record);
            when(store.rebuild(record)).thenReturn(paused);
            return paused;
        }
    }
}
