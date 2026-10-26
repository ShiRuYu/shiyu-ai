package com.shiyu.ai.agent.implementation.runtime.adapter.inmemory;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApproval;
import com.shiyu.ai.agent.implementation.runtime.model.ToolApprovalStatus;
import com.shiyu.ai.agent.implementation.runtime.port.ToolApprovalRepository;

import com.shiyu.ai.agent.contract.runtime.*;
import com.shiyu.ai.kernel.context.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 负责 In 记忆 工具 Approval 的持久化查询、保存和删除，并维护数据访问边界。
 */
public class InMemoryToolApprovalRepository implements ToolApprovalRepository {
    private final Map<String, ToolApproval> values = new ConcurrentHashMap<>();

    /**
     * 创建或保存 In 记忆 工具 Approval 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param approval 用于完成本次业务处理的 approval 参数。
     */
    @Override
    public void insert(ToolApproval approval) {
        if (values.putIfAbsent(approval.id(), approval) != null)
            throw new IllegalStateException("approval already exists");
    }

    /**
     * 查询 In 记忆 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param runId 用于定位run的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ToolApproval> list(String runId, TenantId tenantId, long ownerUserId) {
        long value = tenant(tenantId);
        return values.values().stream()
                .filter(
                        a ->
                                a.runId().equals(runId)
                                        && a.tenantId() == value
                                        && a.ownerUserId() == ownerUserId)
                .sorted(
                        java.util.Comparator.comparing(ToolApproval::createdAt)
                                .thenComparing(ToolApproval::id))
                .toList();
    }

    /**
     * 查询 In 记忆 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    @Override
    public List<ToolApproval> listAll(TenantId tenantId, long ownerUserId) {
        long value = tenant(tenantId);
        return values.values().stream()
                .filter(a -> a.tenantId() == value && a.ownerUserId() == ownerUserId)
                .sorted(
                        java.util.Comparator.comparing(ToolApproval::createdAt)
                                .reversed()
                                .thenComparing(ToolApproval::id))
                .toList();
    }

    /**
     * 查询 In 记忆 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param id 用于定位目标业务对象的标识。
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    @Override
    public Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId) {
        long value = tenant(tenantId);
        return Optional.ofNullable(values.get(id))
                .filter(a -> a.tenantId() == value && a.ownerUserId() == ownerUserId);
    }

    /**
     * 更新或设置 In 记忆 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param approval 用于完成本次业务处理的 approval 参数。
     * @param expectedStatus 用于完成本次业务处理的 expectedStatus 参数。
     * @return 返回 In 记忆 工具 Approval 相关操作生成的结果数据。
     */
    @Override
    public int update(ToolApproval approval, ToolApprovalStatus expectedStatus) {
        ToolApproval[] replaced = {null};
        values.computeIfPresent(
                approval.id(),
                (id, current) -> {
                    if (current.status() != expectedStatus) return current;
                    replaced[0] = approval;
                    return approval;
                });
        return replaced[0] == approval ? 1 : 0;
    }

    /**
     * 执行 In 记忆 工具 Approval 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @return 返回 In 记忆 工具 Approval 相关操作生成的结果数据。
     */
    @Override
    public int expirePending(TenantId tenantId, long ownerUserId) {
        long value = tenant(tenantId);
        int[] count = {0};
        values.replaceAll(
                (id, current) -> {
                    if (current.tenantId() == value
                            && current.ownerUserId() == ownerUserId
                            && current.status() == ToolApprovalStatus.PENDING
                            && Instant.now().isAfter(current.expiresAt())) {
                        count[0]++;
                        return new ToolApproval(
                                current.id(),
                                current.runId(),
                                current.tenantId(),
                                current.ownerUserId(),
                                current.toolName(),
                                current.argumentsRedacted(),
                                ToolApprovalStatus.EXPIRED,
                                current.createdAt(),
                                Instant.now(),
                                current.expiresAt());
                    }
                    return current;
                });
        return count[0];
    }

    @Override
    public List<ToolApproval> findExpiryCompensationWork(Instant now, int limit) {
        return values.values().stream()
                .filter(value -> (value.status() == ToolApprovalStatus.PENDING
                                && value.expiresAt().isBefore(now))
                        || value.status() == ToolApprovalStatus.EXPIRED
                        || (value.status() == ToolApprovalStatus.COMPENSATING
                                && value.decidedAt() != null
                                && !value.decidedAt().isAfter(now)))
                .sorted(java.util.Comparator.comparing(ToolApproval::expiresAt)
                        .thenComparing(ToolApproval::id))
                .limit(Math.max(1, Math.min(limit, 100)))
                .toList();
    }

    @Override
    public int claimExpiryCompensation(
            ToolApproval candidate, Instant now, Instant leaseUntil) {
        ToolApproval[] claimed = {null};
        values.computeIfPresent(candidate.id(), (id, current) -> {
            boolean eligible = current.status() == ToolApprovalStatus.EXPIRED
                    || current.status() == ToolApprovalStatus.PENDING
                            && current.expiresAt().isBefore(now)
                    || current.status() == ToolApprovalStatus.COMPENSATING
                            && current.decidedAt() != null
                            && !current.decidedAt().isAfter(now);
            if (!eligible || current.status() != candidate.status()
                    || !java.util.Objects.equals(current.decidedAt(), candidate.decidedAt())
                    || current.tenantId() != candidate.tenantId()
                    || current.ownerUserId() != candidate.ownerUserId()) return current;
            claimed[0] = new ToolApproval(
                    current.id(), current.runId(), current.tenantId(), current.ownerUserId(),
                    current.toolName(), current.argumentsRedacted(),
                    ToolApprovalStatus.COMPENSATING, current.createdAt(), leaseUntil,
                    current.expiresAt());
            return claimed[0];
        });
        return claimed[0] == null ? 0 : 1;
    }

    @Override
    public int completeExpiryCompensation(
            String id, TenantId tenantId, long ownerUserId,
            Instant leaseUntil, Instant completedAt) {
        long value = tenant(tenantId);
        ToolApproval[] completed = {null};
        values.computeIfPresent(id, (key, current) -> {
            if (current.tenantId() != value || current.ownerUserId() != ownerUserId
                    || current.status() != ToolApprovalStatus.COMPENSATING
                    || !java.util.Objects.equals(current.decidedAt(), leaseUntil)) return current;
            completed[0] = new ToolApproval(
                    current.id(), current.runId(), current.tenantId(), current.ownerUserId(),
                    current.toolName(), current.argumentsRedacted(),
                    ToolApprovalStatus.COMPENSATED, current.createdAt(), completedAt,
                    current.expiresAt());
            return completed[0];
        });
        return completed[0] == null ? 0 : 1;
    }

    private static long tenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }
}
