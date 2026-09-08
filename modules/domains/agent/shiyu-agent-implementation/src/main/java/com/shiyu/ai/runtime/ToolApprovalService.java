package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
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
    public ToolApproval request(String runId, TenantId tenantId, long ownerUserId, String toolName, String argumentsRedacted) { TenantId tenant = requireTenant(tenantId); Instant now = Instant.now(); ToolApproval value = new ToolApproval(UUID.randomUUID().toString(), runId, tenant.value(), ownerUserId, toolName, argumentsRedacted, ToolApprovalStatus.PENDING, now, null, now.plusSeconds(300)); approvals.insert(value); return value; }
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) { TenantId tenant = requireTenant(tenantId); approvals.expirePending(tenant, ownerUserId); return approvals.list(runId, tenant, ownerUserId); }
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) { TenantId tenant = requireTenant(tenantId); approvals.expirePending(tenant, ownerUserId); return approvals.listAll(tenant, ownerUserId); }
    public ToolApproval require(String id, TenantId tenantId, long ownerUserId) { TenantId tenant = requireTenant(tenantId); approvals.expirePending(tenant, ownerUserId); return approvals.find(id, tenant, ownerUserId).orElseThrow(() -> new IllegalArgumentException("approval not found")); }
    public ToolApproval decide(String id, TenantId tenantId, long ownerUserId, ToolApprovalStatus status) {
        TenantId tenant = requireTenant(tenantId);
        if (status != ToolApprovalStatus.APPROVED && status != ToolApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("approval decision must be APPROVED or REJECTED");
        }
        approvals.expirePending(tenant, ownerUserId);
        ToolApproval current = approvals.find(id, tenant, ownerUserId).orElseThrow(() -> new IllegalArgumentException("approval not found"));
        if (current.status() != ToolApprovalStatus.PENDING) return current;
        Instant now = Instant.now();
        ToolApprovalStatus nextStatus = now.isAfter(current.expiresAt()) ? ToolApprovalStatus.EXPIRED : status;
        ToolApproval next = new ToolApproval(current.id(), current.runId(), current.tenantId(), current.ownerUserId(), current.toolName(), current.argumentsRedacted(), nextStatus, current.createdAt(), now, current.expiresAt());
        if (approvals.update(next, ToolApprovalStatus.PENDING) != 1) return approvals.find(id, tenant, ownerUserId).orElseThrow();
        return next;
    }

    private static TenantId requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId;
    }
}
