package com.shiyu.ai.memory.implementation.domain.magma.port;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryEntity;
import com.shiyu.ai.memory.implementation.domain.magma.model.MemoryRetrievalTrace;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 负责 Magma 记忆 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface MagmaMemoryRepository {
    /**
     * 创建或保存 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param event 本次流程携带的事件或业务数据。
     */
    void insertEvent(MemoryEvent event);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param eventId 用于定位event的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<MemoryEvent> findEvent(TenantId tenantId, String eventId);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param subjectType 用于完成本次业务处理的 subjectType 参数。
     * @param subjectId 用于定位subject的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<MemoryEvent> findLatestEvent(
            TenantId tenantId, String namespace, String subjectType, String subjectId);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param subjectType 用于完成本次业务处理的 subjectType 参数。
     * @param subjectId 用于定位subject的标识。
     * @param occurredAt 用于完成本次业务处理的 occurredAt 参数。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<MemoryEvent> findPreviousEvent(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            Instant occurredAt);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param subjectType 用于完成本次业务处理的 subjectType 参数。
     * @param subjectId 用于定位subject的标识。
     * @param occurredAt 用于完成本次业务处理的 occurredAt 参数。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<MemoryEvent> findNextEvent(
            TenantId tenantId,
            String namespace,
            String subjectType,
            String subjectId,
            Instant occurredAt);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param subjectType 用于完成本次业务处理的 subjectType 参数。
     * @param subjectId 用于定位subject的标识。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemoryEvent> findCandidates(
            TenantId tenantId, String namespace, String subjectType, String subjectId, int limit);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param namespace 用于完成本次业务处理的 namespace 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemoryEvent> findByNamespace(TenantId tenantId, String namespace, int limit);

    /**
     * 更新或设置 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param eventId 用于定位event的标识。
     * @param status 用于完成本次业务处理的 status 参数。
     */
    void updateEventStatus(TenantId tenantId, String eventId, MemoryEventStatus status);

    /**
     * 执行 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param nodeId 用于定位node的标识。
     */
    void deactivateEdgesForNode(TenantId tenantId, String nodeId);

    /**
     * 执行 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param entity 用于完成本次业务处理的 entity 参数。
     */
    void upsertEntity(MemoryEntity entity);

    /**
     * 创建或保存 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param edge 用于完成本次业务处理的 edge 参数。
     */
    void insertEdge(MemoryEdge edge);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param nodeId 用于定位node的标识。
     * @param graphType 用于完成本次业务处理的 graphType 参数。
     * @param limit 每页返回的数据数量。
     * @return 返回符合条件的数据集合；没有匹配项时返回空集合。
     */
    List<MemoryEdge> findEdges(TenantId tenantId, String nodeId, GraphType graphType, int limit);

    /**
     * 执行 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param eventId 用于定位event的标识。
     */
    void enqueueConsolidation(TenantId tenantId, String eventId);

    /**
     * 执行 Magma 记忆 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param trace 用于完成本次业务处理的 trace 参数。
     */
    void recordRetrievalTrace(MemoryRetrievalTrace trace);

    /**
     * 查询 Magma 记忆 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param traceId 用于定位trace的标识。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<MemoryRetrievalTrace> findRetrievalTrace(TenantId tenantId, String traceId);
}
