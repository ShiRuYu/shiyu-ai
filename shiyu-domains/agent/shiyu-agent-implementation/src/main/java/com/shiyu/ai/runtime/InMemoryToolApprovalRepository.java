package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryToolApprovalRepository implements ToolApprovalRepository {
    private final Map<String, ToolApproval> values = new ConcurrentHashMap<>();
    @Override public void insert(ToolApproval approval) { if (values.putIfAbsent(approval.id(), approval) != null) throw new IllegalStateException("approval already exists"); }
    @Override public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) { long value = tenant(tenantId); return values.values().stream().filter(a -> a.runId().equals(runId) && a.tenantId() == value && a.ownerUserId() == ownerUserId).toList(); }
    @Override public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) { long value = tenant(tenantId); return values.values().stream().filter(a -> a.tenantId() == value && a.ownerUserId() == ownerUserId).sorted(java.util.Comparator.comparing(ToolApproval::createdAt)).toList(); }
    @Override public Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId) { long value = tenant(tenantId); return Optional.ofNullable(values.get(id)).filter(a -> a.tenantId() == value && a.ownerUserId() == ownerUserId); }
    @Override public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
        ToolApproval[] replaced = {null};
        values.computeIfPresent(approval.id(), (id, current) -> {
            if (current.status() != expectedStatus) return current;
            replaced[0] = approval;
            return approval;
        });
        return replaced[0] == approval ? 1 : 0;
    }
    @Override public int expirePending(TenantId tenantId, long ownerUserId) {
        long value = tenant(tenantId);
        int[] count = {0};
        values.replaceAll((id, current) -> {
            if (current.tenantId() == value && current.ownerUserId() == ownerUserId
                    && current.status() == ToolApprovalStatus.PENDING && Instant.now().isAfter(current.expiresAt())) {
                count[0]++;
                return new ToolApproval(current.id(), current.runId(), current.tenantId(), current.ownerUserId(), current.toolName(), current.argumentsRedacted(), ToolApprovalStatus.EXPIRED, current.createdAt(), Instant.now(), current.expiresAt());
            }
            return current;
        });
        return count[0];
    }
    private static long tenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }
}
