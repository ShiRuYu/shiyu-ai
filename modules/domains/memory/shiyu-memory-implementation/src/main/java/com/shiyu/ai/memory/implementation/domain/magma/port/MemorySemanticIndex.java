package com.shiyu.ai.memory.implementation.domain.magma.port;

import com.shiyu.ai.kernel.context.TenantId;
import com.shiyu.ai.memory.contract.model.*;

import java.util.List;

/**
 * MemorySemanticIndex 接口，定义记忆模块的能力边界。
 */
public interface MemorySemanticIndex {
    /**
     * 保存或更新业务对象。
     *
     * @param event 方法参数。
     */
    void upsert(MemoryEvent event);

    /**
     * 根据条件查询并返回所需数据。
     *
     * @param query 方法参数。
     * @param limit 方法参数。
     *
     * @return 符合条件的结果集合。
     */
    List<MemoryPath> search(MemoryQuery query, int limit);

    /**
     * 删除指定业务对象或关联数据。
     *
     * @param eventId 方法参数。
     */
    void delete(String eventId);

    /**
     * 执行 {@code rebuild} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param namespace 方法参数。
     */
    void rebuild(TenantId tenantId, String namespace);
}
