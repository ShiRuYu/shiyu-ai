package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Optional;

/**
 * IdempotencyRepository 仓储接口，负责访问和持久化会话领域聚合数据。
 */
public interface IdempotencyRepository {
    /**
     * 根据标识查询对应的数据。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param operation 方法参数。
     * @param key 方法参数。
     *
     * @return 查询到的结果；未找到时为空。
     */
    Optional<String> find(TenantId tenantId, long ownerUserId, String operation, String key);

    /**
     * 执行 {@code claim} 定义的接口操作。
     *
     * @param tenantId 租户标识。
     * @param ownerUserId 方法参数。
     * @param operation 方法参数。
     * @param key 方法参数。
     * @param resourceId 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId);
}
