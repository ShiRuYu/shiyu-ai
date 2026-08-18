package com.shiyu.ai.web.runtime;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.core.domain.UserContextHolder;
import com.shiyu.ai.runtime.*;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class ToolApprovalController {
    private final ToolApprovalService approvals;
    private final AiRuntimeService runtime;
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime) { this.approvals = approvals; this.runtime = runtime; }
    @GetMapping("/runs/{runId}/approvals") public Result<List<ToolApproval>> list(@PathVariable String runId) { runtime.requireRun(runId, tenant(), user()); return Result.success(approvals.list(runId, tenant(), user())); }
    @PostMapping("/runs/{runId}/approvals") public Result<ToolApproval> request(@PathVariable String runId, @Valid @RequestBody Request request) { runtime.requireRun(runId, tenant(), user()); ToolApproval approval = approvals.request(runId, tenant(), user(), request.toolName, request.argumentsRedacted); try { runtime.append(runtime.requireRun(runId, tenant(), user()), AiRunEventType.TOOL_APPROVAL_REQUIRED, "{\"approvalId\":\"" + approval.id() + "\"}", true); } catch (RuntimeException ignored) { } return Result.success(approval); }
    @PostMapping("/approvals/{id}/approve") public Result<ToolApproval> approve(@PathVariable String id) { return Result.success(decide(id, ToolApprovalStatus.APPROVED)); }
    @PostMapping("/approvals/{id}/reject") public Result<ToolApproval> reject(@PathVariable String id) { return Result.success(decide(id, ToolApprovalStatus.REJECTED)); }
    private ToolApproval decide(String id, ToolApprovalStatus status) {
        ToolApproval before = approvals.require(id, tenant(), user());
        ToolApproval value = approvals.decide(id, tenant(), user(), status);
        if (before == null || before.status() == ToolApprovalStatus.PENDING && value.status() != ToolApprovalStatus.PENDING) {
            try { runtime.append(runtime.requireRun(value.runId(), tenant(), user()), AiRunEventType.TOOL_APPROVAL_DECIDED,
                    "{\"approvalId\":\"" + value.id() + "\",\"status\":\"" + value.status().name() + "\"}", true); } catch (RuntimeException ignored) { }
        }
        return value;
    }
    private long tenant(){Long id=UserContextHolder.getCurrentTenantId();if(id==null)throw new IllegalStateException("tenant context is required");return id;}
    private long user(){Long id=UserContextHolder.getUserId();if(id==null)throw new IllegalStateException("login is required");return id;}
    @Data public static class Request { private String toolName; private String argumentsRedacted = "{}"; }
}
