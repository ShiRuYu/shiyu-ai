package com.shiyu.ai.runtime;

import com.shiyu.ai.kernel.context.TenantId;
import java.util.List;
import java.util.Optional;

public interface ToolApprovalRepository {
    void insert(ToolApproval approval);
    List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId);
    List<ToolApproval> listAll(TenantId tenantId, long ownerUserId);
    Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId);
    int update(ToolApproval approval, ToolApprovalStatus expectedStatus);
    int expirePending(TenantId tenantId, long ownerUserId);
}
