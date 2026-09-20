package com.shiyu.ai.agent.implementation.web;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.service.AiRuntimeService;
import com.shiyu.ai.agent.implementation.runtime.service.ToolApprovalService;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.common.core.api.Result;
import com.shiyu.ai.common.web.auth.ActorContextHttpAdapter;
import com.shiyu.ai.kernel.context.TenantId;

import jakarta.validation.Valid;

import lombok.Data;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处理 工具 Approval 相关的 Web 请求，并将请求转换为应用服务调用。
 */
@RestController
@RequestMapping("/api/agent")
public class ToolApprovalController {
    /**
     * approvals 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolApprovalService approvals;
    /**
     * runtime 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final AiRuntimeService runtime;

    /**
     * 构建或转换 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     * @param runtime 用于完成本次业务处理的 runtime 参数。
     */
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime) {
        this.approvals = approvals;
        this.runtime = runtime;
    }

    /**
     * 查询 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @GetMapping("/runs/{runId}/approvals")
    public Result<List<ToolApproval>> list(@PathVariable String runId) {
        runtime.requireRun(runId, tenant(), user());
        return Result.success(approvals.list(runId, tenant(), user()));
    }

    /**
     * 查询 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @GetMapping("/approvals")
    public Result<List<ToolApproval>> listAll() {
        return Result.success(approvals.listAll(tenant(), user()));
    }

    /**
     * 执行 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @PostMapping("/runs/{runId}/approvals")
    public Result<ToolApproval> request(
            @PathVariable String runId, @Valid @RequestBody Request request) {
        runtime.requireRun(runId, tenant(), user());
        ToolApproval approval =
                approvals.request(
                        runId, tenant(), user(), request.toolName, request.argumentsRedacted);
        runtime.append(
                runtime.requireRun(runId, tenant(), user()),
                AiRunEventType.TOOL_APPROVAL_REQUIRED,
                "{\"approvalId\":\"" + approval.id() + "\"}",
                true);
        return Result.success(approval);
    }

    /**
     * 执行 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approve 用于完成本次业务处理的 approve 参数。
     */
    @PostMapping("/approvals/{id}/approve")
    public Result<ToolApproval> approve(@PathVariable String id) {
        return Result.success(decide(id, ToolApprovalStatus.APPROVED));
    }

    /**
     * 执行 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param reject 用于完成本次业务处理的 reject 参数。
     */
    @PostMapping("/approvals/{id}/reject")
    public Result<ToolApproval> reject(@PathVariable String id) {
        return Result.success(decide(id, ToolApprovalStatus.REJECTED));
    }

    private ToolApproval decide(String id, ToolApprovalStatus status) {
        ToolApproval before = approvals.require(id, tenant(), user());
        ToolApproval value = approvals.decide(id, tenant(), user(), status);
        if (before == null
                || before.status() == ToolApprovalStatus.PENDING
                        && value.status() != ToolApprovalStatus.PENDING) {
            runtime.append(
                    runtime.requireRun(value.runId(), tenant(), user()),
                    AiRunEventType.TOOL_APPROVAL_DECIDED,
                    "{\"approvalId\":\""
                            + value.id()
                            + "\",\"status\":\""
                            + value.status().name()
                            + "\"}",
                    true);
        }
        return value;
    }

    private TenantId tenant() {
        return new TenantId(ActorContextHttpAdapter.tenantId());
    }

    private long user() {
        return ActorContextHttpAdapter.userId();
    }

    /**
     * 封装 Request 操作所需的请求条件和输入数据。
     */
    @Data
    public static class Request {
        private String toolName;
        /**
         * argumentsRedacted 属性，保存当前对象中的业务数据或协作依赖。
         */
        private String argumentsRedacted = "{}";
    }
}
