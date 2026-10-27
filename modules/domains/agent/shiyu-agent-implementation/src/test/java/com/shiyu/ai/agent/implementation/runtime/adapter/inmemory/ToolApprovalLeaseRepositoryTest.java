package com.shiyu.ai.agent.implementation.runtime.adapter.inmemory;

import static org.junit.jupiter.api.Assertions.*;

import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.kernel.context.TenantId;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

/** 验证审批过期补偿的租约、身份边界和幂等完成语义。 */
class ToolApprovalLeaseRepositoryTest {
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void expiryWorkIncludesOnlyAbandonedOrExpiredApprovalsInStableOrder() {
        var repository = new InMemoryToolApprovalRepository();
        repository.insert(approval("pending", 7, 9, ToolApprovalStatus.PENDING, null, NOW.minusSeconds(1)));
        repository.insert(approval("future", 7, 9, ToolApprovalStatus.PENDING, null, NOW.plusSeconds(1)));
        repository.insert(approval("expired", 7, 9, ToolApprovalStatus.EXPIRED, null, NOW.minusSeconds(2)));
        repository.insert(approval("abandoned", 7, 9, ToolApprovalStatus.COMPENSATING, NOW, NOW.minusSeconds(3)));
        repository.insert(approval("leased", 7, 9, ToolApprovalStatus.COMPENSATING, NOW.plusSeconds(1), NOW.minusSeconds(4)));
        repository.insert(approval("no-lease", 7, 9, ToolApprovalStatus.COMPENSATING, null, NOW.minusSeconds(5)));
        repository.insert(approval("approved", 7, 9, ToolApprovalStatus.APPROVED, NOW, NOW.minusSeconds(6)));

        assertEquals(List.of("abandoned", "expired", "pending"), repository.findExpiryCompensationWork(NOW, 100)
                .stream().map(ToolApproval::id).toList());
        assertEquals("abandoned", repository.findExpiryCompensationWork(NOW, 0).getFirst().id());
        assertEquals(1, repository.findExpiryCompensationWork(NOW, 0).size());
        assertThrows(IllegalStateException.class, () -> repository.insert(
                approval("pending", 7, 9, ToolApprovalStatus.PENDING, null, NOW)));
    }

    @Test
    void staleOrForeignClaimCannotStealLeaseAndOnlyCurrentOwnerCanComplete() {
        var repository = new InMemoryToolApprovalRepository();
        ToolApproval expired = approval("approval", 7, 9, ToolApprovalStatus.EXPIRED, NOW.minusSeconds(2), NOW.minusSeconds(3));
        repository.insert(expired);
        Instant lease = NOW.plusSeconds(30);
        assertEquals(0, repository.claimExpiryCompensation(
                approval("missing", 7, 9, ToolApprovalStatus.EXPIRED, null, NOW), NOW, lease));
        for (ToolApproval foreign : List.of(
                approval("approval", 8, 9, expired.status(), expired.decidedAt(), expired.expiresAt()),
                approval("approval", 7, 10, expired.status(), expired.decidedAt(), expired.expiresAt()),
                approval("approval", 7, 9, ToolApprovalStatus.PENDING, expired.decidedAt(), expired.expiresAt()),
                approval("approval", 7, 9, expired.status(), NOW.minusSeconds(1), expired.expiresAt()))) {
            assertEquals(0, repository.claimExpiryCompensation(foreign, NOW, lease));
        }
        assertEquals(1, repository.claimExpiryCompensation(expired, NOW, lease));
        assertEquals(0, repository.claimExpiryCompensation(expired, NOW, lease));
        ToolApproval claimed = repository.find("approval", new TenantId(7), 9).orElseThrow();
        assertEquals(0, repository.claimExpiryCompensation(claimed, NOW, lease));
        assertTrue(repository.findExpiryCompensationWork(NOW, 100).isEmpty());
        assertEquals(0, repository.completeExpiryCompensation("missing", new TenantId(7), 9, lease, NOW));
        assertEquals(0, repository.completeExpiryCompensation("approval", new TenantId(8), 9, lease, NOW));
        assertEquals(0, repository.completeExpiryCompensation("approval", new TenantId(7), 10, lease, NOW));
        assertEquals(0, repository.completeExpiryCompensation("approval", new TenantId(7), 9, NOW, NOW));
        assertEquals(1, repository.completeExpiryCompensation("approval", new TenantId(7), 9, lease, NOW));
        assertEquals(0, repository.completeExpiryCompensation("approval", new TenantId(7), 9, lease, NOW));
        assertEquals(ToolApprovalStatus.COMPENSATED,
                repository.find("approval", new TenantId(7), 9).orElseThrow().status());
    }

