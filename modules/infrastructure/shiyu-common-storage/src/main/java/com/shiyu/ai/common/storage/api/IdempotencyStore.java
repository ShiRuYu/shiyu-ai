package com.shiyu.ai.common.storage.api;

import java.time.Duration;

/**
 * 管理 Idempotency 相关的运行时状态、注册信息或临时数据。
 */
public interface IdempotencyStore {

    /**
     * 执行 Idempotency 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    boolean putIfAbsent(String key, Duration ttl);

    /**
     * 执行 Idempotency 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回本次条件判断是否成立。
     */
    boolean contains(String key);
}
