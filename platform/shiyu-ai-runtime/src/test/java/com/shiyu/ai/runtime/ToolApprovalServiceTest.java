package com.shiyu.ai.runtime;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ToolApprovalServiceTest {
    @Test
    void approvalExpiresAndCannotBeApprovedAfterDeadline() {
        InMemoryToolApprovalRepository repository = new InMemoryToolApprovalRepository();
        Instant created = Instant.now().minusSeconds(10);
        ToolApproval pending = new ToolApproval("approval-1", "run-1", 1, 2, "filesystem.read", "{}",
                ToolApprovalStatus.PENDING, created, null, Instant.now().minusSeconds(1));
        repository.insert(pending);
        ToolApproval result = new ToolApprovalService(repository).decide("approval-1", 1, 2, ToolApprovalStatus.APPROVED);
        assertEquals(ToolApprovalStatus.EXPIRED, result.status());
    }
}
