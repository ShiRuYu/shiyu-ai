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
 * {@code ToolApprovalController} 是智能体模块的 Web 接口适配器，负责接收请求并转换为应用服务调用。
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
     * {@code ToolApprovalController} 创建并初始化当前类型实例。
     *
     * @param approvals 参数值，用于执行当前操作。
     * @param runtime 参数值，用于执行当前操作。
     */
    public ToolApprovalController(ToolApprovalService approvals, AiRuntimeService runtime) {
        this.approvals = approvals;
        this.runtime = runtime;
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param runId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/runs/{runId}/approvals")
    public Result<List<ToolApproval>> list(@PathVariable String runId) {
        runtime.requireRun(runId, tenant(), user());
        return Result.success(approvals.list(runId, tenant(), user()));
    }

    /**
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @return 返回当前操作产生的结果。
     */
    @GetMapping("/approvals")
    public Result<List<ToolApproval>> listAll() {
        return Result.success(approvals.listAll(tenant(), user()));
    }

    /**
     * {@code request} 执行当前类型定义的业务操作。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param request 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code approve} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @PostMapping("/approvals/{id}/approve")
    public Result<ToolApproval> approve(@PathVariable String id) {
        return Result.success(decide(id, ToolApprovalStatus.APPROVED));
    }

    /**
     * {@code reject} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code Request} 表示智能体模块的请求参数，承载调用方提交的输入数据。
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
