package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.kernel.context.UserId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolApprovalServiceTest {
    @Test
    void repositoryRequiresTypedTenantIdentity() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        assertEquals(List.of(), repository.list("run", new TenantId(1), 2));
        assertThrows(IllegalArgumentException.class, () -> repository.list("run", null, 2));
    }

    @Test
    void approvalExpiresAndCannotBeApprovedAfterDeadline() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(10);
        ToolApproval pending = new ToolApproval("approval-1", "run-1", 1, 2, "filesystem.read", "{}",
                ToolApprovalStatus.PENDING, created, null, Instant.now().minusSeconds(1));
        repository.insert(pending);
        ToolApproval result = new ToolApprovalService(repository).decide("approval-1", new TenantId(1), 2, ToolApprovalStatus.APPROVED);
        assertEquals(ToolApprovalStatus.EXPIRED, result.status());
    }

    @Test
    void validatesApprovalIdentityDefaultsAndExpiry() {
        assertThrows(IllegalArgumentException.class,
                () -> new ToolApproval("", "run", 1, 2, "tool", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolApproval("id", "run", 0, 2, "tool", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ToolApproval("id", "run", 1, 2, "tool", null, null,
                        Instant.now(), null, Instant.now().minusSeconds(1)));
        ToolApproval defaults = new ToolApproval("id", "run", 1, 2, "tool", null,
                null, null, null);
        assertEquals("{}", defaults.argumentsRedacted());
        assertEquals(ToolApprovalStatus.PENDING, defaults.status());
        assertTrue(defaults.expiresAt().isAfter(defaults.createdAt()));
    }

    @Test
    void validatesAppAndVersionIdentityAndDefaultStates() {
        assertThrows(IllegalArgumentException.class,
                () -> new AiApp("", new TenantId(1), new UserId(2), "name", null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AiApp("id", new TenantId(0), new UserId(2), "name", null, null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AiApp("id", new TenantId(1), new UserId(2), "", null, null, null, null, null));
        AiApp app = new AiApp("id", new TenantId(1), new UserId(2), "name", null, " ", null, null, null);
        assertEquals("ACTIVE", app.status());
        assertEquals(app.createdAt(), app.updatedAt());

        assertThrows(IllegalArgumentException.class,
                () -> new AiAppVersion("", "app", new TenantId(1), "1", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AiAppVersion("v", "", new TenantId(1), "1", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AiAppVersion("v", "app", new TenantId(0), "1", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new AiAppVersion("v", "app", new TenantId(1), "", null, null, null, null));
        AiAppVersion version = new AiAppVersion("v", "app", new TenantId(1), "1", null, " ", null, null);
        assertEquals("{}", version.configJson());
        assertEquals("DRAFT", version.status());
        assertFalse(version.published());
    }

    @Test
    void enforcesApprovalOwnershipUniquenessAndCompareAndSetUpdates() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant now = Instant.now();
        ToolApproval pending = new ToolApproval("pending", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.PENDING, now, null, now.plusSeconds(60));
        ToolApproval approved = new ToolApproval("approved", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.APPROVED, now.minusSeconds(1), now, now.plusSeconds(60));
        repository.insert(pending);
        repository.insert(approved);
        assertThrows(IllegalStateException.class, () -> repository.insert(pending));
        assertEquals(2, repository.list("run-1", new TenantId(1), 2).size());
        assertEquals(2, repository.listAll(new TenantId(1), 2).size());
        assertTrue(repository.find("pending", new TenantId(99), 2).isEmpty());
        assertEquals(0, repository.update(approved, ToolApprovalStatus.PENDING));

        ToolApproval decided = new ToolApproval("pending", "run-1", 1, 2, "tool", "{}",
                ToolApprovalStatus.APPROVED, pending.createdAt(), now, pending.expiresAt());
        assertEquals(1, repository.update(decided, ToolApprovalStatus.PENDING));
        assertEquals(ToolApprovalStatus.APPROVED, repository.find("pending", new TenantId(1), 2).orElseThrow().status());
        assertEquals(List.of(), repository.list("other-run", new TenantId(1), 2));
    }

    @Test
    void expiresOnlyMatchingPendingApprovals() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant expiredAt = Instant.now().minusSeconds(2);
        repository.insert(new ToolApproval("expired", "run", 1, 2, "tool", "{}",
                ToolApprovalStatus.PENDING, expiredAt.minusSeconds(10), null, expiredAt));
        repository.insert(new ToolApproval("other-owner", "run", 1, 3, "tool", "{}",
                ToolApprovalStatus.PENDING, expiredAt.minusSeconds(10), null, expiredAt));
        repository.insert(new ToolApproval("approved", "run", 1, 2, "tool", "{}",
                ToolApprovalStatus.APPROVED, expiredAt.minusSeconds(10), null, expiredAt));
        assertEquals(1, repository.expirePending(new TenantId(1), 2));
        assertEquals(ToolApprovalStatus.EXPIRED, repository.find("expired", new TenantId(1), 2).orElseThrow().status());
        assertEquals(0, repository.expirePending(new TenantId(1), 2));
    }
}
