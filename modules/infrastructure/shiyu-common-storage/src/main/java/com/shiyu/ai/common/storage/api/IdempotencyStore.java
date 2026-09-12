package com.shiyu.ai.common.storage.api;

import java.time.Duration;

/**
 * IdempotencyStore 接口，定义基础设施模块的能力边界。
 */
public interface IdempotencyStore {

    /**
     * 执行 {@code putIfAbsent} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param ttl 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean putIfAbsent(String key, Duration ttl);

    /**
     * 执行 {@code contains} 定义的接口操作。
     *
     * @param key 方法参数。
     *
     * @return 条件是否满足。
     */
    boolean contains(String key);
}
