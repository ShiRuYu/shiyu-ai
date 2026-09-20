package com.shiyu.ai.common.storage.api;

import java.time.Duration;

/**
 * 管理 Distributed Lease 相关的运行时状态、注册信息或临时数据。
 */
public interface DistributedLeaseStore extends LeaseStore {

    /**
     * 执行 Distributed Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    default boolean acquire(String key, Duration ttl) {
        return tryAcquire(key, owner(key), ttl);
    }

    /**
     * 执行 Distributed Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @param ttl 用于完成本次业务处理的 ttl 参数。
     * @return 返回本次条件判断是否成立。
     */
    default boolean renew(String key, Duration ttl) {
        return renew(key, owner(key), ttl);
    }

    /**
     * 执行 Distributed Lease 相关业务操作，并维护必要的状态和协作关系。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     */
    default void release(String key) {
        release(key, owner(key));
    }

    /**
     * 执行 Distributed Lease 相关业务数据，并返回处理结果。
     *
     * @param key 用于定位或筛选目标业务对象的业务值。
     * @return 返回 Distributed Lease 相关操作生成的结果数据。
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
