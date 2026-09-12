package com.shiyu.ai.common.storage.lease;

import com.shiyu.ai.common.storage.api.DistributedLeaseStore;
import com.shiyu.ai.common.storage.backup.*;
import com.shiyu.ai.common.storage.config.*;
import com.shiyu.ai.common.storage.file.*;
import com.shiyu.ai.common.storage.metadata.*;
import com.shiyu.ai.common.storage.rate.*;
import com.shiyu.ai.common.storage.security.*;
import com.shiyu.ai.common.storage.vector.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@code LocalLeaseStore} 承载平台基础设施模块的领域状态或协作行为，负责维护本类型的职责边界。
 */
@Component
@ConditionalOnProperty(
        prefix = "shiyu.infrastructure.redis",
        name = "provider",
        havingValue = "disabled",
        matchIfMissing = true)
public class LocalLeaseStore implements DistributedLeaseStore {
    private final ConcurrentHashMap<String, Lease> leases = new ConcurrentHashMap<>();

    /**
     * {@code tryAcquire} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param ttl 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean tryAcquire(String key, String owner, Duration ttl) {
        long expires = System.nanoTime() + ttl.toNanos();
        return leases.compute(
                        key,
                        (k, old) ->
                                old == null || old.expiresAt < System.nanoTime()
                                        ? new Lease(owner, expires)
                                        : old)
                .owner
                .equals(owner);
    }

    /**
     * {@code renew} 执行当前类型定义的业务操作。
     *
     * @param key 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     * @param ttl 参数值，用于执行当前操作。
     *
     * @return 返回当前操作产生的结果。
     */
    public boolean renew(String key, String owner, Duration ttl) {
        return leases.computeIfPresent(
                                key,
                                (k, old) ->
                                        old.owner.equals(owner)
                                                ? new Lease(
                                                        owner, System.nanoTime() + ttl.toNanos())
                                                : old)
                        != null
                && owner.equals(leases.get(key).owner);
    }

    /**
     * {@code release} 释放或移除当前操作涉及的资源。
     *
     * @param key 参数值，用于执行当前操作。
     * @param owner 参数值，用于执行当前操作。
     */
    public void release(String key, String owner) {
        leases.computeIfPresent(key, (k, old) -> old.owner.equals(owner) ? null : old);
    }

    /**
     * {@code Lease} 封装平台基础设施模块中不可变的结构化数据，并作为相关操作之间的值对象。
     * @param owner owner 属性，表示该记录组件承载的数据。
     * @param expiresAt 过期时间，表示该记录组件承载的数据。
     */
    private record Lease(String owner, long expiresAt) {}
}
