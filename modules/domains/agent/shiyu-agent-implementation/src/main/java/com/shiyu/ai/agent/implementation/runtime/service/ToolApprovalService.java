package com.shiyu.ai.agent.implementation.runtime.service;
import com.shiyu.ai.agent.implementation.runtime.adapter.inmemory.InMemoryToolApprovalRepository;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * {@code ToolApprovalService} 定义智能体模块的应用服务能力，供上层用例调用。
 */
@Service
public class ToolApprovalService {
    /**
     * approvals 属性，保存当前对象中的业务数据或协作依赖。
     */
    private final ToolApprovalRepository approvals;

    /**
     * {@code ToolApprovalService} 创建并初始化当前类型实例。
     */
    public ToolApprovalService() {
        this(new InMemoryToolApprovalRepository());
    }

    /**
     * {@code ToolApprovalService} 创建并初始化当前类型实例。
     *
     * @param approvals 参数值，用于执行当前操作。
     */
    @Autowired
    public ToolApprovalService(ToolApprovalRepository approvals) {
        this.approvals = approvals;
    }

    /**
     * {@code request} 执行当前类型定义的业务操作。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param toolName 参数值，用于执行当前操作。
     * @param argumentsRedacted 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ToolApproval request(
            String runId,
            TenantId tenantId,
            long ownerUserId,
            String toolName,
            String argumentsRedacted) {
        TenantId tenant = requireTenant(tenantId);
        Instant now = Instant.now();
        ToolApproval value =
                new ToolApproval(
                        UUID.randomUUID().toString(),
                        runId,
                        tenant.value(),
                        ownerUserId,
                        toolName,
                        argumentsRedacted,
                        ToolApprovalStatus.PENDING,
                        now,
                        null,
                        now.plusSeconds(300));
        approvals.insert(value);
        return value;
    }

    /**
     * {@code list} 查询并返回当前操作所需的数据。
     *
     * @param runId 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) {
        TenantId tenant = requireTenant(tenantId);
        approvals.expirePending(tenant, ownerUserId);
        return approvals.list(runId, tenant, ownerUserId);
    }

    /**
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) {
        TenantId tenant = requireTenant(tenantId);
        approvals.expirePending(tenant, ownerUserId);
        return approvals.listAll(tenant, ownerUserId);
    }

    /**
     * {@code require} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ToolApproval require(String id, TenantId tenantId, long ownerUserId) {
        TenantId tenant = requireTenant(tenantId);
        approvals.expirePending(tenant, ownerUserId);
        return approvals
                .find(id, tenant, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("approval not found"));
    }

    /**
     * {@code decide} 执行当前类型定义的业务操作。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     * @param status 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public ToolApproval decide(
            String id, TenantId tenantId, long ownerUserId, ToolApprovalStatus status) {
        TenantId tenant = requireTenant(tenantId);
        if (status != ToolApprovalStatus.APPROVED && status != ToolApprovalStatus.REJECTED) {
            throw new IllegalArgumentException("approval decision must be APPROVED or REJECTED");
        }
        approvals.expirePending(tenant, ownerUserId);
        ToolApproval current =
                approvals
                        .find(id, tenant, ownerUserId)
                        .orElseThrow(() -> new IllegalArgumentException("approval not found"));
        if (current.status() != ToolApprovalStatus.PENDING) return current;
        Instant now = Instant.now();
        ToolApprovalStatus nextStatus =
                now.isAfter(current.expiresAt()) ? ToolApprovalStatus.EXPIRED : status;
        ToolApproval next =
                new ToolApproval(
                        current.id(),
                        current.runId(),
                        current.tenantId(),
                        current.ownerUserId(),
                        current.toolName(),
                        current.argumentsRedacted(),
                        nextStatus,
                        current.createdAt(),
                        now,
                        current.expiresAt());
        if (approvals.update(next, ToolApprovalStatus.PENDING) != 1)
            return approvals.find(id, tenant, ownerUserId).orElseThrow();
        return next;
    }

    private static TenantId requireTenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId;
    }
}
