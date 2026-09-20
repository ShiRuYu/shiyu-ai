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
 * 提供 工具 Approval 的查询、创建、更新及调用服务，协调业务变更和领域协作。
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
     * 构建或转换 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approvals 用于完成本次业务处理的 approvals 参数。
     */
    @Autowired
    public ToolApprovalService(ToolApprovalRepository approvals) {
        this.approvals = approvals;
    }

    /**
     * 执行 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param toolName 用于完成本次业务处理的 toolName 参数。
     * @param argumentsRedacted 用于完成本次业务处理的 argumentsRedacted 参数。
     * @return 返回 工具 Approval 相关操作生成的结果数据。
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
     * 查询 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) {
        TenantId tenant = requireTenant(tenantId);
        approvals.expirePending(tenant, ownerUserId);
        return approvals.list(runId, tenant, ownerUserId);
    }

    /**
     * 查询 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) {
        TenantId tenant = requireTenant(tenantId);
        approvals.expirePending(tenant, ownerUserId);
        return approvals.listAll(tenant, ownerUserId);
    }

    /**
     * 获取并校验 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 工具 Approval 相关操作生成的结果数据。
     */
    public ToolApproval require(String id, TenantId tenantId, long ownerUserId) {
        TenantId tenant = requireTenant(tenantId);
        approvals.expirePending(tenant, ownerUserId);
        return approvals
                .find(id, tenant, ownerUserId)
                .orElseThrow(() -> new IllegalArgumentException("approval not found"));
    }

    /**
     * 执行 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param status 用于完成本次业务处理的 status 参数。
     * @return 返回 工具 Approval 相关操作生成的结果数据。
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
