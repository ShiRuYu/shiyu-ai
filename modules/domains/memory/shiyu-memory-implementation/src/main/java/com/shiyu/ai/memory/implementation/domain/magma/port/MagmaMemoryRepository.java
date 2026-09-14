package com.shiyu.ai.memory.implementation.domain.magma.port;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryEntity;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryRetrievalTrace;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MagmaMemoryRepository 仓储接口，负责访问和持久化记忆领域聚合数据。
 */
public interface MagmaMemoryRepository {
    /**
     * 创建并保存业务对象。
     *
     * @param event 方法参数。
     */
    void insertEvent(MemoryEvent event);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param eventId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<MemoryEvent> findEvent(TenantId tenantId, String eventId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param subjectType 方法参数。
     * @param subjectId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<MemoryEvent> findLatestEvent(
            TenantId tenantId, String namespace, String subjectType, String subjectId);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param subjectType 方法参数。
     * @param subjectId 方法参数。
     * @param occurredAt 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<MemoryEvent> findPreviousEvent(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            Instant occurredAt);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param subjectType 方法参数。
     * @param subjectId 方法参数。
     * @param occurredAt 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<MemoryEvent> findNextEvent(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            Instant occurredAt);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param subjectType 方法参数。
     * @param subjectId 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MemoryEvent> findCandidates(
            TenantId tenantId, String namespace, String subjectType, String subjectId, int limit);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MemoryEvent> findByNamespace(TenantId tenantId, String namespace, int limit);

    /**
     * 更新业务对象及其关联数据。
     *
     * @param tenantId 租户标识。
     * @param eventId 方法参数。
     * @param status 对象状态。
     */
    void updateEventStatus(TenantId tenantId, String eventId, MemoryEventStatus status);

    /**
     * 执行 {@code deactivateEdgesForNode} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param nodeId 方法参数。
     */
    void deactivateEdgesForNode(TenantId tenantId, String nodeId);

    /**
     * 保存或更新业务对象。
     *
     * @param entity 方法参数。
     */
    void upsertEntity(MemoryEntity entity);

    /**
     * 创建并保存业务对象。
     *
     * @param edge 方法参数。
     */
    void insertEdge(MemoryEdge edge);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param nodeId 方法参数。
     * @param graphType 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MemoryEdge> findEdges(TenantId tenantId, String nodeId, GraphType graphType, int limit);

    /**
     * 执行 {@code enqueueConsolidation} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param eventId 方法参数。
     */
    void enqueueConsolidation(TenantId tenantId, String eventId);

    /**
     * 执行 {@code recordRetrievalTrace} 定义的接口操作。
     *
     * @param trace 方法参数。
     */
    void recordRetrievalTrace(MemoryRetrievalTrace trace);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param tenantId 租户标识。
     * @param traceId 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<MemoryRetrievalTrace> findRetrievalTrace(TenantId tenantId, String traceId);
}
