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
 * {@code InMemoryToolApprovalRepository} 定义智能体模块的持久化端口，隔离领域逻辑与具体存储实现。
 */
public class InMemoryToolApprovalRepository implements ToolApprovalRepository {
    private final Map<String, ToolApproval> values = new ConcurrentHashMap<>();

    /**
     * {@code insert} 执行当前类型定义的业务操作。
     *
     * @param approval 参数值，用于执行当前操作。
     */
    @Override
    public void insert(ToolApproval approval) {
        if (values.putIfAbsent(approval.id(), approval) != null)
            throw new IllegalStateException("approval already exists");
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
     * {@code listAll} 查询并返回当前操作所需的数据。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code find} 查询并返回当前操作所需的数据。
     *
     * @param id 参数值，用于执行当前操作。
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    @Override
    public Optional<ToolApproval> find(String id, TenantId tenantId, long ownerUserId) {
        long value = tenant(tenantId);
        return Optional.ofNullable(values.get(id))
                .filter(a -> a.tenantId() == value && a.ownerUserId() == ownerUserId);
    }

    /**
     * {@code update} 写入或更新当前模块中的业务数据。
     *
     * @param approval 参数值，用于执行当前操作。
     * @param expectedStatus 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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
     * {@code expirePending} 执行当前类型定义的业务操作。
     *
     * @param tenantId 参数值，用于执行当前操作。
     * @param ownerUserId 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
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

    private static long tenant(TenantId tenantId) {
        if (tenantId == null) throw new IllegalArgumentException("tenantId is required");
        return tenantId.value();
    }
}