    @Test
    void pendingExpirationAndAccessFilterKeepOtherTenantsAndOwnersUnchanged() {
        var repository = new InMemoryToolApprovalRepository();
        Instant past = Instant.now().minusSeconds(60);
        repository.insert(approval("mine", 7, 9, ToolApprovalStatus.PENDING, null, past));
        repository.insert(approval("other-tenant", 8, 9, ToolApprovalStatus.PENDING, null, past));
        repository.insert(approval("other-owner", 7, 10, ToolApprovalStatus.PENDING, null, past));
        repository.insert(approval("future", 7, 9, ToolApprovalStatus.PENDING, null, Instant.now().plusSeconds(3600)));
        repository.insert(approval("approved", 7, 9, ToolApprovalStatus.APPROVED, NOW, past));
        assertEquals(1, repository.expirePending(new TenantId(7), 9));
        assertEquals(0, repository.expirePending(new TenantId(7), 9));
        assertEquals(ToolApprovalStatus.PENDING, repository.find("other-tenant", new TenantId(8), 9).orElseThrow().status());
        assertTrue(repository.find("mine", new TenantId(8), 9).isEmpty());
        assertTrue(repository.find("mine", new TenantId(7), 10).isEmpty());
        assertEquals(3, repository.list("run", new TenantId(7), 9).size());
        assertTrue(repository.list("other-run", new TenantId(7), 9).isEmpty());
        assertEquals(3, repository.listAll(new TenantId(7), 9).size());
        assertThrows(IllegalArgumentException.class, () -> repository.find("mine", null, 9));
    }

    @Test
    void expiredCompensationLeaseCanBeReclaimedWithoutLettingOldWorkerComplete() {
        var repository = new InMemoryToolApprovalRepository();
        var pending = approval("approval", 7, 9, ToolApprovalStatus.PENDING, null, NOW.minusSeconds(1));
        repository.insert(pending);
        Instant firstLease = NOW.plusSeconds(5);
        assertEquals(1, repository.claimExpiryCompensation(pending, NOW, firstLease));
        var abandoned = repository.findExpiryCompensationWork(NOW.plusSeconds(6), 10).getFirst();
        Instant replacementLease = NOW.plusSeconds(40);
        assertEquals(1, repository.claimExpiryCompensation(abandoned, NOW.plusSeconds(6), replacementLease));
        assertEquals(0, repository.completeExpiryCompensation("approval", new TenantId(7), 9, firstLease, NOW.plusSeconds(7)));
        assertEquals(1, repository.completeExpiryCompensation("approval", new TenantId(7), 9, replacementLease, NOW.plusSeconds(8)));
        var missingLease = approval("no-lease", 7, 9, ToolApprovalStatus.COMPENSATING, null, NOW.minusSeconds(10));
        repository.insert(missingLease);
        assertEquals(0, repository.claimExpiryCompensation(missingLease, NOW, replacementLease));
        var future = approval("future", 7, 9, ToolApprovalStatus.PENDING, null, NOW.plusSeconds(60));
        var approved = approval("approved", 7, 9, ToolApprovalStatus.APPROVED, NOW, NOW.minusSeconds(1));
        repository.insert(future);
        repository.insert(approved);
        assertEquals(0, repository.claimExpiryCompensation(future, NOW, replacementLease));
        assertEquals(0, repository.claimExpiryCompensation(approved, NOW, replacementLease));
    }

    /** 构造带固定运行归属的审批，便于区分租户、所有者和租约条件。 */
    private static ToolApproval approval(String id, long tenant, long owner, ToolApprovalStatus status,
            Instant decidedAt, Instant expiresAt) {
        return new ToolApproval(id, "run", tenant, owner, "tool", "{}", status,
                NOW.minusSeconds(100), decidedAt, expiresAt);
    }
}
