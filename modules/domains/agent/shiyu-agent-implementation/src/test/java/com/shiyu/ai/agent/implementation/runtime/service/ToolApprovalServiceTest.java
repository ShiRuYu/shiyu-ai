package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.AiApp;
import com.shiyu.ai.agent.implementation.runtime.model.AiAppVersion;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 验证 工具 Approval Service 相关功能、边界条件、异常路径和协作行为。
 */
class ToolApprovalServiceTest {
    @Test
    void completionRequiresClaimedApproval() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant now = Instant.now();
        ToolApproval pending = new ToolApproval("approval-1", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.PENDING, now, null, now.plusSeconds(60));
        repository.insert(pending);

        assertThrows(IllegalStateException.class, () -> new ToolApprovalService(repository)
                .completeClaimed(pending.id(), new TenantId(1), 2));
        assertEquals(ToolApprovalStatus.PENDING, repository.find(pending.id(), new TenantId(1), 2)
                .orElseThrow().status());
    }

    @Test
    void completionConflictPreservesClaimedApproval() {
        InMemoryToolApprovalRepository repository = new CompletionConflictRepository();
        Instant now = Instant.now();
        ToolApproval claimed = new ToolApproval("approval-1", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.CLAIMED, now, now, now.plusSeconds(60));
        repository.insert(claimed);

        assertThrows(IllegalStateException.class, () -> new ToolApprovalService(repository)
                .completeClaimed(claimed.id(), new TenantId(1), 2));
        assertEquals(ToolApprovalStatus.CLAIMED, repository.find(claimed.id(), new TenantId(1), 2)
                .orElseThrow().status());
    }

    @Test
    void notExecutedResolutionConflictPreservesClaimedApproval() {
        InMemoryToolApprovalRepository repository = new NotExecutedResolutionConflictRepository();
        Instant now = Instant.now();
        ToolApproval claimed = new ToolApproval("approval-1", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.CLAIMED, now, now, now.plusSeconds(60));
        repository.insert(claimed);

        assertThrows(IllegalStateException.class, () -> new ToolApprovalService(repository)
                .markClaimedNotExecuted(claimed.id(), new TenantId(1), 2));
        assertEquals(ToolApprovalStatus.CLAIMED, repository.find(claimed.id(), new TenantId(1), 2)
                .orElseThrow().status());
    }

    @Test
    void manualNotExecutedResolutionMovesOnlyClaimedApprovalToTerminalState() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        ToolApprovalService service = new ToolApprovalService(repository);
        Instant now = Instant.now();
        ToolApproval claimed = new ToolApproval("approval-1", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.CLAIMED, now, now, now.plusSeconds(60));
        repository.insert(claimed);

        ToolApproval resolved = service.markClaimedNotExecuted(
                claimed.id(), new TenantId(1), 2);

        assertEquals(ToolApprovalStatus.NOT_EXECUTED, resolved.status());
        assertEquals(ToolApprovalStatus.NOT_EXECUTED,
                repository.find(claimed.id(), new TenantId(1), 2).orElseThrow().status());
        assertThrows(IllegalStateException.class, () -> service.markClaimedNotExecuted(
                claimed.id(), new TenantId(1), 2));
    }

    /** 模拟完成状态写入时的乐观并发冲突。 */
    private static final class CompletionConflictRepository extends InMemoryToolApprovalRepository {
        @Override
        public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
            if (expectedStatus == ToolApprovalStatus.CLAIMED
                    && approval.status() == ToolApprovalStatus.COMPLETED) return 0;
            return super.update(approval, expectedStatus);
        }
    }

    private static final class NotExecutedResolutionConflictRepository
            extends InMemoryToolApprovalRepository {
        @Override
        public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
            if (expectedStatus == ToolApprovalStatus.CLAIMED
                    && approval.status() == ToolApprovalStatus.NOT_EXECUTED) return 0;
            return super.update(approval, expectedStatus);
        }
    }

    /** 模拟定时过期扫描尚未处理到期记录。 */
    private static final class ExpirySweepMissRepository extends InMemoryToolApprovalRepository {
        @Override
        public int expirePending(TenantId tenantId, long ownerUserId) {
            return 0;
        }
    }

    /** 模拟另一个请求先完成审批决策，当前请求随后遇到 CAS 冲突。 */
    private static final class DecisionConflictRepository extends InMemoryToolApprovalRepository {
        @Override
        public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
            if (expectedStatus == ToolApprovalStatus.PENDING
                    && approval.status() == ToolApprovalStatus.APPROVED) {
                ToolApproval current = find(
                        approval.id(), new TenantId(approval.tenantId()), approval.ownerUserId())
                        .orElseThrow();
                ToolApproval rejected = new ToolApproval(
                        current.id(), current.runId(), current.tenantId(), current.ownerUserId(),
                        current.toolName(), current.argumentsRedacted(), ToolApprovalStatus.REJECTED,
                        current.createdAt(), Instant.now(), current.expiresAt());
                super.update(rejected, expectedStatus);
                return 0;
            }
            return super.update(approval, expectedStatus);
        }
    }

    /** 模拟另一个执行请求先占用已批准的工具调用。 */
    private static final class ClaimConflictRepository extends InMemoryToolApprovalRepository {
        @Override
        public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
            if (expectedStatus == ToolApprovalStatus.APPROVED
                    && approval.status() == ToolApprovalStatus.CLAIMED) {
                super.update(approval, expectedStatus);
                return 0;
            }
            return super.update(approval, expectedStatus);
        }
    }

    @Test
    void concurrentClaimsAllowOnlyOneExecution() throws Exception {
        ToolApprovalService service = new ToolApprovalService(new InMemoryToolApprovalRepository());
        ToolApproval pending = service.request("run-1", new TenantId(1), 2, "tool", "hash");
        service.decide(pending.id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        CountDownLatch start = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> claim = () -> {
                start.await();
                try {
                    service.claimForExecution(pending.id(), new TenantId(1), 2,
                            "run-1", "tool", "hash");
                    return true;
                } catch (IllegalStateException expected) {
                    return false;
                }
            };
            Future<Boolean> first = pool.submit(claim);
            Future<Boolean> second = pool.submit(claim);
            start.countDown();
            assertEquals(1, (first.get() ? 1 : 0) + (second.get() ? 1 : 0));
        }
        assertEquals(ToolApprovalStatus.CLAIMED,
                service.require(pending.id(), new TenantId(1), 2).status());
    }

    @Test
    void claimReportsConcurrentWinnerAfterApprovalIsRead() {
        InMemoryToolApprovalRepository repository = new ClaimConflictRepository();
        ToolApprovalService service = new ToolApprovalService(repository);
        ToolApproval pending = service.request("run-1", new TenantId(1), 2, "tool", "hash");
        service.decide(pending.id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);

        assertThrows(IllegalStateException.class, () -> service.claimForExecution(
                pending.id(), new TenantId(1), 2, "run-1", "tool", "hash"));
        assertEquals(ToolApprovalStatus.CLAIMED,
                repository.find(pending.id(), new TenantId(1), 2).orElseThrow().status());
    }

    @Test
    void repositoryRequiresTypedTenantIdentity() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        assertEquals(List.of(), repository.list("run", new TenantId(1), 2));
        assertThrows(IllegalArgumentException.class, () -> repository.list("run", null, 2));
    }

    @Test
    void serviceRejectsMissingTenantIdentityBeforeRepositoryAccess() {
        ToolApprovalService service = new ToolApprovalService(new InMemoryToolApprovalRepository());
        assertThrows(
                IllegalArgumentException.class,
                () -> service.request("run", null, 2, "tool", "{}"));
        assertThrows(IllegalArgumentException.class, () -> service.list("run", null, 2));
        assertThrows(IllegalArgumentException.class, () -> service.listAll(null, 2));
        assertThrows(IllegalArgumentException.class, () -> service.require("approval", null, 2));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.decide("approval", null, 2, ToolApprovalStatus.APPROVED));
    }

    @Test
    void approvalExpiresAndCannotBeApprovedAfterDeadline() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(10);
        ToolApproval pending =
                new ToolApproval(
                        "approval-1",
                        "run-1",
                        1,
                        2,
                        "filesystem.read",
                        "{}",
                        ToolApprovalStatus.PENDING,
                        created,
                        null,
                        Instant.now().minusSeconds(1));
        repository.insert(pending);
        ToolApproval result =
                new ToolApprovalService(repository)
                        .decide("approval-1", new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        assertEquals(ToolApprovalStatus.EXPIRED, result.status());
    }

    @Test
    void decisionMarksExpiredWhenExpirySweepMissesPendingApproval() {
        InMemoryToolApprovalRepository repository = new ExpirySweepMissRepository();
        Instant created = Instant.now().minusSeconds(10);
        ToolApproval pending = new ToolApproval(
                "approval-expired", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.PENDING, created, null, Instant.now().minusSeconds(1));
        repository.insert(pending);

        ToolApproval result = new ToolApprovalService(repository).decide(
                pending.id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);

        assertEquals(ToolApprovalStatus.EXPIRED, result.status());
        assertEquals(ToolApprovalStatus.EXPIRED,
                repository.find(pending.id(), new TenantId(1), 2).orElseThrow().status());
    }

    @Test
    void decisionReturnsTheWinningStateAfterCompareAndSetConflict() {
        InMemoryToolApprovalRepository repository = new DecisionConflictRepository();
        Instant now = Instant.now();
        ToolApproval pending = new ToolApproval(
                "approval-conflict", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.PENDING, now, null, now.plusSeconds(60));
        repository.insert(pending);

        ToolApproval result = new ToolApprovalService(repository).decide(
                pending.id(), new TenantId(1), 2, ToolApprovalStatus.APPROVED);

        assertEquals(ToolApprovalStatus.REJECTED, result.status());
        assertEquals(ToolApprovalStatus.REJECTED,
                repository.find(pending.id(), new TenantId(1), 2).orElseThrow().status());
    }

    @Test
    void decisionOnlyAcceptsApprovedOrRejectedStatuses() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant now = Instant.now();
        repository.insert(
                new ToolApproval(
                        "approval-1",
                        "run-1",
                        1,
                        2,
                        "tool",
                        "{}",
                        ToolApprovalStatus.PENDING,
                        now,
                        null,
                        now.plusSeconds(60)));
        ToolApprovalService service = new ToolApprovalService(repository);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.decide("approval-1", new TenantId(1), 2, ToolApprovalStatus.PENDING));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.decide("approval-1", new TenantId(1), 2, ToolApprovalStatus.EXPIRED));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.decide("approval-1", new TenantId(1), 2, null));
        assertEquals(
                ToolApprovalStatus.PENDING,
                repository.find("approval-1", new TenantId(1), 2).orElseThrow().status());
    }

    @Test
    void validatesApprovalIdentityDefaultsAndExpiry() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ToolApproval("", "run", 1, 2, "tool", null, null, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ToolApproval("id", "run", 0, 2, "tool", null, null, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ToolApproval(
                                "id",
                                "run",
                                1,
                                2,
                                "tool",
                                null,
                                null,
                                Instant.now(),
                                null,
                                Instant.now().minusSeconds(1)));
        ToolApproval defaults = new ToolApproval("id", "run", 1, 2, "tool", null, null, null, null);
        assertEquals("{}", defaults.argumentsRedacted());
        assertEquals(ToolApprovalStatus.PENDING, defaults.status());
        assertTrue(defaults.expiresAt().isAfter(defaults.createdAt()));
    }

    @Test
    void validatesAppAndVersionIdentityAndDefaultStates() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new AiApp(
                                "",
                                new TenantId(1),
                                new UserId(2),
                                "name",
                                null,
                                null,
                                null,
                                null,
                                null));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new AiApp(
                                "id",
                                new TenantId(0),
                                new UserId(2),
                                "name",
                                null,
                                null,
                                null,
                                null,
                                null));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new AiApp(
                                "id",
                                new TenantId(1),
                                new UserId(2),
                                "",
                                null,
                                null,
                                null,
                                null,
                                null));
        AiApp app =
                new AiApp(
                        "id", new TenantId(1), new UserId(2), "name", null, " ", null, null, null);
        assertEquals("ACTIVE", app.status());
        assertEquals(app.createdAt(), app.updatedAt());

        assertThrows(
                IllegalArgumentException.class,
                () -> new AiAppVersion("", "app", new TenantId(1), "1", null, null, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AiAppVersion("v", "", new TenantId(1), "1", null, null, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AiAppVersion("v", "app", new TenantId(0), "1", null, null, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new AiAppVersion("v", "app", new TenantId(1), "", null, null, null, null));
        AiAppVersion version =
                new AiAppVersion("v", "app", new TenantId(1), "1", null, " ", null, null);
        assertEquals("{}", version.configJson());
        assertEquals("DRAFT", version.status());
        assertFalse(version.published());
    }

    @Test
    void enforcesApprovalOwnershipUniquenessAndCompareAndSetUpdates() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant now = Instant.now();
        ToolApproval pending =
                new ToolApproval(
                        "pending",
                        "run-1",
                        1,
                        2,
                        "tool",
                        "{}",
                        ToolApprovalStatus.PENDING,
                        now,
                        null,
                        now.plusSeconds(60));
        ToolApproval approved =
                new ToolApproval(
                        "approved",
                        "run-1",
                        1,
                        2,
                        "tool",
                        "{}",
                        ToolApprovalStatus.APPROVED,
                        now.minusSeconds(1),
                        now,
                        now.plusSeconds(60));
        repository.insert(pending);
        repository.insert(approved);
        assertThrows(IllegalStateException.class, () -> repository.insert(pending));
        assertEquals(2, repository.list("run-1", new TenantId(1), 2).size());
        assertEquals(2, repository.listAll(new TenantId(1), 2).size());
        assertTrue(repository.find("pending", new TenantId(99), 2).isEmpty());
        assertEquals(0, repository.update(approved, ToolApprovalStatus.PENDING));

        ToolApproval decided =
                new ToolApproval(
                        "pending",
                        "run-1",
                        1,
                        2,
                        "tool",
                        "{}",
                        ToolApprovalStatus.APPROVED,
                        pending.createdAt(),
                        now,
                        pending.expiresAt());
        assertEquals(1, repository.update(decided, ToolApprovalStatus.PENDING));
        assertEquals(
                ToolApprovalStatus.APPROVED,
                repository.find("pending", new TenantId(1), 2).orElseThrow().status());
        assertEquals(List.of(), repository.list("other-run", new TenantId(1), 2));
    }

    @Test
    void expiresOnlyMatchingPendingApprovals() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant expiredAt = Instant.now().minusSeconds(2);
        repository.insert(
                new ToolApproval(
                        "expired",
                        "run",
                        1,
                        2,
                        "tool",
                        "{}",
                        ToolApprovalStatus.PENDING,
                        expiredAt.minusSeconds(10),
                        null,
                        expiredAt));
        repository.insert(
                new ToolApproval(
                        "other-owner",
                        "run",
                        1,
                        3,
                        "tool",
                        "{}",
                        ToolApprovalStatus.PENDING,
                        expiredAt.minusSeconds(10),
                        null,
                        expiredAt));
        repository.insert(
                new ToolApproval(
                        "approved",
                        "run",
                        1,
                        2,
                        "tool",
                        "{}",
                        ToolApprovalStatus.APPROVED,
                        expiredAt.minusSeconds(10),
                        null,
                        expiredAt));
        assertEquals(1, repository.expirePending(new TenantId(1), 2));
        assertEquals(
                ToolApprovalStatus.EXPIRED,
                repository.find("expired", new TenantId(1), 2).orElseThrow().status());
        assertEquals(0, repository.expirePending(new TenantId(1), 2));
    }
}
