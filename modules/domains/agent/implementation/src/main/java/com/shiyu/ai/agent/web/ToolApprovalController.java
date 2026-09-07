package com.shiyu.ai.agent.web;

import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.runtime.*;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agent")
public class ToolApprovalController {
    private final ToolApprovalService approvals;
    private final AiRuntimeService runtime;
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime) { this.approvals = approvals; this.runtime = runtime; }
    @GetMapping("/runs/{runId}/approvals") public Result<List<ToolApproval>> list(@PathVariable String runId) { runtime.requireRun(runId, tenant(), user()); return Result.success(approvals.list(runId, tenant(), user())); }
    @GetMapping("/approvals") public Result<List<ToolApproval>> listAll() { return Result.success(approvals.listAll(tenant(), user())); }
    @PostMapping("/runs/{runId}/approvals") public Result<ToolApproval> request(@PathVariable String runId, @Valid @RequestBody Request request) { runtime.requireRun(runId, tenant(), user()); ToolApproval approval = approvals.request(runId, tenant(), user(), request.toolName, request.argumentsRedacted); runtime.append(runtime.requireRun(runId, tenant(), user()), AiRunEventType.TOOL_APPROVAL_REQUIRED, "{\"approvalId\":\"" + approval.id() + "\"}", true); return Result.success(approval); }
    @PostMapping("/approvals/{id}/approve") public Result<ToolApproval> approve(@PathVariable String id) { return Result.success(decide(id, ToolApprovalStatus.APPROVED)); }
    @PostMapping("/approvals/{id}/reject") public Result<ToolApproval> reject(@PathVariable String id) { return Result.success(decide(id, ToolApprovalStatus.REJECTED)); }
    private ToolApproval decide(String id, ToolApprovalStatus status) {
        ToolApproval before = approvals.require(id, tenant(), user());
        ToolApproval value = approvals.decide(id, tenant(), user(), status);
        if (before == null || before.status() == ToolApprovalStatus.PENDING && value.status() != ToolApprovalStatus.PENDING) {
            runtime.append(runtime.requireRun(value.runId(), tenant(), user()), AiRunEventType.TOOL_APPROVAL_DECIDED,
                    "{\"approvalId\":\"" + value.id() + "\",\"status\":\"" + value.status().name() + "\"}", true);
        }
        return value;
    }
    private TenantId tenant(){return new TenantId(ActorContextHttpAdapter.tenantId());}
    private long user(){return ActorContextHttpAdapter.userId();}
    @Data public static class Request { private String toolName; private String argumentsRedacted = "{}"; }
}
