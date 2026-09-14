package com.shiyu.ai.common.storage.api;

import java.time.Duration;

/**
 * DistributedLeaseStore 接口，定义基础设施模块的能力边界。
 */
public interface DistributedLeaseStore extends LeaseStore {

    /**
     * 执行 {@code acquire} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param ttl 方法参数。
     *
     * @return 条件是否满足。
     */
    default boolean acquire(String key, Duration ttl) {
        return tryAcquire(key, owner(key), ttl);
    }

    /**
     * 执行 {@code renew} 定义的接口操作。
     *
     * @param key 方法参数。
     * @param ttl 方法参数。
     *
     * @return 条件是否满足。
     */
    default boolean renew(String key, Duration ttl) {
        return renew(key, owner(key), ttl);
    }

    /**
     * 变更当前业务对象的处理状态。
     *
     * @param key 方法参数。
     */
    default void release(String key) {
        release(key, owner(key));
    }

    /**
     * 执行 {@code owner} 定义的接口操作。
     *
     * @param key 方法参数。
     *
     * @return 操作结果。
     */
    private String owner(String key) {
        return getClass().getName()
                + "@"
                + System.identityHashCode(this)
                + ":thread-"
                + Thread.currentThread().threadId()
                + ":"
                + key;
    }
}
