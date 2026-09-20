package com.shiyu.ai.conversation.implementation.domain.port;

import com.shiyu.ai.kernel.context.TenantId;

import java.util.Optional;

/**
 * 负责 Idempotency 的持久化查询、保存和删除，并维护数据访问边界。
 */
public interface IdempotencyRepository {
    /**
     * 查询 Idempotency 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param operation 用于完成本次业务处理的 operation 参数。
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回可能存在的业务对象；不存在时返回空值容器。
     */
    Optional<String> find(TenantId tenantId, long ownerUserId, String operation, String key);

    /**
     * 执行 Idempotency 相关业务数据，并返回处理结果。
     *
     * @param tenantId 当前操作涉及的租户标识。
     * @param ownerUserId 当前操作涉及的用户标识。
     * @param operation 用于完成本次业务处理的 operation 参数。
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param resourceId 用于定位resource的标识。
     * @return 返回本次条件判断是否成立。
     */
    boolean claim(
            TenantId tenantId, long ownerUserId, String operation, String key, String resourceId);
}
