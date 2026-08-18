package com.shiyu.ai.runtime;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ToolApprovalService {
    private final ToolApprovalRepository approvals;
    public ToolApprovalService() { this(new InMemoryToolApprovalRepository()); }
    @Autowired public ToolApprovalService(ToolApprovalRepository approvals) { this.approvals = approvals; }
    public ToolApproval request(String runId, long tenantId, long ownerUserId, String toolName, String argumentsRedacted) { Instant now = Instant.now(); ToolApproval value = new ToolApproval(UUID.randomUUID().toString(), runId, tenantId, ownerUserId, toolName, argumentsRedacted, ToolApprovalStatus.PENDING, now, null, now.plusSeconds(300)); approvals.insert(value); return value; }
    public List<ToolApproval> list(String runId, long tenantId, long ownerUserId) { approvals.expirePending(tenantId, ownerUserId); return approvals.list(runId, tenantId, ownerUserId); }
    public List<ToolApproval> listAll(long tenantId, long ownerUserId) { approvals.expirePending(tenantId, ownerUserId); return approvals.listAll(tenantId, ownerUserId); }
    public ToolApproval require(String id, long tenantId, long ownerUserId) { approvals.expirePending(tenantId, ownerUserId); return approvals.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("approval not found")); }
    public ToolApproval decide(String id, long tenantId, long ownerUserId, ToolApprovalStatus status) {
        approvals.expirePending(tenantId, ownerUserId);
        ToolApproval current = approvals.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("approval not found"));
        if (current.status() != ToolApprovalStatus.PENDING) return current;
        Instant now = Instant.now();
        ToolApprovalStatus nextStatus = now.isAfter(current.expiresAt()) ? ToolApprovalStatus.EXPIRED : status;
        ToolApproval next = new ToolApproval(current.id(), current.runId(), current.tenantId(), current.ownerUserId(), current.toolName(), current.argumentsRedacted(), nextStatus, current.createdAt(), now, current.expiresAt());
        if (approvals.update(next, ToolApprovalStatus.PENDING) != 1) return approvals.find(id, tenantId, ownerUserId).orElseThrow();
        return next;
    }
}
