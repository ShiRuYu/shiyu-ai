package com.shiyu.ai.runtime;

import java.util.List;
import java.util.Optional;

public interface ToolApprovalRepository {
    void insert(ToolApproval approval);
    List<ToolApproval> list(String runId, long tenantId, long ownerUserId);
    Optional<ToolApproval> find(String id, long tenantId, long ownerUserId);
    int update(ToolApproval approval, ToolApprovalStatus expectedStatus);
}
