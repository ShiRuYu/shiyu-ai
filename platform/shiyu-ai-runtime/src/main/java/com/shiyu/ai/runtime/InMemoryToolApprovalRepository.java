package com.shiyu.ai.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryToolApprovalRepository implements ToolApprovalRepository {
    private final Map<String, ToolApproval> values = new ConcurrentHashMap<>();
    @Override public void insert(ToolApproval approval) { if (values.putIfAbsent(approval.id(), approval) != null) throw new IllegalStateException("approval already exists"); }
    @Override public List<ToolApproval> list(String runId, long tenantId, long ownerUserId) { return values.values().stream().filter(a -> a.runId().equals(runId) && a.tenantId() == tenantId && a.ownerUserId() == ownerUserId).toList(); }
    @Override public Optional<ToolApproval> find(String id, long tenantId, long ownerUserId) { return Optional.ofNullable(values.get(id)).filter(a -> a.tenantId() == tenantId && a.ownerUserId() == ownerUserId); }
    @Override public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
        return values.computeIfPresent(approval.id(), (id, current) -> current.status() == expectedStatus ? approval : current) == approval ? 1 : 0;
    }
    @Override public int expirePending(long tenantId, long ownerUserId) {
        int[] count = {0};
        values.replaceAll((id, current) -> {
            if (current.tenantId() == tenantId && current.ownerUserId() == ownerUserId
                    && current.status() == ToolApprovalStatus.PENDING && Instant.now().isAfter(current.expiresAt())) {
                count[0]++;
                return new ToolApproval(current.id(), current.runId(), current.tenantId(), current.ownerUserId(), current.toolName(), current.argumentsRedacted(), ToolApprovalStatus.EXPIRED, current.createdAt(), Instant.now(), current.expiresAt());
            }
            return current;
        });
        return count[0];
    }
}
