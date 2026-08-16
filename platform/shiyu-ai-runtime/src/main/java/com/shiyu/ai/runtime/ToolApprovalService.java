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
    public ToolApproval request(String runId, long tenantId, long ownerUserId, String toolName, String argumentsRedacted) { ToolApproval value = new ToolApproval(UUID.randomUUID().toString(), runId, tenantId, ownerUserId, toolName, argumentsRedacted, ToolApprovalStatus.PENDING, Instant.now(), null); approvals.insert(value); return value; }
    public List<ToolApproval> list(String runId, long tenantId, long ownerUserId) { return approvals.list(runId, tenantId, ownerUserId); }
    public ToolApproval require(String id, long tenantId, long ownerUserId) { return approvals.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("approval not found")); }
    public ToolApproval decide(String id, long tenantId, long ownerUserId, ToolApprovalStatus status) { ToolApproval current = approvals.find(id, tenantId, ownerUserId).orElseThrow(() -> new IllegalArgumentException("approval not found")); if (current.status() != ToolApprovalStatus.PENDING) return current; ToolApproval next = new ToolApproval(current.id(), current.runId(), current.tenantId(), current.ownerUserId(), current.toolName(), current.argumentsRedacted(), status, current.createdAt(), Instant.now()); if (approvals.update(next, ToolApprovalStatus.PENDING) != 1) return approvals.find(id, tenantId, ownerUserId).orElseThrow(); return next; }
}
